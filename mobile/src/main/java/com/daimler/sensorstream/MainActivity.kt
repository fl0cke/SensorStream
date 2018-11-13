package com.daimler.sensorstream

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.view.Gravity
import com.daimler.sensorstream.service.SensorEventStreamingService
import com.github.mikephil.charting.charts.LineChart
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorEventManager.Observer {

    companion object {
        const val LOG_TAG = "MOBILE"
        const val RANGE = 10000000000f
        const val MAX_ENTRY_COUNT = 1500
        const val MAX_POOL_SIZE = 10000
    }

    private var showLivePreview = false
    private val sensorCharts = mutableMapOf<Int, SensorChart>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM or ActionBar.DISPLAY_SHOW_TITLE
        val view = layoutInflater.inflate(R.layout.view_recording_indicator, null, false)
        supportActionBar!!.setCustomView(view, ActionBar.LayoutParams(64, 64, Gravity.RIGHT))

        startService(Intent(this, SensorEventStreamingService::class.java))
    }

    override fun onStart() {
        super.onStart()
        // TODO: how to make this callback nice? and work well with the lifecycle stuff
        SensorEventManager.observe(this, this)
    }

    override fun onSensorDataReceived(sensorDataEvent: SensorDataEvent) {
        if (!showLivePreview) return
        sensorCharts[sensorDataEvent.sensorType]!!.appendSensorData(sensorDataEvent)
    }

    override fun onSensorStreamStarted(sensorStreamOpenedEvent: SensorStreamOpenedEvent) {
        showLivePreview = sensorStreamOpenedEvent.showLivePreview
        supportActionBar?.customView?.isActivated = true

        // clear all existing charts
        sensorCharts.clear()
        charts.removeAllViews()

        if (showLivePreview) {
            // create the charts
            sensorStreamOpenedEvent.selectedSensorTypes.associateTo(sensorCharts) {
                val chartView = layoutInflater.inflate(R.layout.view_chart, charts, false) as LineChart
                charts.addView(chartView)
                it to SensorChart(it, chartView)
            }
        } else {
            layoutInflater.inflate(R.layout.view_placeholder, charts, true)
        }
    }

    override fun onSensorStreamClosed() {
        supportActionBar?.customView?.isActivated = false
    }
}
