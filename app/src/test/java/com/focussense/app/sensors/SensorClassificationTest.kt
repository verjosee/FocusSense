package com.focussense.app.sensors

import com.focussense.app.sensors.model.AccelerationData
import com.focussense.app.sensors.model.LightCategory
import com.focussense.app.sensors.model.LightData
import com.focussense.app.sensors.model.ProximityData
import com.focussense.app.sensors.model.SecondarySensorType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

/**
 * Unit tests validating ambient light categorization logic,
 * accelerometer vector delta calculations, and motion thresholding.
 */
class SensorClassificationTest {

    // --- LightCategory Classification Tests ---

    @Test
    fun testLightCategoryVeryDimBelow50Lux() {
        assertEquals(LightCategory.VERY_DIM, LightCategory.fromLux(0f))
        assertEquals(LightCategory.VERY_DIM, LightCategory.fromLux(10f))
        assertEquals(LightCategory.VERY_DIM, LightCategory.fromLux(49.9f))
    }

    @Test
    fun testLightCategoryDimBetween50And200Lux() {
        assertEquals(LightCategory.DIM, LightCategory.fromLux(50.0f))
        assertEquals(LightCategory.DIM, LightCategory.fromLux(100f))
        assertEquals(LightCategory.DIM, LightCategory.fromLux(200.0f))
    }

    @Test
    fun testLightCategoryModerateBetween200And500Lux() {
        assertEquals(LightCategory.MODERATE, LightCategory.fromLux(200.1f))
        assertEquals(LightCategory.MODERATE, LightCategory.fromLux(350f))
        assertEquals(LightCategory.MODERATE, LightCategory.fromLux(500.0f))
    }

    @Test
    fun testLightCategoryBrightAbove500Lux() {
        assertEquals(LightCategory.BRIGHT, LightCategory.fromLux(500.1f))
        assertEquals(LightCategory.BRIGHT, LightCategory.fromLux(1000f))
        assertEquals(LightCategory.BRIGHT, LightCategory.fromLux(50000f))
    }

    @Test
    fun testLightCategoryNegativeLuxHandledGracefully() {
        // Sensor anomalies or negative noise should safely map to VERY_DIM
        assertEquals(LightCategory.VERY_DIM, LightCategory.fromLux(-5f))
    }

    // --- Accelerometer Delta Magnitude & Thresholding Tests ---

    @Test
    fun testAccelerometerDeltaMagnitudeMath() {
        // Distance between (0, 0, 0) and (1, 2, 2) is sqrt(1 + 4 + 4) = 3.0
        val delta = AccelerometerMonitor.calculateDeltaMagnitude(0f, 0f, 0f, 1f, 2f, 2f)
        assertEquals(3.0f, delta, 0.0001f)
    }

    @Test
    fun testStationaryDeviceIsNotSignificantMovement() {
        // Stationary device resting on flat desk (approx 9.81 on Z-axis with tiny sensor jitter)
        val delta = AccelerometerMonitor.calculateDeltaMagnitude(
            0.01f, -0.02f, 9.80f,
            0.02f, -0.01f, 9.82f
        )
        assertFalse(AccelerometerMonitor.isMovementSignificant(delta))
    }

    @Test
    fun testMovementSignificantWhenDeltaExceedsDefaultThreshold() {
        // Sudden movement: acceleration change delta > 1.8 m/s²
        val delta = AccelerometerMonitor.calculateDeltaMagnitude(
            0.0f, 0.0f, 9.8f,
            1.5f, 1.2f, 9.0f
        )
        // delta = sqrt(1.5^2 + 1.2^2 + (-0.8)^2) = sqrt(2.25 + 1.44 + 0.64) = sqrt(4.33) ~= 2.08 > 1.8
        assertTrue(delta > AccelerometerMonitor.DEFAULT_MOVEMENT_THRESHOLD)
        assertTrue(AccelerometerMonitor.isMovementSignificant(delta))
    }

    @Test
    fun testMovementBoundaryThresholdCondition() {
        // Exactly at threshold: isMovementSignificant requires strictly greater than threshold
        assertFalse(AccelerometerMonitor.isMovementSignificant(1.8f, threshold = 1.8f))
        assertTrue(AccelerometerMonitor.isMovementSignificant(1.801f, threshold = 1.8f))
    }

    @Test
    fun testCustomMovementThreshold() {
        val customThreshold = 0.5f
        val delta = 0.8f
        assertTrue(AccelerometerMonitor.isMovementSignificant(delta, threshold = customThreshold))
    }

