package com.serviloc.negociations.infrastructure.persistence;

import com.serviloc.negociations.domain.model.Message;
import com.serviloc.negociations.domain.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MessageRepositoryAdapter implements MessageRepository {

    private final MessageJpaRepository jpa;

    public MessageRepositoryAdapter(MessageJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Message save(Message m) {
        MessageJpaEntity entity = new MessageJpaEntity(
                m.getId(), m.getConversationId(), m.getSenderId(),
                m.getSenderRole(), m.getContent(), m.getImageId()
        );
        entity.setRead(m.isRead());
        return toDomain(jpa.save(entity));
    }

    @Override
    public Page<Message> findByConversationIdOrderBySentAtDesc(
            UUID conversationId, Pageable pageable) {
        return jpa.findByConversationIdOrderBySentAtDesc(conversationId, pageable)
                .map(this::toDomain);
    }

    private Message toDomain(MessageJpaEntity e) {
        try {
            var ctor = Message.class.getDeclaredConstructor(
                    UUID.class, UUID.class, UUID.class, String.class,
                    String.class, String.class, boolean.class,
                    java.time.LocalDateTime.class
            );
            ctor.setAccessible(true);
            return ctor.newInstance(
                    e.getId(), e.getConversationId(), e.getSenderId(),
                    e.getSenderRole(), e.getContent(), e.getImageId(),
                    e.isRead(), e.getSentAt()
            );
        } catch (Exception ex) {
            throw new RuntimeException("Erreur reconstitution Message", ex);
        }
    }
}