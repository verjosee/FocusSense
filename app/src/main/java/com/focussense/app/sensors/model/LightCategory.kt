package com.focussense.app.sensors.model

/**
 * Ambient light environment categories based on standard ergonomic study thresholds:
 * - VERY_DIM: < 50 lux (Eye strain hazard for reading/studying)
 * - DIM: 50 - 200 lux (Sub-optimal, ambient low light)
 * - MODERATE: 200 - 500 lux (Recommended standard study/office lighting)
 * - BRIGHT: > 500 lux (Very bright/direct sunlight or high illumination)
 */
enum class LightCategory {
    VERY_DIM,
    DIM,
    MODERATE,
    BRIGHT;

    companion object {
        /**
         * Classifies raw illuminance into a semantic [LightCategory].
         *
         * @param lux Raw illuminance reading from ambient light sensor in lux (lx).
         */
        fun fromLux(lux: Float): LightCategory {
            return when {
                lux < 50f -> VERY_DIM
                lux <= 200f -> DIM
                lux <= 500f -> MODERATE
                else -> BRIGHT
            }
        }
    }
}
