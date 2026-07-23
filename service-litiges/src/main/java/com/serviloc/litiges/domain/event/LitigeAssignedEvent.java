// domain/event/LitigeAssignedEvent.java
package com.serviloc.litiges.domain.event;

/** Routing key: litige.assigned — notifie l'agent de son affectation. */
public record LitigeAssignedEvent(String litigeId, String agentId) {}
