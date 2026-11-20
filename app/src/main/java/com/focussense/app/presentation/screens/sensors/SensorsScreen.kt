package com.focussense.app.presentation.screens.sensors

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focussense.app.presentation.components.AccelerometerCard
import com.focussense.app.presentation.components.AmbientLightCard
import com.focussense.app.presentation.components.FocusIcons
import com.focussense.app.presentation.components.ProximityCard
import com.focussense.app.presentation.viewmodel.SensorsUiState
import com.focussense.app.presentation.viewmodel.SensorsViewModel
import com.focussense.app.sensors.model.AccelerationData
import com.focussense.app.sensors.model.LightData
import com.focussense.app.sensors.model.ProximityData
import com.focussense.app.ui.theme.LocalExtendedColors
import com.focussense.app.ui.theme.SkyBlue
import com.focussense.app.ui.theme.SoftViolet

@Composable
fun SensorsScreen(
    viewModel: SensorsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    SensorsScreenContent(
        uiState = uiState,
        modifier = modifier
    )
}

@Composable
fun SensorsScreenContent(
    uiState: SensorsUiState,
    modifier: Modifier = Modifier
) {
    val borderColor = LocalExtendedColors.current.surfaceBorder

    val accelerationData = AccelerationData(
        x = uiState.accelerationX,
        y = uiState.accelerationY,
        z = uiState.accelerationZ,
        magnitude = uiState.netMagnitude,
        isSignificantMovement = uiState.isSignificantMovement,
        timestamp = uiState.accelerometerTimestamp
    )

    val lightData = LightData(
        lux = uiState.lightLux,
        category = uiState.lightCategory,
        timestamp = uiState.lightTimestamp
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        // Title Section
        Column {
            Text(
                text = "Live Environmental Sensors",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Real-time telemetry monitored during your focus sessions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Accelerometer Sensor Card
        AccelerometerCard(
            accelerationData = accelerationData,
            isSupported = uiState.isAccelerometerAvailable
        )

        // Dynamic Secondary Sensor Cards: Ambient Light and/or Proximity
        if (uiState.isLightSensorAvailable) {
            AmbientLightCard(
                lightData = lightData,
                isSupported = true
            )
        }

        if (uiState.isProximitySensorAvailable) {
            ProximityCard(
                proximityData = uiState.proximityData,
                isSupported = true,
                sensorName = uiState.proximitySensorName
            )
        }

        // If neither secondary sensor is present, show unsupported placeholder
        if (!uiState.isLightSensorAvailable && !uiState.isProximitySensorAvailable) {
            AmbientLightCard(
                lightData = lightData,
                isSupported = false
            )
        }

        // Informational Context Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = FocusIcons.Info,
                        contentDescription = null,
                        tint = SoftViolet,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Why Sensor Mindfulness?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "• Accelerometer: Automatically detects when you pick up or shake your phone during a focus session, flagging movement interruptions to help train focus discipline.\n\n" +
                            "• Ambient Light: Monitors desk illumination. Subdued lighting (< 200 lx) may cause eye fatigue, while moderate illumination (200 - 500 lx) optimizes sustained cognitive attention.\n\n" +
                            "• Proximity Sensor: Monitors physical obstacle proximity in front of the device to detect close surface coverage, maintaining distraction-free focus on devices where an ambient light sensor is unavailable.",
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
