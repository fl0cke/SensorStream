package com.daimler.sensorstream

import android.graphics.Color
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData

class SensorChart(val sensorType: Int, private val chart: LineChart) {

    private companion object {
        val colors = arrayOf(Color.RED, Color.BLUE, Color.GREEN, Color.CYAN, Color.GRAY, Color.BLACK)
        const val RANGE = 10000000000f
    }

    private val sensorMetaData = SensorMetaData.forType(sensorType)
    private val dataSets: List<SensorDataSet>
    private val data: LineData

    init {

        dataSets = List(sensorMetaData.valueCount) {
            val dataSet = SensorDataSet(sensorMetaData.axisLabels[it], 500)
            dataSet.setColor(colors[it], 255)
            dataSet.setDrawValues(false)
            dataSet.setDrawCircles(false)
            dataSet
        }

        data = LineData(dataSets)
        with(chart) {
            disableScroll()
            description.text = sensorMetaData.name
            data = data
            xAxis.setDrawLabels(false)
            axisLeft.setDrawLabels(false)
            invalidate()
        }
    }

    fun appendSensorData(event: SensorDataEvent) {
        val x = event.timestamp.toFloat()

        event.values.forEachIndexed { index, value ->
            dataSets[index].addSensorReading(x, value)
        }

        with(chart) {
            data.notifyDataChanged()
            notifyDataSetChanged()
            setVisibleXRange(RANGE, RANGE)
            moveViewToX(x)
            invalidate()
        }
    }

}