package com.focussense.app.domain.model

enum class SessionStatus {
    COMPLETED,
    INTERRUPTED,
    CANCELLED;

    companion object {
        fun fromString(value: String): SessionStatus {
            return when (value.uppercase()) {
                "COMPLETED" -> COMPLETED
                "INTERRUPTED" -> INTERRUPTED
                "CANCELLED" -> CANCELLED
                else -> INTERRUPTED
            }
        }
    }
}
