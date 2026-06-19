package com.serviloc.paiement.application.service;

import com.serviloc.paiement.domain.model.*;
import com.serviloc.paiement.domain.repository.*;
import com.serviloc.paiement.infrastructure.external.MobileMoneyClient;
import com.serviloc.paiement.infrastructure.messaging.PaymentEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Service
@Transactional
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final TransactionRepository transactionRepository;
    private final PayoutRepository payoutRepository;
    private final CommissionConfigRepository commissionConfigRepository;
    private final MobileMoneyClient mobileMoneyClient;
    private final PaymentEventPublisher eventPublisher;

    @Value("${payment.commission.standard-rate:10.0}")
    private double defaultStandardRate;

    public PaymentService(TransactionRepository transactionRepository,
                          PayoutRepository payoutRepository,
                          CommissionConfigRepository commissionConfigRepository,
                          MobileMoneyClient mobileMoneyClient,
                          PaymentEventPublisher eventPublisher) {
        this.transactionRepository = transactionRepository;
        this.payoutRepository = payoutRepository;
        this.commissionConfigRepository = commissionConfigRepository;
        this.mobileMoneyClient = mobileMoneyClient;
        this.eventPublisher = eventPublisher;
    }

    // ─── Consumer: negotiation.quote_accepted ─────────────────────

    public void processQuoteAccepted(UUID quoteId, UUID demandId, UUID clientId,
                                     UUID providerId, double amount,
                                     String paymentMethod, String phoneNumber) {
        // Idempotence — vérifie si transaction déjà créée
        if (transactionRepository.findByQuoteId(quoteId).isPresent()) {
            log.warn("[PAYMENT] Transaction déjà existante pour quoteId={}", quoteId);
            return;
        }

        double commissionRate = getCommissionRate();
        Transaction transaction = Transaction.create(
                demandId, clientId, providerId, quoteId,
                amount, paymentMethod, phoneNumber, commissionRate
        );
        Transaction saved = transactionRepository.save(transaction);

        log.info("[PAYMENT] Transaction créée : id={} amount={}", saved.getId(), amount);

        // Appel Mobile Money sandbox
        MobileMoneyClient.PaymentRequest request = new MobileMoneyClient.PaymentRequest(
                phoneNumber, amount, "XAF",
                "Paiement ServiLoc demande " + demandId,
                saved.getId().toString()
        );

        MobileMoneyClient.PaymentResult result = mobileMoneyClient.initiatePayment(request);

        if (result.success()) {
            saved.confirm(result.externalRef());
            transactionRepository.save(saved);
            log.info("[PAYMENT] Paiement confirmé : transactionId={} ref={}",
                    saved.getId(), result.externalRef());
            eventPublisher.publishPaymentConfirmed(
                    saved.getId(), demandId, clientId, providerId,
                    amount, result.externalRef()
            );
        } else {
            saved.fail();
            transactionRepository.save(saved);
            log.warn("[PAYMENT] Paiement échoué : transactionId={}", saved.getId());
            eventPublisher.publishPaymentFailed(
                    saved.getId(), demandId, clientId, result.message()
            );
        }
    }

    // ─── Consumer: mission.completed ──────────────────────────────

    public void releaseFunds(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaction introuvable : " + transactionId));

        if (transaction.getStatus() != TransactionStatus.SEQUESTRE &&
                transaction.getStatus() != TransactionStatus.LITIGE) {
            log.warn("[PAYMENT] Transaction non libérable : status={}",
                    transaction.getStatus());
            return;
        }

        transaction.release();
        transactionRepository.save(transaction);

        // Crée le Payout pour le prestataire
        Payout payout = Payout.create(
                transactionId, transaction.getProviderId(),
                transaction.getNetAmount(), transaction.getCommissionAmount()
        );
        payoutRepository.save(payout);

        log.info("[PAYMENT] Fonds libérés : transactionId={} netAmount={}",
                transactionId, transaction.getNetAmount());

        eventPublisher.publishPaymentReleased(
                transactionId, transaction.getProviderId(),
                transaction.getNetAmount(), transaction.getCommissionAmount()
        );
    }

    // ─── POST /internal/transactions/create ───────────────────────

    public Transaction createTransaction(UUID demandId, UUID clientId, UUID providerId,
                                         UUID quoteId, double amount,
                                         String paymentMethod, String phoneNumber) {
        double commissionRate = getCommissionRate();
        Transaction transaction = Transaction.create(
                demandId, clientId, providerId, quoteId,
                amount, paymentMethod, phoneNumber, commissionRate
        );
        return transactionRepository.save(transaction);
    }

    // ─── POST /internal/transactions/:id/release ──────────────────

    public Transaction releaseTransaction(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaction introuvable : " + transactionId));
        transaction.release();
        return transactionRepository.save(transaction);
    }

    // ─── POST /internal/transactions/:id/refund ───────────────────

    public Transaction refundTransaction(UUID transactionId, double refundAmount) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaction introuvable : " + transactionId));

        MobileMoneyClient.PaymentRequest request = new MobileMoneyClient.PaymentRequest(
                transaction.getPhoneNumber(), refundAmount, "XAF",
                "Remboursement ServiLoc", transactionId.toString()
        );

        MobileMoneyClient.PaymentResult result = mobileMoneyClient.initiateRefund(request);

        if (result.success()) {
            transaction.refund();
            transactionRepository.save(transaction);
            log.info("[PAYMENT] Remboursement effectué : transactionId={}", transactionId);
        } else {
            throw new IllegalStateException("Remboursement échoué : " + result.message());
        }
        return transaction;
    }

    // ─── POST /internal/transactions/:id/freeze ───────────────────

    public Transaction freezeTransaction(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaction introuvable : " + transactionId));
        transaction.freeze();
        return transactionRepository.save(transaction);
    }
