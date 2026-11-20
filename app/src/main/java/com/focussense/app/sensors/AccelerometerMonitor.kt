package com.focussense.app.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.focussense.app.sensors.model.AccelerationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

/**
 * Monitors device motion and vibration using the hardware Accelerometer.
 * Detects significant movement events using 3D delta acceleration thresholding.
 */
class AccelerometerMonitor(
    context: Context,
    var movementThreshold: Float = DEFAULT_MOVEMENT_THRESHOLD,
    private val samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_UI
) : SensorEventListener {

    private val sensorManager =
        context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val accelerometer: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    /**
     * True if the device hardware includes an accelerometer sensor.
     */
    val isSupported: Boolean = accelerometer != null

    private val _accelerationData = MutableStateFlow(AccelerationData())
    val accelerationData: StateFlow<AccelerationData> = _accelerationData.asStateFlow()

    private var isListening = false
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var hasFirstSample = false

    /**
     * Safely starts listening to accelerometer updates if hardware is supported.
     */
    @Synchronized
    fun startListening() {
        if (!isSupported || isListening) return
        sensorManager?.registerListener(this, accelerometer, samplingPeriodUs)
        isListening = true
    }

    /**
     * Stops listening to sensor updates to conserve device battery.
     */
    @Synchronized
    fun stopListening() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
        hasFirstSample = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt(x * x + y * y + z * z)

        val isSignificantMovement: Boolean
        if (hasFirstSample) {
            val deltaMagnitude = calculateDeltaMagnitude(lastX, lastY, lastZ, x, y, z)
            isSignificantMovement = isMovementSignificant(deltaMagnitude, movementThreshold)
        } else {
            hasFirstSample = true
            isSignificantMovement = false
        }

        lastX = x
        lastY = y
        lastZ = z

        _accelerationData.value = AccelerationData(
            x = x,
            y = y,
            z = z,
            magnitude = magnitude,
            isSignificantMovement = isSignificantMovement,
            timestamp = System.currentTimeMillis()
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op for accelerometer
    }

    companion object {
        /**
         * Default dynamic delta acceleration threshold in m/s² to qualify as significant movement.
         */
        const val DEFAULT_MOVEMENT_THRESHOLD = 1.8f

        /**
         * Pure calculation helper for 3D Euclidean distance (magnitude of delta acceleration).
         * Can be unit-tested without Android framework dependencies.
         */
        fun calculateDeltaMagnitude(
            x1: Float, y1: Float, z1: Float,
            x2: Float, y2: Float, z2: Float
        ): Float {
            val dx = x2 - x1
            val dy = y2 - y1
            val dz = z2 - z1
            return sqrt(dx * dx + dy * dy + dz * dz)
        }

        /**
         * Determines if delta magnitude exceeds the movement threshold.
         */
        fun isMovementSignificant(
            deltaMagnitude: Float,
            threshold: Float = DEFAULT_MOVEMENT_THRESHOLD
        ): Boolean {
            return deltaMagnitude > threshold
        }
    }
}
