package com.daimler.sensorstream

import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.util.Pools
import com.daimler.sensorstream.service.SensorEventStreamingService
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorEventManager.Observer {

    companion object {
        const val LOG_TAG = "MOBILE"
        const val RANGE = 10000000000f
        const val MAX_ENTRY_COUNT = 1500
        const val MAX_POOL_SIZE = 10000
    }

    private val accelerometerData = LineData()
    private val gyroscopeData = LineData()
    private val magneticFieldData = LineData()
    private val pressureData = LineData()
    private val entryPool = Pools.SimplePool<Entry>(MAX_POOL_SIZE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startService(Intent(this, SensorEventStreamingService::class.java))
        setupAccelerometerChart()
        setupGyroscopeChart()
        //setupMagneticFieldChart()
        setupPressureSensorChart()
    }

    override fun onStart() {
        super.onStart()
        // TODO: how to make this callback nice? and work well with the lifecycle stuff
        SensorEventManager.observe(this, this)
    }

    private fun setupAccelerometerChart() {
        val dataSetX = LineDataSet(ArrayList(), "X")
        dataSetX.setColor(Color.RED, 255)
        dataSetX.setDrawValues(false)
        dataSetX.setDrawCircles(false)
        accelerometerData.addDataSet(dataSetX)

        val dataSetY = LineDataSet(ArrayList(), "Y")
        dataSetY.setColor(Color.BLUE, 255)
        dataSetY.setDrawValues(false)
        dataSetY.setDrawCircles(false)
        accelerometerData.addDataSet(dataSetY)

        val dataSetZ = LineDataSet(ArrayList(), "Z")
        dataSetZ.setColor(Color.GREEN, 255)
        dataSetZ.setDrawValues(false)
        dataSetZ.setDrawCircles(false)
        accelerometerData.addDataSet(dataSetZ)

        with(chart_accelerometer) {
            disableScroll()
            description.text = "Accelerometer"
            data = accelerometerData
            xAxis.setDrawLabels(false)
            invalidate()
        }
    }

    private fun setupGyroscopeChart() {
        val dataSetX = LineDataSet(ArrayList(), "X")
        dataSetX.setColor(Color.RED, 255)
        dataSetX.setDrawValues(false)
        dataSetX.setDrawCircles(false)
        gyroscopeData.addDataSet(dataSetX)

        val dataSetY = LineDataSet(ArrayList(), "Y")
        dataSetY.setColor(Color.BLUE, 255)
        dataSetY.setDrawValues(false)
        dataSetY.setDrawCircles(false)
        gyroscopeData.addDataSet(dataSetY)

        val dataSetZ = LineDataSet(ArrayList(), "Z")
        dataSetZ.setColor(Color.GREEN, 255)
        dataSetZ.setDrawValues(false)
        dataSetZ.setDrawCircles(false)
        gyroscopeData.addDataSet(dataSetZ)

        with(chart_gyroscope) {
            disableScroll()
            description.text = "Gyroscope"
            data = gyroscopeData
            xAxis.setDrawLabels(false)
            invalidate()
        }
    }

    private fun setupMagneticFieldChart() {
        val dataSetX = LineDataSet(ArrayList(), "X")
        dataSetX.setColor(Color.RED, 255)
        dataSetX.setDrawValues(false)
        dataSetX.setDrawCircles(false)
        magneticFieldData.addDataSet(dataSetX)

        val dataSetY = LineDataSet(ArrayList(), "Y")
        dataSetY.setColor(Color.BLUE, 255)
        dataSetY.setDrawValues(false)
        dataSetY.setDrawCircles(false)
        magneticFieldData.addDataSet(dataSetY)

        val dataSetZ = LineDataSet(ArrayList(), "Z")
        dataSetZ.setColor(Color.GREEN, 255)
        dataSetZ.setDrawValues(false)
        dataSetZ.setDrawCircles(false)
        magneticFieldData.addDataSet(dataSetZ)
/*
        with(chart_magnetic_field) {
            disableScroll()
            description.text = "Magnetic Field"
            data = magneticFieldData
            xAxis.setDrawLabels(false)
            invalidate()
        }*/
    }

    private fun setupPressureSensorChart() {
        val dataSet = LineDataSet(ArrayList(), "Pressure")
        dataSet.setColor(Color.RED, 255)
        dataSet.setDrawValues(false)
        dataSet.setDrawCircles(false)
        pressureData.addDataSet(dataSet)

        with(chart_pressure) {
            disableScroll()
            description.text = "Pressure"
            data = pressureData
            xAxis.setDrawLabels(false)
            invalidate()
        }
    }

    override fun onSensorDataReceived(sensorDataEvent: SensorDataEvent) {
        when (sensorDataEvent.sensorType) {
            Sensor.TYPE_ACCELEROMETER -> handleAccelerometerEvent(sensorDataEvent)
            Sensor.TYPE_PRESSURE -> handlePressureSensorEvent(sensorDataEvent)
            Sensor.TYPE_GYROSCOPE -> handleGyroscopeEvent(sensorDataEvent)
            Sensor.TYPE_MAGNETIC_FIELD -> handleMagneticFieldSensorEvent(sensorDataEvent)
        }
    }

    override fun onSensorSelectionReceived(sensorSelectionEvent: SensorSelectionEvent) {
        // clear all entries
        chart_accelerometer.data.dataSets.forEach { it.clear() }
        chart_accelerometer.invalidate()
    }

    private fun handleAccelerometerEvent(sensorEvent: SensorDataEvent) {
        val x = sensorEvent.timestamp.toFloat()

        val entryX = Entry(x, sensorEvent.values[0])
        val entryY = Entry(x, sensorEvent.values[1])
        val entryZ = Entry(x, sensorEvent.values[2])

        val dataSetX = accelerometerData.dataSets[0]
        dataSetX.addEntry(entryX)

        val dataSetY = accelerometerData.dataSets[1]
        dataSetY.addEntry(entryY)

        val dataSetZ = accelerometerData.dataSets[2]
        dataSetZ.addEntry(entryZ)

        if (dataSetX.entryCount > MAX_ENTRY_COUNT) {
            dataSetX.removeFirst()
            dataSetY.removeFirst()
            dataSetZ.removeFirst()
        }

        with(chart_accelerometer) {
            data.notifyDataChanged()
            notifyDataSetChanged()
            setVisibleXRange(RANGE, RANGE)
            moveViewToX(x)
            invalidate()
        }
    }

    private fun handlePressureSensorEvent(sensorEvent: SensorDataEvent) {
        val x = sensorEvent.timestamp.toFloat()

        val entry = Entry(x, sensorEvent.values[0])

        val dataSet = pressureData.dataSets[0]
        dataSet.addEntry(entry)

        if (dataSet.entryCount > MAX_ENTRY_COUNT) {
            dataSet.removeFirst()
        }

        with(chart_pressure) {
            data.notifyDataChanged()
            notifyDataSetChanged()
            setVisibleXRange(RANGE, RANGE)
            moveViewToX(x)
            invalidate()
        }
    }

    private fun handleGyroscopeEvent(sensorEvent: SensorDataEvent) {
        val x = sensorEvent.timestamp.toFloat()

        val entryX = Entry(x, sensorEvent.values[0])
        val entryY = Entry(x, sensorEvent.values[1])
        val entryZ = Entry(x, sensorEvent.values[2])

        val dataSetX = gyroscopeData.dataSets[0]
        dataSetX.addEntry(entryX)

        val dataSetY = gyroscopeData.dataSets[1]
        dataSetY.addEntry(entryY)

        val dataSetZ = gyroscopeData.dataSets[2]
        dataSetZ.addEntry(entryZ)

        if (dataSetX.entryCount > MAX_ENTRY_COUNT) {
            dataSetX.removeFirst()
            dataSetY.removeFirst()
            dataSetZ.removeFirst()
        }

        with(chart_gyroscope) {
            data.notifyDataChanged()
            notifyDataSetChanged()
            setVisibleXRange(RANGE, RANGE)
            moveViewToX(x)
            invalidate()
        }
    }

    private fun handleMagneticFieldSensorEvent(sensorEvent: SensorDataEvent) {
        val x = sensorEvent.timestamp.toFloat()

        val entryX = Entry(x, sensorEvent.values[0])
        val entryY = Entry(x, sensorEvent.values[1])
        val entryZ = Entry(x, sensorEvent.values[2])

        val dataSetX = magneticFieldData.dataSets[0]
        dataSetX.addEntry(entryX)

        val dataSetY = magneticFieldData.dataSets[1]
        dataSetY.addEntry(entryY)

        val dataSetZ = magneticFieldData.dataSets[2]
        dataSetZ.addEntry(entryZ)

        if (dataSetX.entryCount > MAX_ENTRY_COUNT) {
            dataSetX.removeFirst()
            dataSetY.removeFirst()
            dataSetZ.removeFirst()
        }

      /*  with(chart_magnetic_field) {
            data.notifyDataChanged()
            notifyDataSetChanged()
            setVisibleXRange(RANGE, RANGE)
            moveViewToX(x)
            invalidate()
        }*/
    }
}
