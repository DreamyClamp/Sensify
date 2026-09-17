package io.sensify.sensor.ui.components.visualizers

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * 2D Bubble Spirit Level for Accelerometer / Gravity.
 * Features dynamic multi-color gradient bubble and tilt angle degree readout.
 */
@Composable
fun BubbleLevelVisualizer(
    x: Float,
    y: Float,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val animX by animateFloatAsState(
        targetValue = x.coerceIn(-9.8f, 9.8f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "bubbleX"
    )
    val animY by animateFloatAsState(
        targetValue = y.coerceIn(-9.8f, 9.8f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "bubbleY"
    )

    val pitchDeg = (animY / 9.81f * 90f).coerceIn(-90f, 90f)
    val rollDeg = (animX / 9.81f * 90f).coerceIn(-90f, 90f)
    val isLevel = (animX * animX + animY * animY) < 0.8f

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(8.dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = minOf(size.width, size.height) / 2f - 6.dp.toPx()

            // Dynamic Radial gradient background disk
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.16f),
                        accentColor.copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius
                ),
                radius = maxRadius,
                center = center
            )
            // Outer ring
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.4f),
                        Color(0xFF00E5FF).copy(alpha = 0.6f),
                        accentColor.copy(alpha = 0.4f)
                    ),
                    center = center
                ),
                radius = maxRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            // Middle ring
            drawCircle(
                color = accentColor.copy(alpha = 0.2f),
                radius = maxRadius * 0.55f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
            // Center bullseye ring
            val bullseyeGradient = if (isLevel) {
                listOf(Color(0xFF00E676), Color(0xFF1DE9B6))
            } else {
                listOf(accentColor.copy(alpha = 0.5f), accentColor.copy(alpha = 0.2f))
            }
            drawCircle(
                brush = Brush.linearGradient(bullseyeGradient),
                radius = maxRadius * 0.25f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Crosshair guidelines
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, accentColor.copy(alpha = 0.35f), Color.Transparent)
                ),
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1.2.dp.toPx()
            )
            drawLine(
                brush = Brush.verticalGradient(
                    listOf(Color.Transparent, accentColor.copy(alpha = 0.35f), Color.Transparent)
                ),
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1.2.dp.toPx()
            )

            // Bubble offset calculation
            val bubbleFactor = maxRadius / 9.8f * 0.78f
            val bubbleCenter = Offset(
                x = center.x - (animX * bubbleFactor),
                y = center.y + (animY * bubbleFactor)
            )
            val bubbleRadius = 15.dp.toPx()

            // Dynamic fluid gradient for bubble
            val bubbleColors = if (isLevel) {
                listOf(Color(0xFF00E676), Color(0xFF00B0FF), Color.Transparent)
            } else {
                listOf(Color(0xFF00E5FF), accentColor, Color.Transparent)
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = bubbleColors,
                    center = bubbleCenter - Offset(bubbleRadius * 0.3f, bubbleRadius * 0.3f),
                    radius = bubbleRadius * 1.5f
                ),
                radius = bubbleRadius * 1.4f,
                center = bubbleCenter
            )
            drawCircle(
                brush = Brush.linearGradient(
                    colors = if (isLevel) listOf(Color(0xFF00E676), Color(0xFF1DE9B6)) else listOf(Color(0xFF00E5FF), accentColor),
                    start = bubbleCenter - Offset(bubbleRadius, bubbleRadius),
                    end = bubbleCenter + Offset(bubbleRadius, bubbleRadius)
                ),
                radius = bubbleRadius * 0.75f,
                center = bubbleCenter
            )
            // Specular reflection dot
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = bubbleRadius * 0.25f,
                center = bubbleCenter - Offset(bubbleRadius * 0.25f, bubbleRadius * 0.25f)
            )
        }

        // Measurable Angle Readout
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = String.format(Locale.US, "Pitch: %+.1f°", pitchDeg),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isLevel) Color(0xFF00E676) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = String.format(Locale.US, "Roll: %+.1f°", rollDeg),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isLevel) Color(0xFF00E676) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            if (isLevel) {
                Text(
                    text = "• LEVEL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E676)
                )
            }
        }
    }
}

/**
 * 360° Compass Dial with cardinal markings and numeric heading.
 */
