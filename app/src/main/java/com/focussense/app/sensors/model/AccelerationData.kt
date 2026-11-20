package com.focussense.app.sensors.model

/**
 * Represents 3-axis accelerometer readings and calculated motion status.
 *
 * @property x Acceleration along X-axis in m/s² (lateral tilt/motion)
 * @property y Acceleration along Y-axis in m/s² (longitudinal tilt/motion)
 * @property z Acceleration along Z-axis in m/s² (vertical tilt/gravity)
 * @property magnitude Net acceleration vector magnitude in m/s² (sqrt(x² + y² + z²))
 * @property isSignificantMovement True if delta acceleration between consecutive readings exceeds movement threshold
 * @property timestamp Epoch timestamp in milliseconds when reading was recorded
 */
data class AccelerationData(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val magnitude: Float = 0f,
    val isSignificantMovement: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
