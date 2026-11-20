package com.focussense.app.sensors

import android.content.Context
import com.focussense.app.sensors.model.AccelerationData
import com.focussense.app.sensors.model.LightData
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface providing reactive streams and lifecycle controls for device sensors.
 */
interface SensorRepository {
    /**
     * StateFlow emitting real-time 3-axis accelerometer readings and motion detection state.
     */
    val accelerometerFlow: StateFlow<AccelerationData>

    /**
     * StateFlow emitting real-time ambient light readings and classified ergonomic category.
     */
    val lightFlow: StateFlow<LightData>

    /**
     * True if the device hardware supports an accelerometer sensor.
     */
    val isAccelerometerAvailable: Boolean

    /**
     * True if the device hardware supports an ambient light sensor.
     */
    val isLightSensorAvailable: Boolean

    /**
     * Starts monitoring hardware sensors. Reference-counted to support multiple screens/consumers.
     */
    fun startListening()

    /**
     * Stops monitoring hardware sensors when no longer observed, preventing battery drain.
     */
    fun stopListening()

    /**
     * Dynamically adjusts movement threshold in m/s² (default: 1.8 m/s²).
     */
    fun setMovementThreshold(threshold: Float)

    companion object {
        /**
         * Factory function to create standard implementation.
         */
        fun create(context: Context): SensorRepository = DefaultSensorRepository(context)

        /**
         * Allows constructor-style syntax: `SensorRepository(context)`.
         */
        operator fun invoke(context: Context): SensorRepository = DefaultSensorRepository(context)
    }
}
