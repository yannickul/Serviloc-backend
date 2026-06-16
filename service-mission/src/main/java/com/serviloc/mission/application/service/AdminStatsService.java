// application/service/AdminStatsService.java
package com.serviloc.mission.application.service;

import com.serviloc.mission.application.dto.response.DashboardAdminResponse;
import com.serviloc.mission.application.dto.response.DemandResponse;
import com.serviloc.mission.application.dto.response.PagedResponse;
import com.serviloc.mission.domain.model.DemandStatus;
import com.serviloc.mission.domain.model.MissionStatus;
import com.serviloc.mission.domain.repository.DemandRepository;
import com.serviloc.mission.domain.repository.MissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminStatsService {

    private final DemandRepository demandRepository;
    private final MissionRepository missionRepository;
    private final DemandService demandService;

    public AdminStatsService(
            DemandRepository demandRepository,
            MissionRepository missionRepository,
            DemandService demandService) {
        this.demandRepository = demandRepository;
        this.missionRepository = missionRepository;
        this.demandService = demandService;
    }

    public DashboardAdminResponse getAdminDashboard() {
        DashboardAdminResponse response = new DashboardAdminResponse();

        long total = demandRepository.countAll();
        long open = demandRepository.countByStatus(DemandStatus.OUVERTE);
        long inProgress = demandRepository.countByStatus(DemandStatus.EN_COURS);
        long completed = demandRepository.countByStatus(DemandStatus.TERMINEE);
        long cancelled = demandRepository.countByStatus(DemandStatus.ANNULEE);

        response.setTotalDemands(total);
        response.setOpenDemands(open);
        response.setInProgressDemands(inProgress);
        response.setCompletedDemands(completed);
        response.setCancelledDemands(cancelled);

        long totalMissions = missionRepository.countAll();
        long completedMissions = missionRepository.countByStatus(MissionStatus.TERMINEE);
        long disputedMissions = missionRepository.countByStatus(MissionStatus.LITIGE);

        response.setTotalMissions(totalMissions);
        response.setCompletedMissions(completedMissions);
        response.setDisputedMissions(disputedMissions);

        double completionRate = totalMissions > 0
                ? (double) completedMissions / totalMissions * 100
                : 0;
        response.setCompletionRate(Math.round(completionRate * 10.0) / 10.0);

        return response;
    }

    public PagedResponse<DemandResponse> getAllDemands(
            DemandStatus status, int page, int limit) {
        return demandService.getAllDemands(status, page, limit);
    }
}