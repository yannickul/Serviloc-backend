// infrastructure/config/InternalTokenInterceptor.java
package com.serviloc.mission.infrastructure.config;

import com.serviloc.mission.domain.exception.InvalidInternalTokenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class InboundInternalTokenInterceptor implements HandlerInterceptor {

    private final String expectedToken;

    public InboundInternalTokenInterceptor(@Value("${serviloc.internal-token}") String expectedToken) {
        this.expectedToken = expectedToken;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("X-Internal-Token");
        if (token == null || !MessageDigest.isEqual(
                token.getBytes(StandardCharsets.UTF_8),
                expectedToken.getBytes(StandardCharsets.UTF_8))) {
            throw new InvalidInternalTokenException();
        }
        return true;
    }
}