package io.sensify.sensor.ui.components.chart.mpchart.base

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.github.mikephil.charting.charts.LineChart

interface IMpChartLineView {
    fun create(
        context: Context,
        colorSurface: Color = Color.Transparent,
        colorOnSurface: Color = Color.DarkGray
    ): LineChart;
}