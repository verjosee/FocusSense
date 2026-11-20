package com.focussense.app.sensors.model

/**
 * Identifies which secondary environmental sensor is currently active on the device.
 * FocusSense uses Accelerometer as primary motion sensor.
 * Secondary sensor selection follows priority:
 * 1. [LIGHT] - Ambient Light Sensor (preferred for study ergonomics)
 * 2. [PROXIMITY] - Physical Proximity Sensor (fallback on devices like Galaxy M12 without Light sensor)
 * 3. [NONE] - Fallback when neither secondary hardware sensor is available
 */
enum class SecondarySensorType {
    LIGHT,
    PROXIMITY,
    NONE
}
