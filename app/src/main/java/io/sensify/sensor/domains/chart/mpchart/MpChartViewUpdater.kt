package io.sensify.sensor.domains.chart.mpchart

import android.hardware.Sensor
import android.util.Log
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import io.sensify.sensor.domains.chart.entity.ModelChartDataSet
import io.sensify.sensor.domains.chart.entity.ModelChartUiUpdate
import io.sensify.sensor.domains.chart.entity.ModelLineChart

class MpChartViewUpdater {


    fun update(chart: LineChart, value: ModelChartUiUpdate, modelLineChart: ModelLineChart) : Boolean {
        if (value.size == 0) return false;
        var lineData: LineData? = chart.data

        if(lineData==null){
//            Log.v("MpChartViewUpdater", "update no line data ")
            return false
        }

        //        value.packets[0].
//        modelLineChart.
        var datasets = modelLineChart.getDataSets()
//        Log.d("MpChartViewManager ", "update status 1: size: ${value.size} ${datasets.size} ")

        for (i in datasets.indices) {
            lineData = updateDataSet(i, value, datasets[i], lineData!!)

        }
        if(value.sensorType == Sensor.TYPE_GYROSCOPE){

//            Log.d("MpChartViewManager ", "update status : size: ${value.size}")
        }

        notifyDataChange(chart)

        //

        /*for (dataSet in datasets){
        }*/

        return true

    }

    private fun updateDataSet(
        index: Int, value: ModelChartUiUpdate,
        modelDataSet: ModelChartDataSet,
        pLineData: LineData
    ): LineData {
        var chartLineData = pLineData

        if (index >= pLineData.dataSets.size) return pLineData;

        var dataset = pLineData.getDataSetByIndex(index)
//        modelDataSet

        var totalShift = 0

        var extraEntry = dataset.entryCount - modelDataSet.getSampleLength();
//        Log.d("MpChartViewManager ", "updateDataSet status 1: index: ${index}  entryCount: ${dataset.entryCount} ")

        if (extraEntry > 0) {
            for (j in 0 until extraEntry) {
                if (dataset.entryCount > 0) {
                    dataset.removeEntry(0)
                    totalShift++
                }
            }
        }

        for (j in 0 until value.size) {
            if (dataset.entryCount > 0) {
                dataset.removeEntry(0)
                totalShift++
            }
        }

        // change Indexes - move to beginning by 1
        for (i in totalShift until dataset.entryCount) {
            val entry: Entry = dataset.getEntryForIndex(i)
            entry.x = entry.x - totalShift
        }

        var entrySize = dataset.entryCount
        for (j in 0 until value.size) {
            val valuesArray = value.packets[j].values
            val pointVal = if (valuesArray != null && index < valuesArray.size) valuesArray[index] else 0f
            chartLineData.addEntry(
                Entry(
                    (entrySize + j).toFloat(),
                    pointVal
                ), index
            )
        }
//        Log.d("MpChartViewManager ", "updateDataSet status 2: index: ${index}  entryCount: ${dataset.entryCount} ")
        return chartLineData
    }


    fun notifyDataChange(chart: LineChart) {
        val data: LineData? = chart.data
        data?.notifyDataChanged()
        chart.notifyDataSetChanged()
        chart.invalidate()
    }



}