package com.pyq.tcm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyq.tcm.common.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterceptorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void adminInterceptorAllowsAdmin() throws Exception {
        AdminInterceptor interceptor = new AdminInterceptor(objectMapper);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ai/admin/providers");
        request.setAttribute("role", "ADMIN");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void adminInterceptorRejectsNonAdmin() throws Exception {
        AdminInterceptor interceptor = new AdminInterceptor(objectMapper);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ai/admin/providers");
        request.setAttribute("role", "USER");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(403, response.getStatus());
        ApiResponse<?> body = objectMapper.readValue(response.getContentAsString(), ApiResponse.class);
        assertEquals(403, body.getCode());
    }

    @Test
    void adminInterceptorAllowsOptions() throws Exception {
        AdminInterceptor interceptor = new AdminInterceptor(objectMapper);
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/ai/admin/providers");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void rateLimitAllowsWithinLimitAndBlocksOverLimit() throws Exception {
        AiRateLimitInterceptor interceptor = new AiRateLimitInterceptor(objectMapper, 2, 60);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/ai/reviews");
        request.setAttribute("userId", 7L);

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));

        MockHttpServletResponse blocked = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(request, blocked, new Object()));
        assertEquals(429, blocked.getStatus());
    }

    @Test
    void rateLimitIsPerUser() throws Exception {
        AiRateLimitInterceptor interceptor = new AiRateLimitInterceptor(objectMapper, 1, 60);
        MockHttpServletRequest userA = new MockHttpServletRequest("POST", "/api/ai/reviews");
        userA.setAttribute("userId", 1L);
        MockHttpServletRequest userB = new MockHttpServletRequest("POST", "/api/ai/reviews");
        userB.setAttribute("userId", 2L);

        assertTrue(interceptor.preHandle(userA, new MockHttpServletResponse(), new Object()));
        assertFalse(interceptor.preHandle(userA, new MockHttpServletResponse(), new Object()));
        assertTrue(interceptor.preHandle(userB, new MockHttpServletResponse(), new Object()));
    }
}
