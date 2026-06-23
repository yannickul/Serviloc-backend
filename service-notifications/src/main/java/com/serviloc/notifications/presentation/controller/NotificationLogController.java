package com.serviloc.notifications.presentation.controller;

import com.serviloc.notifications.application.dto.NotificationLogView;
import com.serviloc.notifications.application.dto.PagedResult;
import com.serviloc.notifications.application.port.in.GetNotificationLogsUseCase;
import com.serviloc.notifications.presentation.dto.ApiResponse;
import com.serviloc.notifications.presentation.dto.NotificationLogResponse;
import com.serviloc.notifications.presentation.dto.PageMeta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/notification-logs")
@Tag(name = "Internal - Notification Logs", description = "Audit des notifications envoyées (SMS/push/email)")
public class NotificationLogController {

    private final GetNotificationLogsUseCase getNotificationLogsUseCase;

    public NotificationLogController(GetNotificationLogsUseCase getNotificationLogsUseCase) {
        this.getNotificationLogsUseCase = getNotificationLogsUseCase;
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Liste paginée des notifications envoyées à un utilisateur",
            description = "Audit interne — appelé par l'admin ou les autres services pour vérifier "
                    + "qu'une notification a bien été envoyée (debug support client).")
    public ResponseEntity<ApiResponse<List<NotificationLogResponse>>> getNotificationLogs(
            @PathVariable String userId,
            @Parameter(description = "Numéro de page (0-indexé)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de page") @RequestParam(defaultValue = "20") int size) {

        PagedResult<NotificationLogView> result = getNotificationLogsUseCase.getNotificationLogs(userId, page, size);

        List<NotificationLogResponse> content = result.content().stream()
                .map(NotificationLogResponse::from)
                .toList();

        PageMeta meta = new PageMeta(result.page(), result.size(), result.totalElements(), result.totalPages());

        return ResponseEntity.ok(ApiResponse.ok(content, meta));
    }
}
