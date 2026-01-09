package io.github.johntortoise.interceptor;

import io.github.johntortoise.context.TortoiseContext;
import io.github.johntortoise.utils.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        String requestURI = request.getRequestURI();

        
        boolean isPageRequest = !requestURI.startsWith("/api/");
        
        
        if (isPageRequest) {
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("user") != null) {
                
                Long userId = (Long) session.getAttribute("userId");
                if (userId != null) {
                    TortoiseContext.setCurrentUserId(userId);
                }
                return true;
            }
            
            response.sendRedirect("/login");
            return false;
        }
        

        String token = request.getHeader("Authorization");

        // 如果header中没有token，尝试从URL参数获取（用于SSE连接）
        if (token == null) {
            token = request.getParameter("token");
            if (token != null && !token.startsWith("Bearer ")) {
                token = "Bearer " + token;
            }
        }

        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"未提供有效的认证令牌\"}");
            return false;
        }


        token = token.substring(7);
        
        
        if (!JwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"令牌无效或已过期\"}");
            return false;
        }
        
        
        Long userId = JwtUtil.getUserIdFromToken(token);
        request.setAttribute("userId", userId);
        request.setAttribute("email", JwtUtil.getEmailFromToken(token));
        request.setAttribute("roleCode", JwtUtil.getRoleCodeFromToken(token));
        
        
        TortoiseContext.setCurrentUserId(userId);
        
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        TortoiseContext.clear();
    }
}