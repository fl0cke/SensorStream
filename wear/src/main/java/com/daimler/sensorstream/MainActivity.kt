package com.daimler.sensorstream

import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable

class MainActivity : WearableActivity(), CapabilityClient.OnCapabilityChangedListener {

    companion object {
        const val LOG_TAG = "WEAR"
    }

    private lateinit var channelClient: ChannelClient
    private lateinit var capabilityClient: CapabilityClient

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
        capabilityClient = Wearable.getCapabilityClient(this)
        channelClient.registerChannelCallback(callback)
        capabilityClient.addListener(this, "stream_sensor_data")
    }

    override fun onStop() {
        capabilityClient.removeListener(this)
        channelClient.unregisterChannelCallback(callback)
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        Log.d(LOG_TAG, "capability changed: " + capabilityInfo.toString())
    }
}
