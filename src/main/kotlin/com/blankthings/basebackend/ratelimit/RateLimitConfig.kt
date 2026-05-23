package com.blankthings.basebackend.ratelimit

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RateLimitConfig {
    @Bean
    fun rateLimiter(
        @Value("\${rate-limit.auth.max-requests:5}") maxRequests: Long,
        @Value("\${rate-limit.auth.window-seconds:900}") windowSeconds: Long,
    ): RateLimiter = RateLimiter(maxRequests, windowSeconds)
}
