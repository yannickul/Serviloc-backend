// infrastructure/external/ConversationPortAdapter.java
package com.serviloc.litiges.infrastructure.external.adapter;

import com.serviloc.litiges.application.port.out.ConversationPort;
import com.serviloc.litiges.infrastructure.external.NegociationClient;
import com.serviloc.litiges.infrastructure.external.dto.ConversationDto;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConversationPortAdapter implements ConversationPort {

    private final NegociationClient negociationClient;

    @Override
    public ConversationDto getConversation(String demandId) {
        try {
            return negociationClient.getConversation(demandId);
        } catch (Exception e) {
            log.warn("[FEIGN] service-negociations indisponible pour demandId={} — fallback vide", demandId);
            return ConversationDto.empty(demandId);
        }
    }
}