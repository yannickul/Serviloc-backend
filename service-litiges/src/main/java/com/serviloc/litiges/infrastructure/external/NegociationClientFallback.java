// infrastructure/external/NegociationClientFallback.java
package com.serviloc.litiges.infrastructure.external;

import com.serviloc.litiges.infrastructure.external.dto.ConversationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NegociationClientFallback implements NegociationClient {

    @Override
    public ConversationDto getConversation(String demandId) {
        log.warn("[FALLBACK] Historique conversation indisponible pour demande {}", demandId);
        return ConversationDto.empty(demandId);
    }
}