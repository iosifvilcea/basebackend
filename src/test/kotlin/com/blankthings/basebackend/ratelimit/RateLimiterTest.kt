package com.blankthings.basebackend.ratelimit

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RateLimiterTest {
    private val rateLimiter = RateLimiter(maxRequests = 3, windowSeconds = 60)

    @Test
    fun `allows requests within limit`() {
        repeat(3) { assertTrue(rateLimiter.isAllowed("1.2.3.4")) }
    }

    @Test
    fun `blocks request that exceeds limit`() {
        repeat(3) { rateLimiter.isAllowed("1.2.3.4") }
        assertFalse(rateLimiter.isAllowed("1.2.3.4"))
    }

    @Test
    fun `tracks different IPs independently`() {
        repeat(3) { rateLimiter.isAllowed("1.2.3.4") }
        assertTrue(rateLimiter.isAllowed("5.6.7.8"))
    }
}
