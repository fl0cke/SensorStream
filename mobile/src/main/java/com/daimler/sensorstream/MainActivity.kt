package com.daimler.sensorstream

import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.view.Gravity
import com.daimler.sensorstream.service.SensorEventStreamingService
import com.github.mikephil.charting.data.LineData
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

    private val accelerometerDataSetX = SensorDataSet("X", 500)
    private val accelerometerDataSetY = SensorDataSet("Y", 500)
    private val accelerometerDataSetZ = SensorDataSet("Z", 500)

    private val gyroscopeDataSetX = SensorDataSet("X", 500)
    private val gyroscopeDataSetY = SensorDataSet("Y", 500)
    private val gyroscopeDataSetZ = SensorDataSet("Z", 500)

    private val magneticFieldDataSetX = SensorDataSet("X", 200)
    private val magneticFieldDataSetY = SensorDataSet("Y", 200)
    private val magneticFieldDataSetZ = SensorDataSet("Z", 200)

    private val pressureDataSet = SensorDataSet("PRESSURE", 200)

    private var showRealtimePlot = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM or ActionBar.DISPLAY_SHOW_TITLE
        val view = layoutInflater.inflate(R.layout.view_recording_indicator, null, false)
        supportActionBar!!.setCustomView(view, ActionBar.LayoutParams(64, 64, Gravity.RIGHT))

        startService(Intent(this, SensorEventStreamingService::class.java))
        setupAccelerometerChart()
        setupGyroscopeChart()
        setupMagneticFieldChart()
        setupPressureSensorChart()
    }

    override fun onStart() {
        super.onStart()
        // TODO: how to make this callback nice? and work well with the lifecycle stuff
        SensorEventManager.observe(this, this)
    }

    private fun setupAccelerometerChart() {
        accelerometerDataSetX.setColor(Color.RED, 255)
        accelerometerDataSetX.setDrawValues(false)
        accelerometerDataSetX.setDrawCircles(false)
        accelerometerData.addDataSet(accelerometerDataSetX)

        accelerometerDataSetY.setColor(Color.BLUE, 255)
        accelerometerDataSetY.setDrawValues(false)
        accelerometerDataSetY.setDrawCircles(false)
        accelerometerData.addDataSet(accelerometerDataSetY)

        accelerometerDataSetZ.setColor(Color.GREEN, 255)
        accelerometerDataSetZ.setDrawValues(false)
        accelerometerDataSetZ.setDrawCircles(false)
        accelerometerData.addDataSet(accelerometerDataSetZ)

        with(chart_accelerometer) {
            disableScroll()
            description.text = "Accelerometer"
            data = accelerometerData
            xAxis.setDrawLabels(false)
            axisLeft.setDrawLabels(false)
            invalidate()
        }
    }

    private fun setupGyroscopeChart() {
        gyroscopeDataSetX.setColor(Color.RED, 255)
        gyroscopeDataSetX.setDrawValues(false)
        gyroscopeDataSetX.setDrawCircles(false)
        gyroscopeData.addDataSet(gyroscopeDataSetX)

        gyroscopeDataSetY.setColor(Color.BLUE, 255)
        gyroscopeDataSetY.setDrawValues(false)
        gyroscopeDataSetY.setDrawCircles(false)
        gyroscopeData.addDataSet(gyroscopeDataSetY)

        gyroscopeDataSetZ.setColor(Color.GREEN, 255)
        gyroscopeDataSetZ.setDrawValues(false)
        gyroscopeDataSetZ.setDrawCircles(false)
        accelerometerData.addDataSet(gyroscopeDataSetZ)

        with(chart_gyroscope) {
            disableScroll()
            description.text = "Gyroscope"
            data = gyroscopeData
            xAxis.setDrawLabels(false)
            axisLeft.setDrawLabels(false)
            invalidate()
        }
    }

    private fun setupMagneticFieldChart() {
        magneticFieldDataSetX.setColor(Color.RED, 255)
        magneticFieldDataSetX.setDrawValues(false)
        magneticFieldDataSetX.setDrawCircles(false)
        magneticFieldData.addDataSet(magneticFieldDataSetX)

        magneticFieldDataSetY.setColor(Color.BLUE, 255)
        magneticFieldDataSetY.setDrawValues(false)
        magneticFieldDataSetY.setDrawCircles(false)
        magneticFieldData.addDataSet(magneticFieldDataSetY)

        magneticFieldDataSetZ.setColor(Color.GREEN, 255)
        magneticFieldDataSetZ.setDrawValues(false)
        magneticFieldDataSetZ.setDrawCircles(false)
        magneticFieldData.addDataSet(magneticFieldDataSetZ)

        with(chart_magnetic_field) {
            disableScroll()
            description.text = "Magnetic Field"
            data = magneticFieldData
            xAxis.setDrawLabels(false)
            axisLeft.setDrawLabels(false)
            invalidate()
        }
    }

    private fun setupPressureSensorChart() {
        pressureDataSet.setColor(Color.RED, 255)
        pressureDataSet.setDrawValues(false)
        pressureDataSet.setDrawCircles(false)
        pressureData.addDataSet(pressureDataSet)

        with(chart_pressure) {
            disableScroll()
            description.text = "Pressure"
            data = pressureData
            xAxis.setDrawLabels(false)
            axisLeft.setDrawLabels(false)
            invalidate()
        }
    }

    override fun onSensorDataReceived(sensorDataEvent: SensorDataEvent) {
        if (!showRealtimePlot) return
        when (sensorDataEvent.sensorType) {
            Sensor.TYPE_ACCELEROMETER -> handleAccelerometerEvent(sensorDataEvent)
            Sensor.TYPE_PRESSURE -> handlePressureSensorEvent(sensorDataEvent)
            Sensor.TYPE_GYROSCOPE -> handleGyroscopeEvent(sensorDataEvent)
            Sensor.TYPE_MAGNETIC_FIELD -> handleMagneticFieldSensorEvent(sensorDataEvent)
        }
    }

    override fun onSensorStreamStarted(sensorStreamOpenedEvent: SensorStreamOpenedEvent) {
        // clear all entries
        chart_accelerometer.data.dataSets.forEach { it.clear() }
        chart_accelerometer.invalidate()

        chart_gyroscope.data.dataSets.forEach { it.clear() }
        chart_gyroscope.invalidate()

        chart_magnetic_field.data.dataSets.forEach { it.clear() }
        chart_magnetic_field.invalidate()

        chart_pressure.data.dataSets.forEach { it.clear() }
        chart_pressure.invalidate()

        showRealtimePlot = sensorStreamOpenedEvent.showRealtimePlot
        supportActionBar?.customView?.isActivated = true
    }

    override fun onSensorStreamClosed() {
        supportActionBar?.customView?.isActivated = false
    }

    private fun handleAccelerometerEvent(sensorEvent: SensorDataEvent) {
        val x = sensorEvent.timestamp.toFloat()

        accelerometerDataSetX.addSensorReading(x, sensorEvent.values[0])
        accelerometerDataSetY.addSensorReading(x, sensorEvent.values[1])
        accelerometerDataSetZ.addSensorReading(x, sensorEvent.values[2])

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

        pressureDataSet.addSensorReading(x, sensorEvent.values[0])

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

        gyroscopeDataSetX.addSensorReading(x, sensorEvent.values[0])
        gyroscopeDataSetY.addSensorReading(x, sensorEvent.values[1])
        gyroscopeDataSetZ.addSensorReading(x, sensorEvent.values[2])

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

        magneticFieldDataSetX.addSensorReading(x, sensorEvent.values[0])
        magneticFieldDataSetY.addSensorReading(x, sensorEvent.values[1])
        magneticFieldDataSetZ.addSensorReading(x, sensorEvent.values[2])

        with(chart_magnetic_field) {
            data.notifyDataChanged()
            notifyDataSetChanged()
            setVisibleXRange(RANGE, RANGE)
            moveViewToX(x)
            invalidate()
        }
    }
}