    // --- Sensor Models Default State Tests ---

    @Test
    fun testAccelerationDataDefaults() {
        val defaultData = AccelerationData()
        assertEquals(0f, defaultData.x, 0.0001f)
        assertEquals(0f, defaultData.y, 0.0001f)
        assertEquals(0f, defaultData.z, 0.0001f)
        assertEquals(0f, defaultData.magnitude, 0.0001f)
        assertFalse(defaultData.isSignificantMovement)
        assertTrue(defaultData.timestamp > 0L)
    }

    @Test
    fun testLightDataDefaults() {
        val defaultData = LightData()
        assertEquals(0f, defaultData.lux, 0.0001f)
        assertEquals(LightCategory.VERY_DIM, defaultData.category)
        assertTrue(defaultData.timestamp > 0L)
    }

    // --- Proximity Sensor Classification & Model Tests ---

    @Test
    fun testProximityIsNearWhenDistanceLessThanMaxRange() {
        // Typical hardware reporting near state: distance (0.0cm) < maxRange (5.0cm)
        assertTrue(ProximityMonitor.isNear(0.0f, maxRange = 5.0f))
        assertTrue(ProximityMonitor.isNear(1.5f, maxRange = 5.0f))
        assertTrue(ProximityMonitor.isNear(4.99f, maxRange = 5.0f))
    }

    @Test
    fun testProximityIsFarWhenDistanceEqualsOrEqualToMaxRange() {
        // Far state: distance at or beyond max range
        assertFalse(ProximityMonitor.isNear(5.0f, maxRange = 5.0f))
        assertFalse(ProximityMonitor.isNear(8.0f, maxRange = 5.0f))
        assertFalse(ProximityMonitor.isNear(10.0f, maxRange = 5.0f))
    }

    @Test
    fun testProximityDefaultMaxRangeClassification() {
        // Uses default max range (5.0 cm)
        assertTrue(ProximityMonitor.isNear(0.0f))
        assertTrue(ProximityMonitor.isNear(2.0f))
        assertFalse(ProximityMonitor.isNear(5.0f))
        assertFalse(ProximityMonitor.isNear(6.0f))
    }

    @Test
    fun testProximityZeroMaxRangeBoundary() {
        // Sensors with binary 0.0 reporting
        assertTrue(ProximityMonitor.isNear(0.0f, maxRange = 0.0f))
        assertFalse(ProximityMonitor.isNear(1.0f, maxRange = 0.0f))
    }

    @Test
    fun testProximityClassifyStatusNearAndFar() {
        assertEquals("Near", ProximityMonitor.classifyStatus(0.0f, 5.0f))
        assertEquals("Near", ProximityMonitor.classifyStatus(2.5f, 5.0f))
        assertEquals("Far", ProximityMonitor.classifyStatus(5.0f, 5.0f))
        assertEquals("Far", ProximityMonitor.classifyStatus(7.0f, 5.0f))
    }

    @Test
    fun testProximityDataDefaults() {
        val defaultData = ProximityData()
        assertEquals(0.0f, defaultData.distance, 0.0001f)
        assertEquals(5.0f, defaultData.maxRange, 0.0001f)
        assertFalse(defaultData.isNear)
        assertTrue(defaultData.isFar)
        assertEquals("Far", defaultData.statusText)
        assertTrue(defaultData.timestamp > 0L)
    }

    @Test
    fun testProximityDataNearState() {
        val nearData = ProximityData(
            distance = 0.0f,
            maxRange = 5.0f,
            isNear = true
        )
        assertTrue(nearData.isNear)
        assertFalse(nearData.isFar)
        assertEquals("Near", nearData.statusText)
    }

    @Test
    fun testProximityDataFarState() {
        val farData = ProximityData(
            distance = 5.0f,
            maxRange = 5.0f,
            isNear = false
        )
        assertFalse(farData.isNear)
        assertTrue(farData.isFar)
        assertEquals("Far", farData.statusText)
    }

    @Test
    fun testSecondarySensorTypeEnumValues() {
        assertEquals(3, SecondarySensorType.values().size)
        assertTrue(SecondarySensorType.values().contains(SecondarySensorType.LIGHT))
        assertTrue(SecondarySensorType.values().contains(SecondarySensorType.PROXIMITY))
        assertTrue(SecondarySensorType.values().contains(SecondarySensorType.NONE))
    }
}
