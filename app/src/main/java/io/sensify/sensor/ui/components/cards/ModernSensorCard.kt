package io.sensify.sensor.ui.components.cards

import android.hardware.Sensor
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sensify.sensor.R
import io.sensify.sensor.domains.sensors.SensorsConstants
import io.sensify.sensor.ui.components.visualizers.BubbleLevelVisualizer
import io.sensify.sensor.ui.components.visualizers.CompassVisualizer
import io.sensify.sensor.ui.components.visualizers.ProximityVisualizer
import io.sensify.sensor.ui.components.visualizers.RadialGaugeVisualizer
import io.sensify.sensor.ui.pages.home.model.ModelHomeSensor
import io.sensify.sensor.ui.resource.sensors.SensorsIcons
import io.sensify.sensor.ui.resource.values.JlResDimens
import io.sensify.sensor.ui.resource.values.JlResTxtStyles
import java.util.Locale

@Composable
fun ModernSensorCard(
    modelSensor: ModelHomeSensor,
    liveValues: FloatArray? = null,
    onClick: (sensorType: Int) -> Unit = {},
    onCheckChange: (Int, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleAnim by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "cardBounce"
    )

    val isActive = modelSensor.isActive
    val accentColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scaleAnim
                scaleY = scaleAnim
            }
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick(modelSensor.type) }
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isActive) 0.45f else 0.22f),
                        MaterialTheme.colorScheme.surface.copy(alpha = if (isActive) 0.30f else 0.12f)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = if (isActive) 0.4f else 0.15f),
                        accentColor.copy(alpha = if (isActive) 0.1f else 0.03f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header Row: Icon + Name + Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bubbly icon container
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = if (isActive) 0.15f else 0.08f))
                        .border(
                            1.dp,
                            accentColor.copy(alpha = if (isActive) 0.35f else 0.15f),
                            CircleShape
                        )
                ) {
                    Image(
                        painter = painterResource(
                            SensorsIcons.MAP_TYPE_TO_ICON.get(
                                modelSensor.type,
                                R.drawable.ic_sensor_unknown
                            )
                        ),
                        contentDescription = modelSensor.sensor?.name,
                        colorFilter = ColorFilter.tint(
                            if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = SensorsConstants.MAP_TYPE_TO_NAME.get(
                            modelSensor.type,
                            modelSensor.sensor?.name ?: "Sensor"
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = if (isActive) "Active • Realtime" else "Standby",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) Color(0xFF13ED6A) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Switch(
                    checked = modelSensor.isActive,
                    onCheckedChange = { onCheckChange(modelSensor.type, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF13ED6A),
                        uncheckedThumbColor = Color(0xFF898989),
                        checkedTrackColor = Color(0x4D00FF66),
                        uncheckedTrackColor = Color(0x33B1B1B1)
                    ),
                    modifier = Modifier.scale(0.8f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dedicated Physical Visualizer Area (when active)
            if (isActive) {
                val vals = liveValues ?: floatArrayOf(0f, 0f, 0f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    when (modelSensor.type) {
                        Sensor.TYPE_ACCELEROMETER,
                        Sensor.TYPE_GRAVITY,
                        Sensor.TYPE_LINEAR_ACCELERATION -> {
                            val x = vals.getOrElse(0) { 0f }
                            val y = vals.getOrElse(1) { 0f }
                            BubbleLevelVisualizer(x = x, y = y)
                        }
                        Sensor.TYPE_ROTATION_VECTOR,
                        Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR,
                        Sensor.TYPE_ORIENTATION -> {
                            val azimuth = if (modelSensor.type == Sensor.TYPE_ORIENTATION) {
                                vals.getOrElse(0) { 0f }
                            } else {
                                io.sensify.sensor.domains.sensors.metadata.SensorMath.quaternionToAzimuthDegrees(vals)
                            }
                            CompassVisualizer(azimuthDeg = azimuth)
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            val heading = io.sensify.sensor.domains.sensors.metadata.SensorMath.magneticToHeadingDegrees(vals)
                            CompassVisualizer(azimuthDeg = heading)
                        }
                        Sensor.TYPE_LIGHT -> {
                            val lux = vals.getOrElse(0) { 0f }
                            RadialGaugeVisualizer(value = lux, max = 2000f, unit = "lx")
                        }
                        Sensor.TYPE_PRESSURE -> {
                            val hpa = vals.getOrElse(0) { 1013f }
                            RadialGaugeVisualizer(value = hpa, max = 1100f, unit = "hPa")
                        }
                        Sensor.TYPE_PROXIMITY -> {
                            val dist = vals.getOrElse(0) { 5f }
                            ProximityVisualizer(isNear = dist < 3f, distanceCm = dist)
                        }
                        else -> {
                            val v = vals.getOrElse(0) { 0f }
                            RadialGaugeVisualizer(value = v, max = 100f, unit = "")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Telemetry Numeric Readout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayValue = if (vals.isNotEmpty()) {
                        when (modelSensor.type) {
                            Sensor.TYPE_ACCELEROMETER,
                            Sensor.TYPE_GRAVITY,
                            Sensor.TYPE_LINEAR_ACCELERATION -> {
                                String.format(Locale.US, "X:%.1f  Y:%.1f  Z:%.1f", vals.getOrElse(0){0f}, vals.getOrElse(1){0f}, vals.getOrElse(2){0f})
                            }
                            Sensor.TYPE_ROTATION_VECTOR,
                            Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> {
                                val deg = io.sensify.sensor.domains.sensors.metadata.SensorMath.quaternionToAzimuthDegrees(vals)
                                String.format(Locale.US, "Azimuth: %.0f°", deg)
                            }
                            Sensor.TYPE_MAGNETIC_FIELD -> {
                                val total = io.sensify.sensor.domains.sensors.metadata.SensorMath.vectorMagnitude(vals)
                                String.format(Locale.US, "|B|: %.1f µT", total)
                            }
                            Sensor.TYPE_LIGHT -> {
                                String.format(Locale.US, "%.0f lx", vals.getOrElse(0){0f})
                            }
                            Sensor.TYPE_PRESSURE -> {
                                String.format(Locale.US, "%.1f hPa", vals.getOrElse(0){1013f})
                            }
                            Sensor.TYPE_PROXIMITY -> {
                                if (vals.getOrElse(0){5f} < 3f) "NEAR" else "FAR"
                            }
                            else -> {
                                String.format(Locale.US, "%.2f", vals.getOrElse(0){0f})
                            }
                        }
                    } else "0.00"

                    Text(
                        text = displayValue,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Inspect →",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                // Inactive placeholder prompt
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Turn switch ON to stream visualizer",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}
