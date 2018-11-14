package com.daimler.sensorstream

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.daimler.sensorstream.service.SensorEventStreamingService

class MainActivity : AppCompatActivity() {

    companion object {
        const val LOG_TAG = "MOBILE"
        const val RANGE = 10000000000f
        const val MAX_ENTRY_COUNT = 1500
        const val MAX_POOL_SIZE = 10000
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
/*

        supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM or ActionBar.DISPLAY_SHOW_TITLE
        val view = layoutInflater.inflate(R.layout.view_recording_indicator, null, false)
        supportActionBar!!.setCustomView(view, ActionBar.LayoutParams(64, 64, Gravity.RIGHT))
*/

        startService(Intent(this, SensorEventStreamingService::class.java))
    }

}
