// adapter/rest/AdminInternalController.java
package com.serviloc.mission.adapter.rest;

import com.serviloc.mission.application.dto.response.AdminStatsResponse;
import com.serviloc.mission.application.dto.response.ApiResponse;
import com.serviloc.mission.application.dto.response.DashboardAdminResponse;
import com.serviloc.mission.application.service.AdminStatsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@Tag(name = "Internal", description = "Endpoints inter-services — non exposés au Gateway côté utilisateurs finaux")
public class AdminInternalController {

    private final AdminStatsService adminStatsService;

    public AdminInternalController(AdminStatsService adminStatsService) {
        this.adminStatsService = adminStatsService;
    }

    @GetMapping("/dashboard/missions")
    public ResponseEntity<ApiResponse<DashboardAdminResponse>> getMissionsDashboardSlice() {
        return ResponseEntity.ok(ApiResponse.success(adminStatsService.getAdminDashboard()));
    }

    @GetMapping("/stats/missions")
    public ResponseEntity<ApiResponse<AdminStatsResponse.MissionsStats>> getMissionsStatsSlice() {
        return ResponseEntity.ok(ApiResponse.success(adminStatsService.getMissionsStatsSlice()));
    }
}