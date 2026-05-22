package com.blankthings.basebackend.user.dto

data class LoginResponse(
    val successMessage: String =
        """
        Your login link has been sent!
        Please check your email to login.
        If you can't find the login link in your email, be sure to check your spam folder.
        """.trimIndent(),
)