@Composable
fun CompassVisualizer(
    azimuthDeg: Float,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val normalizedDeg = ((azimuthDeg % 360f) + 360f) % 360f
    val animAngle by animateFloatAsState(
        targetValue = -normalizedDeg,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "compassRotation"
    )

    val cardinal = when (normalizedDeg.toInt()) {
        in 338..360, in 0..22 -> "N"
        in 23..67 -> "NE"
        in 68..112 -> "E"
        in 113..157 -> "SE"
        in 158..202 -> "S"
        in 203..247 -> "SW"
        in 248..292 -> "W"
        in 293..337 -> "NW"
        else -> "N"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(8.dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = minOf(size.width, size.height) / 2f - 8.dp.toPx()

            // Outer dial gradient ring
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFFFF5252).copy(alpha = 0.5f),
                        accentColor.copy(alpha = 0.25f),
                        Color(0xFF00E5FF).copy(alpha = 0.4f),
                        Color(0xFFFF5252).copy(alpha = 0.5f)
                    ),
                    center = center
                ),
                radius = radius,
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Inner darkened dial surface
            drawCircle(
                color = Color.Black.copy(alpha = 0.25f),
                radius = radius - 1.5.dp.toPx(),
                center = center
            )

            // Rotating compass rose
            rotate(degrees = animAngle, pivot = center) {
                // Compass tick marks
                for (deg in 0 until 360 step 15) {
                    val isCardinal = deg % 90 == 0
                    val isSubCardinal = deg % 45 == 0 && !isCardinal
                    val isNorth = deg == 0
                    val tickLen = when {
                        isNorth -> 12.dp.toPx()
                        isCardinal -> 9.dp.toPx()
                        isSubCardinal -> 6.dp.toPx()
                        else -> 3.5.dp.toPx()
                    }
                    val tickColor = when {
                        isNorth -> Color(0xFFFF5252)
                        isCardinal -> Color.White
                        else -> accentColor.copy(alpha = 0.4f)
                    }
                    val rad = Math.toRadians(deg.toDouble())
                    val start = Offset(
                        center.x + (radius - tickLen) * sin(rad).toFloat(),
                        center.y - (radius - tickLen) * cos(rad).toFloat()
                    )
                    val end = Offset(
                        center.x + radius * sin(rad).toFloat(),
                        center.y - radius * cos(rad).toFloat()
                    )
                    drawLine(
                        color = tickColor,
                        start = start,
                        end = end,
                        strokeWidth = if (isCardinal) 2.2.dp.toPx() else 1.dp.toPx()
                    )
                }

                // Dynamic North Needle Point (Red-Orange arrow)
                val northPath = Path().apply {
                    moveTo(center.x, center.y - radius * 0.72f)
                    lineTo(center.x - 6.5.dp.toPx(), center.y)
                    lineTo(center.x + 6.5.dp.toPx(), center.y)
                    close()
                }
                drawPath(
                    path = northPath,
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFFF5252), Color(0xFFFF7A00)),
                        startY = center.y - radius * 0.72f,
                        endY = center.y
                    )
                )

                // South Needle Point (Teal-Cyan arrow)
                val southPath = Path().apply {
                    moveTo(center.x, center.y + radius * 0.72f)
                    lineTo(center.x - 6.5.dp.toPx(), center.y)
                    lineTo(center.x + 6.5.dp.toPx(), center.y)
                    close()
                }
                drawPath(
                    path = southPath,
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF00E5FF).copy(alpha = 0.35f), Color(0xFF00B0FF).copy(alpha = 0.85f)),
                        startY = center.y,
                        endY = center.y + radius * 0.72f
                    )
                )

                // Center pivot cap
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color.White, Color.LightGray)),
                    radius = 4.5.dp.toPx(),
                    center = center
                )
            }

            // Fixed Top Heading Index Marker (lubber line triangle at 12 o'clock)
            val topIndicator = Path().apply {
                moveTo(center.x, center.y - radius - 4.dp.toPx())
                lineTo(center.x - 5.dp.toPx(), center.y - radius + 5.dp.toPx())
                lineTo(center.x + 5.dp.toPx(), center.y - radius + 5.dp.toPx())
                close()
            }
            drawPath(
                path = topIndicator,
                color = Color(0xFFFF5252)
            )
        }

        // Measurable Heading Readout
        Text(
            text = String.format(Locale.US, "%.0f° %s (Heading)", normalizedDeg, cardinal),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF5252)
        )
    }
}

/**
 * Radial Arc Gauge with dynamic multi-color gradient and contextual label tags.
 */
