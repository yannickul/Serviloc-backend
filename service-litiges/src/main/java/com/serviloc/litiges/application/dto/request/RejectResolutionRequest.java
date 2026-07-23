// application/dto/request/RejectResolutionRequest.java
package com.serviloc.litiges.application.dto.request;

/** Body optionnel de PATCH /client|provider/litiges/:id/resolution/reject. */
public record RejectResolutionRequest(String reason) {}
