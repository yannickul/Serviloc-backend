// adapter/rest/AdminMissionController.java
package com.serviloc.mission.adapter.rest;

import com.serviloc.mission.application.dto.response.ApiResponse;
import com.serviloc.mission.application.dto.response.DashboardAdminResponse;
import com.serviloc.mission.application.dto.response.DemandResponse;
import com.serviloc.mission.application.dto.response.PagedResponse;
import com.serviloc.mission.application.service.AdminStatsService;
import com.serviloc.mission.domain.exception.UnauthorizedMissionAccessException;
import com.serviloc.mission.domain.model.DemandStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminMissionController {

    private final AdminStatsService adminStatsService;

    public AdminMissionController(AdminStatsService adminStatsService) {
        this.adminStatsService = adminStatsService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardAdminResponse>> getStats(
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("admin")) {
            throw new UnauthorizedMissionAccessException("non-admin", "stats");
        }

        return ResponseEntity.ok(
                ApiResponse.success(adminStatsService.getAdminDashboard()));
    }

    @GetMapping("/demands")
    public ResponseEntity<PagedResponse<DemandResponse>> getAllDemands(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String status) {

        if (!role.equals("admin")) {
            throw new UnauthorizedMissionAccessException("non-admin", "demands");
        }

        DemandStatus demandStatus = status != null
                ? DemandStatus.valueOf(status.toUpperCase()) : null;

        return ResponseEntity.ok(
                adminStatsService.getAllDemands(demandStatus, page, limit));
    }
}