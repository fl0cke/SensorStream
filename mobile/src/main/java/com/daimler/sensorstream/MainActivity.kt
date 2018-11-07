package com.daimler.sensorstream

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable


class MainActivity : AppCompatActivity() {

    companion object {
        const val LOG_TAG = "MOBILE"
    }

    private val adapter = EventListAdapter()
    private lateinit var channelClient: ChannelClient

    private val callback = object : ChannelClient.ChannelCallback() {
        override fun onChannelClosed(p0: ChannelClient.Channel, p1: Int, p2: Int) {
            Log.d(LOG_TAG, "channel closed")
        }

        override fun onChannelOpened(p0: ChannelClient.Channel) {
            Log.d(LOG_TAG, "channel opened")
        }

        override fun onInputClosed(p0: ChannelClient.Channel, p1: Int, p2: Int) {
            Log.d(LOG_TAG, "input closed")
        }

        override fun onOutputClosed(p0: ChannelClient.Channel, p1: Int, p2: Int) {
            Log.d(LOG_TAG, "output closed")
        }
    }

    override fun onStart() {
        super.onStart()
        channelClient = Wearable.getChannelClient(this)
        channelClient.registerChannelCallback(callback)
    }

    override fun onStop() {
        channelClient.unregisterChannelCallback(callback)
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

}
