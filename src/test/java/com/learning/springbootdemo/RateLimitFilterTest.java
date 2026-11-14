package com.learning.springbootdemo;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitFilterTest {

    private final AtomicLong now = new AtomicLong(1_000_000_000L);

    private MockHttpServletResponse call(RateLimitFilter filter, String client) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/software-engineers");
        req.setRemoteAddr(client);
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, new MockFilterChain());
        return res;
    }

    @Test
    void burstIsAllowedThenThrottledWithRetryAfter() throws Exception {
        RateLimitFilter f = new RateLimitFilter(true, 10, 5, now::get);
        for (int i = 0; i < 5; i++) {
            assertEquals(200, call(f, "10.0.0.1").getStatus());
        }
        MockHttpServletResponse limited = call(f, "10.0.0.1");
        assertEquals(429, limited.getStatus());
        assertEquals("1", limited.getHeader("Retry-After"));
        assertTrue(limited.getContentAsString().contains("Too Many Requests"));
    }

    @Test
    void tokensComeBackOverTime() throws Exception {
        RateLimitFilter f = new RateLimitFilter(true, 10, 1, now::get);
        assertEquals(200, call(f, "a").getStatus());
        assertEquals(429, call(f, "a").getStatus());
        now.addAndGet(100_000_000L); // 0.1 s at 10/s = one token
        assertEquals(200, call(f, "a").getStatus());
    }

    @Test
    void clientsAreCountedSeparately() throws Exception {
        RateLimitFilter f = new RateLimitFilter(true, 1, 1, now::get);
        assertEquals(200, call(f, "a").getStatus());
        assertEquals(429, call(f, "a").getStatus());
        assertEquals(200, call(f, "b").getStatus());
    }

    @Test
    void forwardedForHeaderCannotBypassTheLimit() throws Exception {
        RateLimitFilter f = new RateLimitFilter(true, 1, 1, now::get);
        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/x");
            req.setRemoteAddr("10.0.0.9");
            req.addHeader("X-Forwarded-For", "203.0.113." + i);
            MockHttpServletResponse res = new MockHttpServletResponse();
            f.doFilter(req, res, new MockFilterChain());
            assertEquals(i == 0 ? 200 : 429, res.getStatus());
        }
    }

    @Test
    void disabledFilterNeverThrottles() throws Exception {
        RateLimitFilter f = new RateLimitFilter(false, 1, 1, now::get);
        for (int i = 0; i < 20; i++) {
            assertEquals(200, call(f, "a").getStatus());
        }
    }

    @Test
    void invalidSettingsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new RateLimitFilter(true, 0, 1, now::get));
        assertThrows(IllegalArgumentException.class, () -> new RateLimitFilter(true, 1, 0, now::get));
    }
}
