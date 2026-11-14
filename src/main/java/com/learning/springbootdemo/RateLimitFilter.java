package com.learning.springbootdemo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * Per-client token bucket. A client holds up to {@code burst} tokens and earns {@code requestsPerSecond} tokens per
 * second; each request costs one. Without a token the answer is 429 with a Retry-After header. The client is the TCP
 * peer address: X-Forwarded-For is deliberately ignored because any caller can forge it.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1) // right after SecurityHeadersFilter, so 429 responses carry its headers too
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_CLIENTS = 10_000;
    private static final long IDLE_NANOS = 60_000_000_000L;

    private final boolean enabled;
    private final double refillPerNano;
    private final double burst;
    private final LongSupplier nanoClock;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Autowired
    public RateLimitFilter(@Value("${app.rate-limit.enabled:true}") boolean enabled,
                           @Value("${app.rate-limit.requests-per-second:50}") double requestsPerSecond,
                           @Value("${app.rate-limit.burst:10}") int burst) {
        this(enabled, requestsPerSecond, burst, System::nanoTime);
    }

    RateLimitFilter(boolean enabled, double requestsPerSecond, int burst, LongSupplier nanoClock) {
        if (requestsPerSecond <= 0 || burst < 1) {
            throw new IllegalArgumentException("app.rate-limit.requests-per-second must be > 0 and burst >= 1");
        }
        this.enabled = enabled;
        this.refillPerNano = requestsPerSecond / 1_000_000_000.0;
        this.burst = burst;
        this.nanoClock = nanoClock;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!enabled || tryAcquire(request.getRemoteAddr())) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(429);
        response.setHeader("Retry-After", "1");
        response.setContentType("application/problem+json");
        response.getWriter().write("{\"status\":429,\"title\":\"Too Many Requests\",\"detail\":\"Rate limit exceeded\"}");
    }

    boolean tryAcquire(String client) {
        long now = nanoClock.getAsLong();
        if (buckets.size() >= MAX_CLIENTS) {
            buckets.values().removeIf(b -> now - b.lastNanos > IDLE_NANOS);
            if (buckets.size() >= MAX_CLIENTS) {
                buckets.clear(); // bounded memory beats perfect accounting under an address-spraying flood
            }
        }
        Bucket b = buckets.computeIfAbsent(client, k -> new Bucket(burst, now));
        synchronized (b) {
            b.tokens = Math.min(burst, b.tokens + (now - b.lastNanos) * refillPerNano);
            b.lastNanos = now;
            if (b.tokens >= 1) {
                b.tokens -= 1;
                return true;
            }
            return false;
        }
    }

    private static final class Bucket {
        double tokens;
        long lastNanos;

        Bucket(double tokens, long lastNanos) {
            this.tokens = tokens;
            this.lastNanos = lastNanos;
        }
    }
}
