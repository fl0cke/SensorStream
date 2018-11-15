package com.daimler.sensorstream

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.LineChart
import kotlinx.android.synthetic.main.activity_live_preview.*
import java.util.ArrayList

class LivePreviewFragment : Fragment(), SensorEventManager.SensorDataObserver {

    companion object {
        const val LOG_TAG = "LivePreviewActivity"
        const val ARG_SENSOR_TYPES = "SENSOR_TYPES"

        fun newInstance(sensorTypes: List<Int>): LivePreviewFragment {
            val fragment = LivePreviewFragment()
            val args = Bundle()
            args.putIntegerArrayList(ARG_SENSOR_TYPES, sensorTypes as ArrayList<Int>)
            fragment.arguments = args
            return fragment
        }

    }

    private lateinit var sensorCharts: Map<Int, SensorChart>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SensorEventManager.registerObserver(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_live_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val sensorTypes = arguments!!.getIntegerArrayList(ARG_SENSOR_TYPES)!!
        sensorCharts = sensorTypes.associate {
            val chartView = layoutInflater.inflate(R.layout.view_chart, charts, false) as LineChart
            charts.addView(chartView)
            it to SensorChart(it, chartView)
        }
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
