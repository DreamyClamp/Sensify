package io.sensify.sensor.ui

import android.content.Context
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import io.sensify.sensor.domains.sensors.packets.SensorPacketsProvider
import io.sensify.sensor.domains.sensors.provider.SensorsProvider
import io.sensify.sensor.ui.navigation.NavGraphApp
import io.sensify.sensor.ui.resource.themes.SensifyM3Theme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        SensorsProvider.getInstance().setSensorManager(sensorManager)
        SensorPacketsProvider.getInstance().setSensorManager(sensorManager)

        setContent {
            SensifyM3Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraphApp()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            SensorsProvider.getInstance().clearAll()
            SensorPacketsProvider.getInstance().clearAll()
        }
    }
}
