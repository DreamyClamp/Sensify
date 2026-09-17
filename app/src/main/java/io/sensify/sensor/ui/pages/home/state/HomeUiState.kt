package io.sensify.sensor.ui.pages.home.state

import io.sensify.sensor.ui.pages.home.model.ModelHomeSensor

data class HomeUiState(var currentSensor: ModelHomeSensor? = null, var activeSensorCounts: Int = 1) {


}