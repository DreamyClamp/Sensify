package io.sensify.sensor.ui.components.chart.mpchart

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.github.mikephil.charting.charts.LineChart
import io.sensify.sensor.ui.components.chart.mpchart.base.MpChartLineView

class MpChartLineAxis(key: Int) : MpChartLineView(key) {

    override fun create(context: Context, colorSurface: Color, colorOnSurface: Color): LineChart {
        val lineChart = super.create(context, colorSurface, colorOnSurface)

        lineChart.apply {
            axisLeft.setDrawLabels(true)
            axisLeft.setDrawGridLines(true)
            axisLeft.gridColor = android.graphics.Color.argb(35, 255, 255, 255)
            axisLeft.textColor = android.graphics.Color.argb(180, 255, 255, 255)
            axisLeft.textSize = 9f
            legend.textColor = android.graphics.Color.argb(200, 255, 255, 255)
            legend.textSize = 10f
        }

        return lineChart
    }




}