package com.focussense.app.sensors

import android.content.Context
import com.focussense.app.sensors.model.AccelerationData
import com.focussense.app.sensors.model.LightData
import kotlinx.coroutines.flow.StateFlow

/**
 * Default implementation of [SensorRepository] delegating to [AccelerometerMonitor] and [AmbientLightMonitor].
 * Employs subscriber reference counting to avoid unnecessary sensor power consumption.
 */
class DefaultSensorRepository(
    context: Context,
    val accelerometerMonitor: AccelerometerMonitor = AccelerometerMonitor(context.applicationContext),
    val ambientLightMonitor: AmbientLightMonitor = AmbientLightMonitor(context.applicationContext)
) : SensorRepository {

    override val accelerometerFlow: StateFlow<AccelerationData> =
        accelerometerMonitor.accelerationData

    override val lightFlow: StateFlow<LightData> =
        ambientLightMonitor.lightData

    override val isAccelerometerAvailable: Boolean =
        accelerometerMonitor.isSupported

    override val isLightSensorAvailable: Boolean =
        ambientLightMonitor.isSupported

    private var subscriberCount = 0

    @Synchronized
    override fun startListening() {
        subscriberCount++
        if (subscriberCount == 1) {
            accelerometerMonitor.startListening()
            ambientLightMonitor.startListening()
        }
    }

    @Synchronized
    override fun stopListening() {
        if (subscriberCount > 0) {
            subscriberCount--
            if (subscriberCount == 0) {
                accelerometerMonitor.stopListening()
                ambientLightMonitor.stopListening()
            }
        }
    }

    override fun setMovementThreshold(threshold: Float) {
        accelerometerMonitor.movementThreshold = threshold
    }

    /**
     * Force stop all hardware sensors immediately regardless of subscriber count.
     */
    @Synchronized
    fun forceStop() {
        subscriberCount = 0
        accelerometerMonitor.stopListening()
        ambientLightMonitor.stopListening()
    }
}
