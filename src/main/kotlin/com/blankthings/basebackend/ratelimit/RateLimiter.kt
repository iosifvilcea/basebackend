package com.blankthings.basebackend.ratelimit

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BandwidthBuilder
import io.github.bucket4j.Bucket
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

class RateLimiter(maxRequests: Long, windowSeconds: Long) {
    private val bandwidth: Bandwidth =
        BandwidthBuilder
            .builder()
            .capacity(maxRequests)
            .refillGreedy(maxRequests, Duration.ofSeconds(windowSeconds))
            .build()

    private val buckets = ConcurrentHashMap<String, Bucket>()

    fun isAllowed(key: String): Boolean =
        buckets
            .computeIfAbsent(key) { Bucket.builder().addLimit(bandwidth).build() }
            .tryConsume(1)
}