// ─── GET /provider/earnings ───────────────────────────────────

    @Transactional(readOnly = true)
    public ProviderEarnings getProviderEarnings(UUID providerId, int page,
                                                int limit, String month) {
        // Parse month "2026-05" → from/to
        LocalDateTime from;
        LocalDateTime to;

        if (month != null && !month.isBlank()) {
            String[] parts = month.split("-");
            int year  = Integer.parseInt(parts[0]);
            int monthNum = Integer.parseInt(parts[1]);
            from = LocalDateTime.of(year, monthNum, 1, 0, 0, 0);
            to   = from.plusMonths(1).minusSeconds(1);
        } else {
            // Mois courant par défaut
            from = LocalDateTime.now().withDayOfMonth(1).withHour(0)
                    .withMinute(0).withSecond(0);
            to   = LocalDateTime.now();
        }

        double monthlyTotal = transactionRepository
                .sumAmountByProviderIdAndCreatedAtBetween(providerId, from, to);

        List<Payout> payouts = payoutRepository.findByProviderId(providerId);

        return new ProviderEarnings(monthlyTotal, payouts);
    }

// ─── DTO interne ──────────────────────────────────────────────

    public record ProviderEarnings(
            double monthlyTotal,
            List<com.serviloc.paiement.domain.model.Payout> payouts
    ) {}
    // ─── GET /internal/stats/financials ───────────────────────────

    @Transactional(readOnly = true)
    public FinancialStats getFinancialStats(LocalDateTime from, LocalDateTime to) {
        double totalRevenue = transactionRepository.sumCommissionBetween(from, to);
        long totalSequestre = transactionRepository.countByStatus(TransactionStatus.SEQUESTRE);
        long totalLibere    = transactionRepository.countByStatus(TransactionStatus.LIBERE);
        long totalEchec     = transactionRepository.countByStatus(TransactionStatus.ECHEC);

        return new FinancialStats(totalRevenue, totalSequestre, totalLibere, totalEchec);
    }

    // ─── GET /admin/transactions ──────────────────────────────────

    @Transactional(readOnly = true)
    public Page<Transaction> getTransactions(TransactionStatus status,
                                             int page, int limit) {
        return transactionRepository.findByStatus(status, PageRequest.of(page - 1, limit));
    }

    // ─── PATCH /admin/settings/commission ────────────────────────

    public CommissionConfig updateCommission(double standardRate, double urgencyRate) {
        CommissionConfig config = commissionConfigRepository.findFirst()
                .orElse(CommissionConfig.createDefault());
        config.update(standardRate, urgencyRate);
        return commissionConfigRepository.save(config);
    }

    // ─── Helpers ──────────────────────────────────────────────────

    private double getCommissionRate() {
        return commissionConfigRepository.findFirst()
                .map(CommissionConfig::getStandardRate)
                .orElse(defaultStandardRate);
    }

    // ─── DTO interne ──────────────────────────────────────────────

    public record FinancialStats(
            double totalRevenue,
            long totalSequestre,
            long totalLibere,
            long totalEchec
    ) {}
}