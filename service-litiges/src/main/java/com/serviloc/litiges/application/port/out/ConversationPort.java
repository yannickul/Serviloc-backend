// application/port/out/ConversationPort.java
package com.serviloc.litiges.application.port.out;

import com.serviloc.litiges.infrastructure.external.dto.ConversationDto;

public interface ConversationPort {
    ConversationDto getConversation(String demandId);
}