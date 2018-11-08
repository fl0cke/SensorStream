package com.daimler.sensorstream

import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.daimler.sensorstream.service.SensorEventStreamingService
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), SensorEventManager.Observer {
    companion object {
        const val LOG_TAG = "MOBILE"
    }

    private val adapter = EventListAdapter()
    private val accelerometerData = LineData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startService(Intent(this, SensorEventStreamingService::class.java))
        setupAccelerometerChart()
    }

    override fun onStart() {
        super.onStart()
        // TODO: how to make this callback nice? and work well with the lifecycle stuff
        SensorEventManager.observe(this, this)
    }

    private fun setupAccelerometerChart() {
        val dataSetX = LineDataSet(ArrayList(), "X")
        dataSetX.setColor(Color.RED, 255)
        accelerometerData.addDataSet(dataSetX)

        val dataSetY = LineDataSet(ArrayList(), "Y")
        dataSetY.setColor(Color.BLUE, 255)
        accelerometerData.addDataSet(dataSetY)

        val dataSetZ = LineDataSet(ArrayList(), "Z")
        dataSetZ.setColor(Color.GREEN, 255)
        accelerometerData.addDataSet(dataSetZ)

        chart_accelerometer.data = accelerometerData
        chart_accelerometer.invalidate()
    }

    override fun onSensorEventReceived(sensorEvent: SensorStreamEvent) {
        when (sensorEvent.sensorType) {
            Sensor.TYPE_ACCELEROMETER -> handleAccelerometerEvent(sensorEvent)
        }
    }

    private fun handleAccelerometerEvent(sensorEvent: SensorStreamEvent) {
        val x = sensorEvent.timestamp.toFloat()
        val entryX = Entry(x, sensorEvent.values[0])
        val entryY = Entry(x, sensorEvent.values[1])
        val entryZ = Entry(x, sensorEvent.values[2])
        accelerometerData.addEntry(entryX, 0)
        accelerometerData.addEntry(entryY, 1)
        accelerometerData.addEntry(entryZ, 2)
        chart_accelerometer.notifyDataSetChanged()
        chart_accelerometer.invalidate()
    }

}
