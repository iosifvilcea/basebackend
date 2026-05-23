package com.blankthings.basebackend.user

import com.blankthings.basebackend.analytics.AnalyticsTracker
import com.blankthings.basebackend.auth.ACCESS_TOKEN
import com.blankthings.basebackend.auth.CookieManager
import com.blankthings.basebackend.auth.JwtService
import com.blankthings.basebackend.auth.REFRESH_TOKEN
import com.blankthings.basebackend.ratelimit.RateLimiter
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseCookie
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(UserController::class)
@TestPropertySource(properties = ["app.url=http://localhost"])
class UserControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var userService: UserService

    @MockitoBean
    private lateinit var cookieManager: CookieManager

    @MockitoBean
    private lateinit var jwtService: JwtService

    @MockitoBean
    private lateinit var userRepository: UserRepository

    @MockitoBean
    private lateinit var analyticsTracker: AnalyticsTracker

    @MockitoBean
    private lateinit var rateLimiter: RateLimiter

    @BeforeEach
    fun setUp() {
        given(rateLimiter.isAllowed(anyString())).willReturn(true)
    }

    private fun stubAuthCookies() {
        given(cookieManager.accessCookie(anyString())).willReturn(
            ResponseCookie.from(ACCESS_TOKEN, "access-token").build(),
        )
        given(cookieManager.refreshCookie(anyString())).willReturn(
            ResponseCookie.from(REFRESH_TOKEN, "refresh-token").build(),
        )
    }

    // --- POST /api/auth ---

    @Test
    fun `POST login returns 429 when rate limit is exceeded`() {
        given(rateLimiter.isAllowed(anyString())).willReturn(false)
        mockMvc
            .post("/api/auth") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"email": "user@example.com"}"""
            }.andExpect {
                status { isTooManyRequests() }
            }
    }

    @Test
    fun `POST login returns 200 with success message`() {
        mockMvc
            .post("/api/auth") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"email": "user@example.com"}"""
            }.andExpect {
                status { isOk() }
                jsonPath("$.successMessage") { exists() }
            }
    }

    @Test
    fun `POST login returns 400 when request body is missing`() {
        mockMvc
            .post("/api/auth") {
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
            }
    }

    // --- GET /api/auth?token=... ---

    @Test
    fun `GET authenticate returns 302 with cookies and redirects when token is valid`() {
        given(userService.authenticate("validtoken")).willReturn(Session.Data("access", "refresh"))
        stubAuthCookies()

        mockMvc
            .get("/api/auth") {
                param("token", "validtoken")
            }.andExpect {
                status { isFound() }
                header { exists(HttpHeaders.SET_COOKIE) }
                header { string(HttpHeaders.LOCATION, "http://localhost") }
            }
    }

    @Test
    fun `GET authenticate returns 401 when token is invalid`() {
        given(userService.authenticate("badtoken")).willReturn(Session.None)

        mockMvc
            .get("/api/auth") {
                param("token", "badtoken")
            }.andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun `GET authenticate returns 400 when token param is missing`() {
        mockMvc.get("/api/auth").andExpect {
            status { isBadRequest() }
        }
    }

    // --- POST /api/auth/refresh ---

    @Test
    fun `POST refresh returns 401 when refresh cookie is absent`() {
        mockMvc.post("/api/auth/refresh").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `POST refresh returns 200 and sets cookies when refresh token is valid`() {
        given(userService.refreshSession("rawrefresh")).willReturn(Session.Data("access", "refresh"))
        stubAuthCookies()

        mockMvc
            .post("/api/auth/refresh") {
                cookie(Cookie(REFRESH_TOKEN, "rawrefresh"))
            }.andExpect {
                status { isOk() }
                header { exists(HttpHeaders.SET_COOKIE) }
            }
    }

    @Test
    fun `POST refresh returns 401 when refresh token is invalid`() {
        given(userService.refreshSession("badrefresh")).willReturn(Session.None)

        mockMvc
            .post("/api/auth/refresh") {
                cookie(Cookie(REFRESH_TOKEN, "badrefresh"))
            }.andExpect {
                status { isUnauthorized() }
            }
    }
}
