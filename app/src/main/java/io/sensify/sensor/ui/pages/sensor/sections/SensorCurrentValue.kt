package io.sensify.sensor.ui.pages.sensor.sections

import android.hardware.Sensor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sensify.sensor.domains.sensors.metadata.SensorPhysicsInfo
import java.util.Locale

@Composable
fun SensorDetailCurrentValue(
    sensorType: Int = -1,
    value: Float = 0.0f,
    liveValues: FloatArray? = null,
    samplingRateHz: Float = 0f
) {
    val meta = SensorPhysicsInfo.getMeta(sensorType)
    val vals = liveValues ?: floatArrayOf(0f, 0f, 0f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.25f)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00E5FF).copy(alpha = 0.45f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Magnitude / Scalar + Frequency Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Real-time Telemetry",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f %s", value, meta.unit),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                }

                // Live Frequency Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF00E5FF).copy(alpha = 0.15f), MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            )
                        )
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f Hz", samplingRateHz),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF00E5FF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Multi-Axis Component Breakdown
            if (vals.size > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (i in 0 until minOf(vals.size, 4)) {
                        val axisLabel = if (i < meta.axisNames.size) meta.axisNames[i] else "Axis $i"
                        val axisColor = when (i) {
                            0 -> Color(0xFFFF5252)
                            1 -> Color(0xFF00E676)
                            2 -> Color(0xFF00B0FF)
                            else -> Color(0xFFFFD600)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(axisColor.copy(alpha = 0.08f))
                                .border(1.dp, axisColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = axisLabel.split(" ").firstOrNull() ?: "Ax",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = axisColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = String.format(Locale.US, "%+.2f", vals[i]),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
