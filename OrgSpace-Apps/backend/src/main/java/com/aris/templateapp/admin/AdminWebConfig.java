package com.aris.templateapp.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Memasang {@link AdminGuard} di depan semua endpoint admin. */
@Configuration
@RequiredArgsConstructor
public class AdminWebConfig implements WebMvcConfigurer {

    public static final String ADMIN_PATHS = "/api/admin/**";

    private final AdminGuard adminGuard;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminGuard).addPathPatterns(ADMIN_PATHS);
    }
}
