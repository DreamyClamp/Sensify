package io.sensify.sensor.ui.pages.home.items

import android.hardware.Sensor
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import io.sensify.sensor.domains.chart.entity.ModelChartUiUpdate
import io.sensify.sensor.domains.chart.mpchart.MpChartDataManager
import io.sensify.sensor.domains.chart.mpchart.MpChartViewBinder
import io.sensify.sensor.domains.chart.mpchart.MpChartViewUpdater
import io.sensify.sensor.ui.components.chart.mpchart.base.MpChartLineView
import io.sensify.sensor.ui.pages.home.model.ModelHomeSensor
import io.sensify.sensor.ui.resource.values.JlResDimens
import io.sensify.sensor.ui.resource.values.JlResShapes
import io.sensify.sensor.ui.resource.values.JlResTxtStyles

@Composable
fun HomeSensorChartItem(
    modelSensor: ModelHomeSensor = ModelHomeSensor(
        type = Sensor.TYPE_LIGHT
    ),
    mpChartDataManager: MpChartDataManager = MpChartDataManager(modelSensor.type),
    mpChartViewUpdater: MpChartViewUpdater = MpChartViewUpdater(),
) {
    val state = mpChartDataManager.mSensorPacketFlow.collectAsState(
        initial = ModelChartUiUpdate(
            sensorType = modelSensor.type,
            0,
            listOf()
        )
    )
    val sensorUiUpdate = remember { state }

    Log.d("HomeSensorChart", "Chart model: ${modelSensor.name} ${modelSensor.type}  ${mpChartDataManager.sensorType}")

    val colorOnSurface = MaterialTheme.colorScheme.onSurface
    Column(
        modifier = Modifier
            .background(color = Color.Transparent)
            .padding(horizontal = JlResDimens.dp12, vertical = JlResDimens.dp12)
            .fillMaxSize(),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = JlResDimens.dp12),
            text = "${modelSensor.name}",
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start,
            style = JlResTxtStyles.h5,
        )
        AndroidView(
            modifier = Modifier
                .background(color = Color.Transparent)
                .fillMaxSize(),
            factory = { ctx ->
                Log.v("HomeSensorChart", "factory: ${mpChartDataManager.sensorType}")
                val view = MpChartLineView(modelSensor.type)
                val lineChart = MpChartViewBinder(ctx, view, colorOnSurface = colorOnSurface)
                    .prepareDataSets(mpChartDataManager.getModel())
                    .invalidate()
                lineChart
            },
            update = {
                mpChartViewUpdater.update(it, sensorUiUpdate.value, mpChartDataManager.getModel())
            }
        )
        Spacer(modifier = JlResShapes.Space.H18)
    }
}