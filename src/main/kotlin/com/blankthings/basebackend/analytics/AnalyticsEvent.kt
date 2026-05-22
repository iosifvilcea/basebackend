package com.blankthings.basebackend.analytics

enum class AnalyticsEvent {
    USER_CREATED,
    USER_DELETED,
    USER_UPDATED,
    USER_FETCHED,

    AUTH_ERROR,

    AUTH_SUCCESSFUL,
    AUTH_FAILED,

    EMAIL_SENT,
    EMAIL_FAILED,

    LOGOUT_SUCCESSFUL,
    LOGOUT_FAILED,
}
