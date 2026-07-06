// infrastructure/external/NegociationClient.java
package com.serviloc.litiges.infrastructure.external;

import com.serviloc.litiges.infrastructure.external.dto.ConversationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-negociations", fallback = NegociationClientFallback.class)
public interface NegociationClient {

    @GetMapping("/internal/conversations/{demandId}")
    ConversationDto getConversation(@PathVariable String demandId);
}