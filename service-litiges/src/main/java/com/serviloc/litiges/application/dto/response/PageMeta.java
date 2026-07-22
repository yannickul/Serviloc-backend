// application/dto/response/PageMeta.java
package com.serviloc.litiges.application.dto.response;

/** Objet meta de pagination, placé au niveau racine de l'ApiResponse (section 3 du contrat). */
public record PageMeta(int page, int limit, long total, int totalPages) {}
