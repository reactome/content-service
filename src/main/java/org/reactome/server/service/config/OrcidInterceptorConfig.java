package org.reactome.server.service.config;

import org.reactome.server.orcid.controller.OrcidServerInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class OrcidInterceptorConfig implements WebMvcConfigurer {

    private final OrcidServerInterceptor serverInterceptor;

    public OrcidInterceptorConfig(OrcidServerInterceptor serverInterceptor) {
        this.serverInterceptor = serverInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(serverInterceptor).addPathPatterns("/orcid/**");
    }
}
