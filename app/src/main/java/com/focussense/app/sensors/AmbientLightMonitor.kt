package com.focussense.app.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.focussense.app.sensors.model.LightCategory
import com.focussense.app.sensors.model.LightData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Monitors ambient illumination using the hardware Light sensor.
 * Categorizes lux readings according to study environment standards.
 */
class AmbientLightMonitor(
    context: Context,
    private val samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_UI
) : SensorEventListener {

    private val sensorManager =
        context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val lightSensor: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    /**
     * True if the device hardware includes an ambient light sensor.
     */
    val isSupported: Boolean = lightSensor != null

    private val _lightData = MutableStateFlow(LightData())
    val lightData: StateFlow<LightData> = _lightData.asStateFlow()

    private var isListening = false

    /**
     * Safely starts listening to ambient light sensor updates if hardware is supported.
     */
    @Synchronized
    fun startListening() {
        if (!isSupported || isListening) return
        sensorManager?.registerListener(this, lightSensor, samplingPeriodUs)
        isListening = true
    }

    /**
     * Stops listening to light sensor updates to conserve device battery.
     */
    @Synchronized
    fun stopListening() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_LIGHT) return

        val lux = event.values[0]
        val category = LightCategory.fromLux(lux)

        _lightData.value = LightData(
            lux = lux,
            category = category,
            timestamp = System.currentTimeMillis()
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op for ambient light
    }
}
