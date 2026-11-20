package com.focussense.app.sensors

import android.content.Context
import com.focussense.app.sensors.model.AccelerationData
import com.focussense.app.sensors.model.LightData
import com.focussense.app.sensors.model.ProximityData
import com.focussense.app.sensors.model.SecondarySensorType
import kotlinx.coroutines.flow.StateFlow

/**
 * Default implementation of [SensorRepository] delegating to [AccelerometerMonitor],
 * [AmbientLightMonitor], and [ProximityMonitor].
 * Employs subscriber reference counting to avoid unnecessary sensor power consumption.
 */
class DefaultSensorRepository(
    context: Context,
    val accelerometerMonitor: AccelerometerMonitor = AccelerometerMonitor(context.applicationContext),
    val ambientLightMonitor: AmbientLightMonitor = AmbientLightMonitor(context.applicationContext),
    val proximityMonitor: ProximityMonitor = ProximityMonitor(context.applicationContext)
) : SensorRepository {

    override val accelerometerFlow: StateFlow<AccelerationData> =
        accelerometerMonitor.accelerationData

    override val lightFlow: StateFlow<LightData> =
        ambientLightMonitor.lightData

    override val proximityFlow: StateFlow<ProximityData> =
        proximityMonitor.proximityData

    override val isAccelerometerAvailable: Boolean =
        accelerometerMonitor.isSupported

    override val isLightSensorAvailable: Boolean =
        ambientLightMonitor.isSupported

    override val isProximitySensorAvailable: Boolean =
        proximityMonitor.isSupported

    override val proximitySensorName: String =
        proximityMonitor.sensorName

    override val activeSecondarySensorType: SecondarySensorType = when {
        isLightSensorAvailable -> SecondarySensorType.LIGHT
        isProximitySensorAvailable -> SecondarySensorType.PROXIMITY
        else -> SecondarySensorType.NONE
    }

    private var subscriberCount = 0

    @Synchronized
    override fun startListening() {
        subscriberCount++
        if (subscriberCount == 1) {
            accelerometerMonitor.startListening()
            ambientLightMonitor.startListening()
            proximityMonitor.startListening()
        }
    }

    @Synchronized
    override fun stopListening() {
        if (subscriberCount > 0) {
            subscriberCount--
            if (subscriberCount == 0) {
                accelerometerMonitor.stopListening()
                ambientLightMonitor.stopListening()
                proximityMonitor.stopListening()
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
        proximityMonitor.stopListening()
    }
}
