package com.serviloc.notifications.application.service;

import com.serviloc.notifications.application.dto.DeviceTokenResult;
import com.serviloc.notifications.application.dto.RegisterDeviceTokenCommand;
import com.serviloc.notifications.application.port.in.RegisterDeviceTokenUseCase;
import com.serviloc.notifications.domain.model.DeviceToken;
import com.serviloc.notifications.domain.repository.DeviceTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class DeviceTokenService implements RegisterDeviceTokenUseCase {

    private final DeviceTokenRepository deviceTokenRepository;

    public DeviceTokenService(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Override
    public DeviceTokenResult registerDeviceToken(RegisterDeviceTokenCommand command) {
        Optional<DeviceToken> existing = deviceTokenRepository
                .findByPlatformAndToken(command.platform(), command.token());

        DeviceToken deviceToken;
        if (existing.isPresent()) {
            deviceToken = existing.get();
            if (!deviceToken.getUserId().equals(command.userId())) {
                // Le token physique était associé à un autre utilisateur (changement de compte sur l'appareil).
                deviceToken.reassignTo(command.userId());
            } else {
                deviceToken.touch();
            }
        } else {
            deviceToken = DeviceToken.register(command.userId(), command.platform(), command.token());
        }

        DeviceToken saved = deviceTokenRepository.save(deviceToken);
        return new DeviceTokenResult(saved.getId(), saved.getUserId(), saved.getPlatform(),
                saved.getToken(), saved.getUpdatedAt());
    }
}
