package com.daimler.sensorstream

import android.graphics.Color
import android.util.Log
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
            val dataSet = SensorDataSet(sensorMetaData.axisLabels[it], 1000)
            dataSet.setColor(colors[it], 255)
            dataSet.setDrawValues(false)
            dataSet.setDrawCircles(false)
            dataSet
        }

        data = LineData(dataSets)
        chart.disableScroll()
        chart.description.text = sensorMetaData.name
        chart.data = data
        chart.xAxis.setDrawLabels(false)
        chart.axisLeft.setDrawLabels(false)
        chart.invalidate()
    }

    fun appendSensorData(event: SensorDataEvent) {
        val x = event.timestamp.toFloat()

        for (i in 0 until sensorMetaData.valueCount) {
            dataSets[i].addSensorReading(x, event.values[i])
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