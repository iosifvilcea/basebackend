package com.blankthings.basebackend.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:Email @field:NotBlank val email: String,
)
