package com.focussense.app.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object FocusIcons {
    val Home: ImageVector = Icons.Default.Home
    val Settings: ImageVector = Icons.Default.Settings
    val Play: ImageVector = Icons.Default.PlayArrow
    val Check: ImageVector = Icons.Default.Check
    val Close: ImageVector = Icons.Default.Close
    val Warning: ImageVector = Icons.Default.Warning
    val Info: ImageVector = Icons.Default.Info
    val Refresh: ImageVector = Icons.Default.Refresh
    val Delete: ImageVector = Icons.Default.Delete
    val Edit: ImageVector = Icons.Default.Edit
    val ArrowBack: ImageVector = Icons.Default.ArrowBack
    val DateRange: ImageVector = Icons.Default.DateRange

    // Custom Timer Icon (Stopwatch)
    val Timer: ImageVector = ImageVector.Builder(
        name = "FocusTimer",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(10f, 2f)
            lineTo(14f, 2f)
            moveTo(12f, 14f)
            lineTo(12f, 9f)
            moveTo(12f, 22f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 12f, 6f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 22f)
        }
    }.build()

    // Custom Pause Icon
    val Pause: ImageVector = ImageVector.Builder(
        name = "FocusPause",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(6f, 19f)
            horizontalLineTo(10f)
            verticalLineTo(5f)
            horizontalLineTo(6f)
            verticalLineTo(19f)
            close()
            moveTo(14f, 5f)
            verticalLineTo(19f)
            horizontalLineTo(18f)
            verticalLineTo(5f)
            horizontalLineTo(14f)
            close()
        }
    }.build()

    // Custom Stop Icon
    val Stop: ImageVector = ImageVector.Builder(
        name = "FocusStop",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(6f, 6f)
            horizontalLineTo(18f)
            verticalLineTo(18f)
            horizontalLineTo(6f)
            close()
        }
    }.build()

    // Custom Sensors Icon (Waves & Node)
    val Sensors: ImageVector = ImageVector.Builder(
        name = "FocusSensors",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            // Center point
            moveTo(12f, 12f)
            lineTo(12.01f, 12f)
            // Inner wave
            moveTo(8.5f, 8.5f)
            arcTo(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8.5f, 15.5f)
            moveTo(15.5f, 8.5f)
            arcTo(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 15.5f, 15.5f)
            // Outer wave
            moveTo(5.5f, 5.5f)
            arcTo(9.5f, 9.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.5f, 18.5f)
            moveTo(18.5f, 5.5f)
            arcTo(9.5f, 9.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 18.5f, 18.5f)
        }
    }.build()

    // Custom History Icon (Clock with Counter-Clockwise Arrow)
    val History: ImageVector = ImageVector.Builder(
        name = "FocusHistory",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(3f, 12f)
            arcTo(9f, 9f, 0f, isMoreThanHalf = true, isPositiveArc = true, 5.64f, 18.36f)
            moveTo(3f, 7f)
            lineTo(3f, 12f)
            lineTo(8f, 12f)
            moveTo(12f, 7f)
            lineTo(12f, 12f)
            lineTo(15.5f, 14f)
        }
    }.build()

    // Custom Statistics Bar Chart Icon
    val Statistics: ImageVector = ImageVector.Builder(
        name = "FocusStatistics",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(4f, 20f)
            lineTo(20f, 20f)
            moveTo(7f, 16f)
            lineTo(7f, 11f)
            moveTo(12f, 16f)
            lineTo(12f, 6f)
            moveTo(17f, 16f)
            lineTo(17f, 9f)
        }
    }.build()

    // Light Sensor Icon (Sun / Ray)
    val LightSensor: ImageVector = ImageVector.Builder(
        name = "LightSensor",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 16f)
            arcTo(4f, 4f, 0f, isMoreThanHalf = true, isPositiveArc = false, 12f, 8f)
            arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 12f, 16f)
            moveTo(12f, 2f)
            lineTo(12f, 4f)
            moveTo(12f, 20f)
            lineTo(12f, 22f)
            moveTo(4.93f, 4.93f)
            lineTo(6.34f, 6.34f)
            moveTo(17.66f, 17.66f)
            lineTo(19.07f, 19.07f)
            moveTo(2f, 12f)
            lineTo(4f, 12f)
            moveTo(20f, 12f)
            lineTo(22f, 12f)
            moveTo(4.93f, 19.07f)
            lineTo(6.34f, 17.66f)
            moveTo(17.66f, 6.34f)
            lineTo(19.07f, 4.93f)
        }
    }.build()

    // Motion Sensor Icon (Vibrate / Shake)
    val MotionSensor: ImageVector = ImageVector.Builder(
        name = "MotionSensor",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(2f, 8f)
            lineTo(4f, 10f)
            lineTo(2f, 12f)
            lineTo(4f, 14f)
            lineTo(2f, 16f)
            moveTo(22f, 8f)
            lineTo(20f, 10f)
            lineTo(22f, 12f)
            lineTo(20f, 14f)
            lineTo(22f, 16f)
            moveTo(8f, 5f)
            lineTo(16f, 5f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 18f, 7f)
            lineTo(18f, 17f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 19f)
            lineTo(8f, 19f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6f, 17f)
            lineTo(6f, 7f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8f, 5f)
        }
    }.build()

    // Proximity Sensor Icon (Device with proximity waves)
    val ProximitySensor: ImageVector = ImageVector.Builder(
        name = "ProximitySensor",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            // Emitter / phone icon
            moveTo(8f, 21f)
            lineTo(16f, 21f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 18f, 19f)
            lineTo(18f, 11f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 9f)
            lineTo(8f, 9f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6f, 11f)
            lineTo(6f, 19f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, 21f)
            // Sensor indicator dot
            moveTo(12f, 12f)
            lineTo(12.01f, 12f)
            // Proximity waves above phone
            moveTo(9f, 6f)
            arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 15f, 6f)
            moveTo(6.5f, 3.5f)
            arcTo(7.5f, 7.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 17.5f, 3.5f)
        }
    }.build()
}
