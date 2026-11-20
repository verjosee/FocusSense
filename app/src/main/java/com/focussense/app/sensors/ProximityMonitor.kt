package com.focussense.app.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.focussense.app.sensors.model.ProximityData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Monitors physical proximity using the hardware Proximity sensor (Sensor.TYPE_PROXIMITY).
 * Provides lifecycle-safe listener management, real distance readings, and near/far classification.
 */
class ProximityMonitor(
    context: Context,
    private val samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_UI
) : SensorEventListener {

    private val sensorManager =
        context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val proximitySensor: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    /**
     * True if the device hardware includes a physical proximity sensor.
     */
    val isSupported: Boolean = proximitySensor != null

    /**
     * Name of the hardware proximity sensor, or fallback if unavailable.
     */
    val sensorName: String = proximitySensor?.name ?: "Proximity Sensor"

    /**
     * Maximum reporting range of the sensor in centimeters (cm).
     */
    val maximumRange: Float = proximitySensor?.maximumRange ?: DEFAULT_MAX_RANGE

    private val _proximityData = MutableStateFlow(
        ProximityData(
            distance = maximumRange,
            maxRange = maximumRange,
            isNear = false
        )
    )
    val proximityData: StateFlow<ProximityData> = _proximityData.asStateFlow()

    private var isListening = false

    /**
     * Safely starts listening to proximity sensor updates if hardware is supported.
     */
    @Synchronized
    fun startListening() {
        if (!isSupported || isListening) return
        sensorManager?.registerListener(this, proximitySensor, samplingPeriodUs)
        isListening = true
    }

    /**
     * Stops listening to proximity sensor updates to conserve device battery.
     */
    @Synchronized
    fun stopListening() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_PROXIMITY) return

        val distance = event.values[0]
        val currentMaxRange = proximitySensor?.maximumRange ?: maximumRange
        val near = isNear(distance, currentMaxRange)

        _proximityData.value = ProximityData(
            distance = distance,
            maxRange = currentMaxRange,
            isNear = near,
            timestamp = System.currentTimeMillis()
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op for proximity sensor
    }

    companion object {
        /**
         * Standard fallback maximum range for proximity sensors in cm.
         */
        const val DEFAULT_MAX_RANGE = 5.0f

        /**
         * Pure classification function for proximity readings.
         * In Android Sensor.TYPE_PROXIMITY:
         * - If distance < maxRange, object is classified as NEAR.
         * - If distance >= maxRange, object is classified as FAR.
         * - If maxRange <= 0, distance <= 0 is classified as NEAR.
         */
        fun isNear(distance: Float, maxRange: Float = DEFAULT_MAX_RANGE): Boolean {
            return if (maxRange <= 0f) {
                distance <= 0f
            } else {
                distance < maxRange
            }
        }

        /**
         * Classifies proximity distance into semantic string ("Near" or "Far").
         */
        fun classifyStatus(distance: Float, maxRange: Float = DEFAULT_MAX_RANGE): String {
            return if (isNear(distance, maxRange)) "Near" else "Far"
        }
    }
}
