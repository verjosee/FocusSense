package com.focussense.app.sensors.model

/**
 * Represents ambient light sensor readings and categorized illumination level.
 *
 * @property lux Raw illuminance reading in lux (lx)
 * @property category Categorized environment illumination
 * @property timestamp Epoch timestamp in milliseconds when reading was recorded
 */
data class LightData(
    val lux: Float = 0f,
    val category: LightCategory = LightCategory.VERY_DIM,
    val timestamp: Long = System.currentTimeMillis()
)
