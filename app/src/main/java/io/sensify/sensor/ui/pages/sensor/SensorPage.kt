package io.sensify.sensor.ui.pages.sensor.details

import android.hardware.Sensor
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.sensify.sensor.R
import io.sensify.sensor.domains.sensors.provider.SensorsProvider
import io.sensify.sensor.ui.pages.sensor.SensorViewModel
import io.sensify.sensor.ui.pages.sensor.SensorViewModelFactory
import io.sensify.sensor.ui.pages.sensor.sections.SensorChart
import io.sensify.sensor.ui.pages.sensor.sections.SensorDetail
import io.sensify.sensor.ui.pages.sensor.sections.SensorDetailCurrentValue
import io.sensify.sensor.ui.resource.values.JlResDimens
import io.sensify.sensor.ui.resource.values.JlResTxtStyles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.sensify.sensor.domains.sensors.SensorsConstants

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalTextApi::class
)
@Preview(showBackground = true, backgroundColor = 0xFF041B11)
@Composable
fun SensorPage(
    modifier: Modifier = Modifier, navController: NavController? = null,
    type: Int = Sensor.TYPE_GYROSCOPE,
    viewModel: SensorViewModel = viewModel(
        factory = SensorViewModelFactory(
            type
        )
    )
) {
    val lazyListState = rememberLazyListState()

    var sensorFlowState = SensorsProvider.getInstance().listenSensor(type)
        .collectAsState(initial = SensorsProvider.getInstance().getSensor(type))
    var sensorState = remember {
        sensorFlowState
    }

    var sensorRms = viewModel.mSensorModulus.collectAsState(initial = 0.0f)

    var sensorRmsState = remember {sensorRms}
    Log.d("SensorPage", "$type")

    val sensorName = sensorState.value?.name
        ?: SensorsConstants.MAP_TYPE_TO_NAME.get(type, "Sensor")

    val liveValues by viewModel.liveValues.collectAsState()
    val samplingRateHz by viewModel.samplingRateHz.collectAsState()

    var showUnavailableDialog by remember(sensorState.value) {
        mutableStateOf(sensorState.value == null)
    }

    if (showUnavailableDialog) {
        AlertDialog(
            onDismissRequest = { showUnavailableDialog = false },
            icon = {
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Hardware Sensor Unavailable",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This device does not have a physical $sensorName hardware sensor installed. Real-time telemetry, visualizers, and waveform streaming are disabled for this component.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnavailableDialog = false
                        navController?.navigateUp()
                    }
                ) {
                    Text("Go Back")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUnavailableDialog = false }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }

    Scaffold(
        topBar = {

            TopAppBar(
                colors = if (lazyListState.firstVisibleItemIndex > 0) TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                ) else TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color.Transparent
                ),

                modifier = Modifier.padding(horizontal = JlResDimens.dp16),

                navigationIcon = {
                    IconButton(
                        onClick = { navController?.navigateUp() },
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "back")
                    }
                },
                title = {
                    Text(
                        text = sensorName,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        style = JlResTxtStyles.h4,
                        fontWeight = FontWeight(400),
                        modifier = modifier.fillMaxWidth(),
                    )
                }, actions = {
                    Box(Modifier.padding(horizontal = JlResDimens.dp20)) {
                        Image(
                            painterResource(id = R.drawable.pic_sensify_logo),
                            modifier = Modifier
                                .alpha(0f)
                                .width(JlResDimens.dp32)
                                .height(JlResDimens.dp36),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }
            )
        }
    ) { it ->
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f),
                        )
                    )
                ),
            contentPadding = it,
            state = lazyListState
        ) {
            if (sensorState.value == null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = JlResDimens.dp32, vertical = JlResDimens.dp32),
                        shape = RoundedCornerShape(JlResDimens.dp24),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(JlResDimens.dp24),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Rounded.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Sensor Unavailable",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "This device does not have a physical $sensorName hardware sensor installed.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Hero Physical Visualizer
                item {
                    val vals = liveValues ?: floatArrayOf(0f, 0f, 0f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = JlResDimens.dp32, end = JlResDimens.dp32, bottom = JlResDimens.dp16)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.verticalGradient(
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
                                        Color(0xFF00E5FF).copy(alpha = 0.4f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (type) {
                            Sensor.TYPE_ACCELEROMETER,
                            Sensor.TYPE_GRAVITY,
                            Sensor.TYPE_LINEAR_ACCELERATION -> {
                                val x = vals.getOrElse(0) { 0f }
                                val y = vals.getOrElse(1) { 0f }
                                io.sensify.sensor.ui.components.visualizers.BubbleLevelVisualizer(x = x, y = y)
                            }
                            Sensor.TYPE_ROTATION_VECTOR,
                            Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR,
                            Sensor.TYPE_ORIENTATION -> {
                                val azimuth = if (type == Sensor.TYPE_ORIENTATION) {
                                    vals.getOrElse(0) { 0f }
                                } else {
                                    io.sensify.sensor.domains.sensors.metadata.SensorMath.quaternionToAzimuthDegrees(vals)
                                }
                                io.sensify.sensor.ui.components.visualizers.CompassVisualizer(azimuthDeg = azimuth)
                            }
                            Sensor.TYPE_MAGNETIC_FIELD -> {
                                val heading = io.sensify.sensor.domains.sensors.metadata.SensorMath.magneticToHeadingDegrees(vals)
                                io.sensify.sensor.ui.components.visualizers.CompassVisualizer(azimuthDeg = heading)
                            }
                            Sensor.TYPE_LIGHT -> {
                                val lux = vals.getOrElse(0) { 0f }
                                io.sensify.sensor.ui.components.visualizers.RadialGaugeVisualizer(value = lux, max = 2000f, unit = "lx")
                            }
                            Sensor.TYPE_PRESSURE -> {
                                val hpa = vals.getOrElse(0) { 1013f }
                                io.sensify.sensor.ui.components.visualizers.RadialGaugeVisualizer(value = hpa, max = 1100f, unit = "hPa")
                            }
                            Sensor.TYPE_PROXIMITY -> {
                                val dist = vals.getOrElse(0) { 5f }
                                io.sensify.sensor.ui.components.visualizers.ProximityVisualizer(isNear = dist < 3f, distanceCm = dist)
                            }
                            else -> {
                                val v = vals.getOrElse(0) { 0f }
                                io.sensify.sensor.ui.components.visualizers.RadialGaugeVisualizer(value = v, max = 100f, unit = "")
                            }
                        }
                    }
                }

                // Direct Real-time Waveform Chart
                item {
                    SensorChart(
                        modelSensor = sensorState.value ?: io.sensify.sensor.domains.sensors.provider.ModelSensor(type = type),
                        mpChartDataManager = viewModel.getChartDataManager(type),
                        sensorPacketFlow = viewModel.mSensorPacketFlow
                    )
                }

                // Plotting area
                item {
                    Spacer(modifier = Modifier.height(JlResDimens.dp16))
                }

                item {
                    Box(
                        modifier = Modifier.padding(
                            start = JlResDimens.dp32,
                            end = JlResDimens.dp32
                        ),
                    ) {
                        SensorDetailCurrentValue(
                            sensorType = type,
                            value = sensorRmsState.value,
                            liveValues = liveValues,
                            samplingRateHz = samplingRateHz
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(JlResDimens.dp12))
                }

                item {
                    Box(
                        modifier = Modifier.padding(
                            start = JlResDimens.dp32,
                            end = JlResDimens.dp32
                        ),
                    ) {
                        SensorDetail(
                            sensorType = type,
                            keyValues = sensorState.value?.info ?: mutableMapOf()
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(JlResDimens.dp72))
            }
        }
    }
}
