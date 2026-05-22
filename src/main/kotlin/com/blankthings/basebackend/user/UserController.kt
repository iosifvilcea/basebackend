package com.blankthings.basebackend.user

import com.blankthings.basebackend.auth.AUTH_URL_PATH
import com.blankthings.basebackend.auth.CookieManager
import com.blankthings.basebackend.auth.REFRESH_TOKEN
import com.blankthings.basebackend.user.dto.LoginRequest
import com.blankthings.basebackend.user.dto.LoginResponse
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping(AUTH_URL_PATH)
class UserController(
    private val userService: UserService,
    private val cookieManager: CookieManager,
    @Value("\${app.url}") private val appUrl: String,
) {
    @PostMapping
    fun login(
        @RequestBody @Valid loginRequest: LoginRequest,
    ): ResponseEntity<LoginResponse> {
        userService.processLogin(loginRequest.email)
        return ResponseEntity.ok(LoginResponse())
    }

    @GetMapping
    fun authenticate(
        @RequestParam token: String,
    ): ResponseEntity<Void> =
        when (val result = userService.authenticate(token)) {
            Session.None -> {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
            }

            is Session.Data -> {
                ResponseEntity
                    .status(HttpStatus.FOUND)
                    .location(URI.create(appUrl))
                    .header(HttpHeaders.SET_COOKIE, cookieManager.accessCookie(result.accessToken).toString())
                    .header(HttpHeaders.SET_COOKIE, cookieManager.refreshCookie(result.refreshToken).toString())
                    .build()
            }
        }

    @PostMapping("/refresh")
    fun refresh(
        @CookieValue(REFRESH_TOKEN, required = false) rawRefreshToken: String?,
    ): ResponseEntity<Void> =
        when (rawRefreshToken) {
            null -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
            else -> buildRefreshResponse(userService.refreshSession(rawRefreshToken))
        }

    private fun buildRefreshResponse(result: Session): ResponseEntity<Void> =
        when (result) {
            Session.None -> {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
            }

            is Session.Data -> {
                ResponseEntity
                    .ok()
                    .header(HttpHeaders.SET_COOKIE, cookieManager.accessCookie(result.accessToken).toString())
                    .header(HttpHeaders.SET_COOKIE, cookieManager.refreshCookie(result.refreshToken).toString())
                    .build()
            }
        }
}