@Composable
fun RadialGaugeVisualizer(
    value: Float,
    max: Float = 1000f,
    unit: String = "",
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val progress = (value / max.coerceAtLeast(1f)).coerceIn(0f, 1f)
    val animProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "gaugeProgress"
    )

    val conditionTag = when {
        unit == "lx" -> when {
            value < 20f -> "Dark / Night"
            value < 300f -> "Indoor / Living"
            value < 1000f -> "Bright Office"
            else -> "Direct Daylight"
        }
        unit == "hPa" -> when {
            value < 1000f -> "Low Pressure (Rain/Storm)"
            value < 1020f -> "Normal Atmospheric"
            else -> "High Pressure (Clear Sky)"
        }
        else -> String.format(Locale.US, "%.1f %s", value, unit)
    }

 Column(
 modifier = modifier.fillMaxWidth(),
 horizontalAlignment = Alignment.CenterHorizontally
 ) {
 Canvas(
 modifier = Modifier
 .fillMaxWidth()
 .height(105.dp)
 .padding(horizontal = 12.dp, vertical = 6.dp)
 ) {
 val strokeWidth = 10.dp.toPx()
 val diameter = minOf(size.width, size.height * 1.7f) - strokeWidth
 val arcTopLeft = Offset((size.width - diameter) / 2f, size.height - diameter / 1.8f)
 val arcSize = Size(diameter, diameter)

 // Background track
 drawArc(
 color = accentColor.copy(alpha = 0.12f),
 startAngle = 180f,
 sweepAngle = 180f,
 useCenter = false,
 topLeft = arcTopLeft,
 size = arcSize,
 style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
 )

 // Dynamic Sweep Gradient Arc
 drawArc(
 brush = Brush.horizontalGradient(
 colors = listOf(
 Color(0xFF00E5FF),
 accentColor,
 Color(0xFFFF7A00),
 Color(0xFFFF5252)
 )
 ),
 startAngle = 180f,
 sweepAngle = 180f * animProgress,
 useCenter = false,
 topLeft = arcTopLeft,
 size = arcSize,
 style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
 )

 // Needle point
 val angleRad = Math.toRadians((180f + 180f * animProgress).toDouble())
 val arcCenter = Offset(arcTopLeft.x + diameter / 2f, arcTopLeft.y + diameter / 2f)
 val dotX = arcCenter.x + (diameter / 2f) * cos(angleRad).toFloat()
 val dotY = arcCenter.y + (diameter / 2f) * sin(angleRad).toFloat()

 drawCircle(
 color = Color.White,
 radius = 6.dp.toPx(),
 center = Offset(dotX, dotY)
 )
 drawCircle(
 brush = Brush.radialGradient(listOf(Color(0xFF00E5FF), accentColor)),
 radius = 3.5.dp.toPx(),
 center = Offset(dotX, dotY)
 )
 }

 Text(
 text = conditionTag,
 fontSize = 11.sp,
 fontWeight = FontWeight.Medium,
 color = MaterialTheme.colorScheme.primary
 )
 }
}

/**
 * Radar Pulse Visualizer for Proximity Sensor with dynamic wave gradients.
 */
@Composable
fun ProximityVisualizer(
 isNear: Boolean,
 distanceCm: Float,
 modifier: Modifier = Modifier,
 accentColor: Color = MaterialTheme.colorScheme.primary
) {
 val activeColors = if (isNear) {
 listOf(Color(0xFFFF5252), Color(0xFFFF7A00))
 } else {
 listOf(Color(0xFF00E676), Color(0xFF00E5FF))
 }

 Column(
 modifier = modifier.fillMaxWidth(),
 horizontalAlignment = Alignment.CenterHorizontally
 ) {
 Canvas(
 modifier = Modifier
 .fillMaxWidth()
 .height(105.dp)
 .padding(8.dp)
 ) {
 val center = Offset(size.width / 2f, size.height / 2f)
 val maxRadius = minOf(size.width, size.height) / 2f - 6.dp.toPx()

 for (i in 1..3) {
 val waveRadius = maxRadius * (i / 3f)
 val alpha = if (isNear) 0.35f / i else 0.15f / i
 drawCircle(
 brush = Brush.radialGradient(
 colors = listOf(activeColors[0].copy(alpha = alpha), Color.Transparent),
 center = center,
 radius = waveRadius
 ),
 radius = waveRadius,
 center = center
 )
 drawCircle(
 brush = Brush.sweepGradient(
 colors = activeColors.map { it.copy(alpha = if (isNear) 0.6f else 0.3f) },
 center = center
 ),
 radius = waveRadius,
 center = center,
 style = Stroke(width = 1.8.dp.toPx())
 )
 }

 drawCircle(
 brush = Brush.radialGradient(activeColors),
 radius = if (isNear) 12.dp.toPx() else 8.dp.toPx(),
 center = center
 )
 drawCircle(
 color = Color.White.copy(alpha = 0.9f),
 radius = if (isNear) 5.dp.toPx() else 3.dp.toPx(),
 center = center
 )
 }

 Text(
 text = if (isNear) "NEAR (< 3 cm) • Sensor Covered" else "FAR (Clear) • Beam Open",
 fontSize = 11.sp,
 fontWeight = FontWeight.Bold,
 color = activeColors[0]
 )
 }
}
