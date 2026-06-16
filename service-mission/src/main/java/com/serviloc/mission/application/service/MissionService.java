// application/service/MissionService.java
package com.serviloc.mission.application.service;

import com.serviloc.mission.application.dto.request.LocationDto;
import com.serviloc.mission.application.dto.response.MissionResponse;
import com.serviloc.mission.application.port.in.MissionUseCase;
import com.serviloc.mission.domain.exception.MissionNotFoundException;
import com.serviloc.mission.domain.exception.UnauthorizedMissionAccessException;
import com.serviloc.mission.domain.model.Mission;
import com.serviloc.mission.domain.repository.MissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MissionService implements MissionUseCase {

    private final MissionRepository missionRepository;

    public MissionService(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public MissionResponse getMissionById(String id, String userId, String role) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException(id));

        if (role.equals("client") && !mission.getClientId().equals(userId)) {
            throw new UnauthorizedMissionAccessException(userId, id);
        }
        if (role.equals("provider") && !mission.getProviderId().equals(userId)) {
            throw new UnauthorizedMissionAccessException(userId, id);
        }

        return toResponse(mission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissionResponse> getMissionsByProvider(String providerId) {
        return missionRepository.findByProviderId(providerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissionResponse> getMissionsByClient(String clientId) {
        return missionRepository.findByClientId(clientId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private MissionResponse toResponse(Mission mission) {
        MissionResponse response = new MissionResponse();
        response.setId(mission.getId());
        response.setDemandId(mission.getDemandId());
        response.setQuoteId(mission.getQuoteId());
        response.setClientId(mission.getClientId());
        response.setProviderId(mission.getProviderId());
        response.setCategory(mission.getCategory());
        response.setStatus(mission.getStatus().name());
        response.setTotalAmount(mission.getTotalAmount());
        response.setSequesteredAmount(mission.getSequesteredAmount());
        response.setPaymentStatus(mission.getPaymentStatus());
        response.setStartedAt(mission.getStartedAt());
        response.setEstimatedDurationHours(mission.getEstimatedDurationHours());
        response.setCompletedAt(mission.getCompletedAt());

        if (mission.getLocation() != null) {
            LocationDto loc = new LocationDto();
            loc.setLat(mission.getLocation().lat());
            loc.setLng(mission.getLocation().lng());
            loc.setAddress(mission.getLocation().address());
            response.setLocation(loc);
        }

        return response;
    }
}