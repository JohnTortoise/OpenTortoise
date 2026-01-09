package io.github.johntortoise.config;

import io.github.johntortoise.interceptor.ApiKeyInterceptor;
import io.github.johntortoise.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private JwtInterceptor jwtInterceptor;

    @Resource
    private ApiKeyInterceptor apiKeyInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/api/admin/**")
                .order(0);

        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login")
                .excludePathPatterns("/api/auth/register")
                .excludePathPatterns("/api/admin/**")
                .excludePathPatterns("/api/defaultTool/**")
                .order(1);

    }
}