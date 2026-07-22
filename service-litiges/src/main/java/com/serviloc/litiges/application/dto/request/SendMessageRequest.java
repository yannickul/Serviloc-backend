// application/dto/request/SendMessageRequest.java
package com.serviloc.litiges.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(@NotBlank String content) {}
