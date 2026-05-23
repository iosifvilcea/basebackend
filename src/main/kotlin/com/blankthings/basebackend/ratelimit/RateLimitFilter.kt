package com.blankthings.basebackend.ratelimit

import com.blankthings.basebackend.auth.AUTH_URL_PATH
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RateLimitFilter(private val rateLimiter: RateLimiter) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        if (request.method == "POST" && request.requestURI == AUTH_URL_PATH) {
            if (!rateLimiter.isAllowed(clientIp(request))) {
                response.status = HttpStatus.TOO_MANY_REQUESTS.value()
                response.contentType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
                response.writer.write(
                    """{"type":"about:blank","title":"Too Many Requests","status":429,"detail":"Too many login requests. Please try again later."}""",
                )
                return
            }
        }
        chain.doFilter(request, response)
    }

    private fun clientIp(request: HttpServletRequest): String =
        request.getHeader("X-Forwarded-For")?.split(",")?.first()?.trim()
            ?: request.remoteAddr
}
