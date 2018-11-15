package com.daimler.sensorstream

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log
import com.google.android.gms.wearable.*
import java.io.IOException
import java.io.ObjectOutputStream

class MainActivity : WearableActivity(), SensorEventListener {

    companion object {
        const val LOG_TAG = "WEAR"
    }

    private val channelClient by lazy(LazyThreadSafetyMode.NONE) {
        Wearable.getChannelClient(this)
    }
    private val sensorManager by lazy(LazyThreadSafetyMode.NONE) {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private var channel: ChannelClient.Channel? = null
    private var outputStream: ObjectOutputStream? = null

    private val callback = object : ChannelClient.ChannelCallback() {
        override fun onChannelClosed(channel: ChannelClient.Channel, p1: Int, p2: Int) {
            Log.d(LOG_TAG, "Channel Callback: channel closed")
        }

        override fun onChannelOpened(channel: ChannelClient.Channel) {
            Log.d(LOG_TAG, "Channel Callback: channel opened")
            startStreaming(channel)
        }

        override fun onInputClosed(p0: ChannelClient.Channel, p1: Int, p2: Int) {
            Log.d(LOG_TAG, "Channel Callback: input closed")
        }

        override fun onOutputClosed(p0: ChannelClient.Channel, p1: Int, p2: Int) {
            Log.d(LOG_TAG, "Channel Callback: output closed")
            stopStreaming()
        }
    }

    override fun onStop() {
        Log.d(LOG_TAG, "stop")
        stopStreaming()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        channelClient.registerChannelCallback(callback)
    }

    override fun onDestroy() {
        channelClient.unregisterChannelCallback(callback)
        super.onDestroy()
    }


    private fun startStreaming(channel: ChannelClient.Channel) {
        this.channel = channel
        // acquire an output stream from the channel client
        channelClient.getOutputStream(channel).addOnSuccessListener { stream ->
            outputStream = ObjectOutputStream(stream)
            // the sensors that should be enabled are encoded in the path
            channel.path
                    .split('+')
                    .map { it.toInt() }
                    .forEach {
                        enableSensor(it)
                    }
        }
    }

    private fun stopStreaming() {
        disableAllSensors()
        // close the channel/stream
        channel?.let {
            outputStream?.close()
            channelClient.close(it)
            channel = null
            outputStream = null
        }
    }

    private fun enableSensor(type: Int) {
        val sensor = sensorManager.getDefaultSensor(type)
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    private fun disableAllSensors() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.d(LOG_TAG, "Accuracy of sensor ${sensor.name} changed to $accuracy")
    }

    override fun onSensorChanged(event: SensorEvent) {
        val sensorStreamEvent = SensorDataEvent(event.sensor.type, event.timestamp, event.values.copyOf())
        try {
            outputStream?.writeUnshared(sensorStreamEvent)
        } catch (e: IOException) {
            Log.d(LOG_TAG, "Unable to send sensor event", e)
        }
    }

}
