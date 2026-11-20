package com.focussense.app.sensors.model

/**
 * Represents physical proximity sensor readings and classified near/far condition.
 *
 * @property distance Measured distance in centimeters (cm).
 * @property maxRange Maximum measurement range of the proximity hardware in centimeters (cm).
 * @property isNear True if an obstruction is within near threshold (< maxRange).
 * @property timestamp Epoch timestamp in milliseconds when reading was recorded.
 */
data class ProximityData(
    val distance: Float = 0f,
    val maxRange: Float = 5f,
    val isNear: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * True if the object/hand is far from the proximity sensor.
     */
    val isFar: Boolean
        get() = !isNear

    /**
     * Human-readable label for proximity state: "Near" or "Far".
     */
    val statusText: String
        get() = if (isNear) "Near" else "Far"
}
