package io.github.johntortoise.interceptor;

import io.github.johntortoise.enums.EnabelEnum;
import io.github.johntortoise.model.TortoiseChatProfile;
import io.github.johntortoise.service.TortoiseChatProfileService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    @Resource
    private TortoiseChatProfileService tortoiseChatProfileService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String apiKey = request.getHeader("X-API-KEY");

        
        response.setContentType("application/json;charset=UTF-8");

        if (apiKey == null || apiKey.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"未提供认证令牌\"}");
            return false;
        }
        TortoiseChatProfile tortoiseChatProfile = tortoiseChatProfileService.findBySK(apiKey);
        if(Objects.isNull(tortoiseChatProfile)){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"令牌不存在\"}");
            return false;
        }
        if(tortoiseChatProfile.getStatus().equals(EnabelEnum.CLOSE.getCode())){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"SK未启用\"}");
            return false;
        }
        return true;
    }
}