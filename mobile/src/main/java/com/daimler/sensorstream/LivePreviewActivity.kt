package com.daimler.sensorstream

import android.arch.lifecycle.LiveData
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.github.mikephil.charting.charts.LineChart
import kotlinx.android.synthetic.main.activity_main.*

class LivePreviewActivity : AppCompatActivity(), SensorEventManager.SensorDataObserver {

    companion object {
        const val LOG_TAG = "LivePreviewActivity"
        const val EXTRA_SENSOR_TYPES = "SENSOR_TYPES"
    }

    private lateinit var sensorCharts: Map<Int, SensorChart>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_preview)

        val sensorTypes = intent.getIntArrayExtra(EXTRA_SENSOR_TYPES)
        sensorCharts = sensorTypes.associate {
            val chartView = layoutInflater.inflate(R.layout.view_chart, charts, false) as LineChart
            charts.addView(chartView)
            it to SensorChart(it, chartView)
        }

        SensorEventManager.registerObserver(this)
    }


    override fun onDestroy() {
        // TODO: there is a strange race condition where this is called before
        SensorEventManager.unregisterObserver(this)
        super.onDestroy()
    }

    override fun onSensorDataReceived(sensorDataEvent: SensorDataEvent) {
        sensorCharts[sensorDataEvent.sensorType]!!.appendSensorData(sensorDataEvent)
    }

}
