// infrastructure/config/WebConfig.java
package com.serviloc.mission.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final InboundInternalTokenInterceptor inboundInternalTokenInterceptor;

    public WebConfig(InboundInternalTokenInterceptor inboundInternalTokenInterceptor) {
        this.inboundInternalTokenInterceptor = inboundInternalTokenInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(inboundInternalTokenInterceptor)
                .addPathPatterns("/internal/**");
    }
}