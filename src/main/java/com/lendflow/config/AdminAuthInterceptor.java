package com.lendflow.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        if (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/") 
                || uri.startsWith("/webjars/") || uri.equals("/login") 
                || uri.startsWith("/api/auth/") || uri.equals("/error")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("admin") != null) {
            return true;
        }

        response.sendRedirect("/login");
        return false;
    }
}

