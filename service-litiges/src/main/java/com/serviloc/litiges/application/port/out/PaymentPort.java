// application/port/out/PaymentPort.java
package com.serviloc.litiges.application.port.out;

import java.math.BigDecimal;

public interface PaymentPort {
    void freezeTransaction(String transactionId);
    void refund(String transactionId, BigDecimal amount, String reason);
}