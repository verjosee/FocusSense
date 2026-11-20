package com.focussense.app.domain.timer

import com.focussense.app.sensors.model.LightCategory

data class TimerState(
    val mode: TimerMode = TimerMode.IDLE,
    val targetDurationSeconds: Long = 25 * 60L,
    val elapsedSeconds: Long = 0L,
    val remainingSeconds: Long = 25 * 60L,
    val progress: Float = 0.0f,
    val formattedRemainingTime: String = "25:00",
    val movementCount: Int = 0,
    val averageLightLux: Float? = null,
    val currentLux: Float? = null,
    val currentLightCategory: LightCategory? = null,
    val isProximityNear: Boolean = false,
    val proximityDistance: Float? = null,
    val isProximitySupported: Boolean = false,
    val isLightSupported: Boolean = true,
    val isMotionAlertActive: Boolean = false,
    val sessionStartTime: Long = 0L
) {
    val isRunning: Boolean get() = mode == TimerMode.RUNNING
    val isPaused: Boolean get() = mode == TimerMode.PAUSED
    val isCompleted: Boolean get() = mode == TimerMode.COMPLETED
    val isIdle: Boolean get() = mode == TimerMode.IDLE

    val formattedElapsedTime: String
        get() = formatTime(elapsedSeconds)

    companion object {
        fun formatTime(seconds: Long): String {
            val minutes = seconds / 60
            val remainingSecs = seconds % 60
            return String.format("%02d:%02d", minutes, remainingSecs)
        }
    }
}
