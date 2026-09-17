package io.sensify.sensor.domains.chart.entity

import io.sensify.sensor.domains.sensors.packets.ModelSensorPacket

data class ModelChartUiUpdate(
    var sensorType: Int,
    var size: Int,
    var packets: List<ModelSensorPacket> = listOf(),
    var timestamp: Long = System.currentTimeMillis()
) {
}