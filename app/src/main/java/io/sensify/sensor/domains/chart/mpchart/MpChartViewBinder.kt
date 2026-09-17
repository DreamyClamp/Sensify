package io.sensify.sensor.domains.chart.mpchart

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import io.sensify.sensor.domains.chart.entity.ModelChartDataSet
import io.sensify.sensor.domains.chart.entity.ModelLineChart
import io.sensify.sensor.ui.components.chart.mpchart.base.IMpChartLineView
import io.sensify.util.QueueFixedLength

class MpChartViewBinder (var context: Context, var chartView: IMpChartLineView,
                         var colorSurface: Color =  Color.Transparent,
                         var colorOnSurface: Color = Color.White){

    companion object{
//        const val DATA_SET_TYPE_SINGLE = 1
//        const val DATA_SET_TYPE_3D = 3
    }

    private val chart : LineChart by lazy {
        chartView.create(context, colorSurface, colorOnSurface)
    }


    /*fun attachLineChart(modelLineChart: ModelLineChart){
//        var dataType
    }*/

    fun prepareDataSets(modelLineChart: ModelLineChart): MpChartViewBinder {

        //LOGV(TAG, "addDataSet: dataType: "+ dataType +", color: "+ color + ", label: "+ label +"index: "+ index);
        var lineData: LineData? = chart.data

        var datasets = modelLineChart.getDataSets()

//        Log.d("MpChartViewBinder", " prepareDataSets: ${datasets.size}")

        for (dataSet in datasets){
            lineData = prepareDataSet(dataSet,  lineData)
        }

        return this
    }

    private fun prepareDataSet(modelDataSet: ModelChartDataSet,  pLineData: LineData?): LineData? {
//        Log.d("MpChartViewBinder", " prepareDataSet: ${modelDataSet.getData()?.size()}")

        var chartLineData = pLineData

        val dataSet = createDataSet(modelDataSet)
        if (pLineData == null) {
            val sets = java.util.ArrayList<ILineDataSet>()
            sets.add(dataSet)
            chartLineData = LineData(sets)
            chart.setData(chartLineData)
        } else {
            val sets: MutableList<ILineDataSet> = pLineData.getDataSets()
            sets.add(dataSet)
            chart.setData(chartLineData)
        }

        return chartLineData

    }


    private fun createDataSet(dataSet: ModelChartDataSet): ILineDataSet {
       /* val index: Int = modelLineChart.getIndex(dataType)*/
        val label: String? = dataSet.getLabel()
        val data: QueueFixedLength<Float>? = dataSet.getData()
        val entries: MutableList<Entry>
        if (data == null) {
            entries = ArrayList<Entry>(dataSet.getSampleLength())
            for (i in 0 until dataSet.getSampleLength()) {
                entries.add(Entry(i.toFloat(), 0.0f))
            }
        } else {
            var emptySize: Int = dataSet.getSampleLength() - data.size()
            entries = ArrayList<Entry>(dataSet.getSampleLength())
            for (i in 0 until emptySize) {
                entries.add(Entry(i.toFloat(), 0.0f))
            }
            val iterator: Iterator<Float> = data.getValues()
            while (iterator.hasNext()) {
                if (emptySize < dataSet.getSampleLength()) {
                    entries.add(Entry(emptySize.toFloat(), iterator.next()))
                    emptySize++
                }
            }
        }
        val ds = LineDataSet(entries, label)
        ds.lineWidth = 1.6f
        ds.axisDependency = YAxis.AxisDependency.LEFT
        ds.setDrawCircles(false)
        ds.setDrawValues(false)
        ds.color = dataSet.getColor()
        //LOGV(TAG, "create: dataType: "+ dataTypes+", color: "+ mModelLineChart.getColor(dataTypes));
        return ds
    }

    fun invalidate(): LineChart {
        chart.invalidate()
        return chart
    }
}