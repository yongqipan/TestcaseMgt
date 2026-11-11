package com.pyq.tcm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyq.tcm.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiRateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Deque<Long>> windows = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final int maxRequests;
    private final long windowMillis;

    public AiRateLimitInterceptor(ObjectMapper objectMapper,
                                  @Value("${tcm.ai.rate-limit-max-requests:10}") int maxRequests,
                                  @Value("${tcm.ai.rate-limit-window-seconds:60}") long windowSeconds) {
        this.objectMapper = objectMapper;
        this.maxRequests = maxRequests;
        this.windowMillis = windowSeconds * 1000L;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        Object userId = request.getAttribute("userId");
        String key = userId == null ? request.getRemoteAddr() : String.valueOf(userId);
        long now = System.currentTimeMillis();
        Deque<Long> window = windows.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (window) {
            while (!window.isEmpty() && now - window.peekFirst() > windowMillis) {
                window.pollFirst();
            }
            if (window.size() >= maxRequests) {
                response.setStatus(429);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(
                        ApiResponse.error(429, "AI 调用过于频繁，请稍后再试")));
                return false;
            }
            window.addLast(now);
        }
        return true;
    }
}
