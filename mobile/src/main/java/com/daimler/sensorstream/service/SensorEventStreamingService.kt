package com.daimler.sensorstream.service

import android.app.Service
import android.content.Intent
import android.os.Looper
import android.util.Log
import com.daimler.sensorstream.SensorEventManager
import com.daimler.sensorstream.SensorStreamEvent
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import java.io.*
import java.util.concurrent.atomic.AtomicReference


class SensorEventStreamingService : Service(), MessageClient.OnMessageReceivedListener {

    companion object {
        const val LOG_TAG = "SensorEventStreamingService"
    }

    private val messageClient by lazy(LazyThreadSafetyMode.NONE) {
        Wearable.getMessageClient(this)
    }

    private val channelClient by lazy(LazyThreadSafetyMode.NONE) {
        Wearable.getChannelClient(this)
    }

    private var streamingThread: Thread? = null
    private val channelCallback = object : ChannelClient.ChannelCallback() {
        override fun onChannelClosed(channel: ChannelClient.Channel, p1: Int, p2: Int) {
            Log.d(LOG_TAG, "channel closed")
        }

        override fun onChannelOpened(channel: ChannelClient.Channel) {
            channelClient.getInputStream(channel).addOnSuccessListener {
                Log.d(LOG_TAG, "successfully opened input stream to node ${channel.nodeId}")
                startStreaming(it)
            }.addOnFailureListener {
                Log.d(LOG_TAG, "failed to input output stream to node ${channel.nodeId}")
            }
        }

        override fun onInputClosed(channel: ChannelClient.Channel, p1: Int, p2: Int) {
            Log.d(LOG_TAG, "input closed")
        }

        override fun onOutputClosed(channel: ChannelClient.Channel, p1: Int, p2: Int) {
            Log.d(LOG_TAG, "output closed")
        }
    }

    private val outFile = AtomicReference<File>()

    override fun onCreate() {
        super.onCreate()
        messageClient.addListener(this)
        channelClient.registerChannelCallback(channelCallback)
    }

    override fun onDestroy() {
        messageClient.removeListener(this)
        channelClient.unregisterChannelCallback(channelCallback)
        super.onDestroy()
    }

    // Fires when a service is started up
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            "start" -> {
                Log.d(LOG_TAG, "received start command")
            }
            "stop" -> {
                Log.d(LOG_TAG, "received stop command")
            }
            else -> {
                Log.d(LOG_TAG, "received unknown command")
            }
        }
    }

    // Binding is another way to communicate between service and activity
    // Not needed here, local broadcasts will be used instead
    override fun onBind(intent: Intent) = null

    private fun startStreaming(inputStream: InputStream) {
        streamingThread?.interrupt()
        // create a new streaming thread and run it
        streamingThread = SensorDataStreamingThread(inputStream).apply { start() }
    }

    // A SensorDataStreamingThread streams sensor data from an input stream and
    inner class SensorDataStreamingThread(private val inputStream: InputStream) : Thread() {
        override fun run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
            try {
                val dataStream = ObjectInputStream(BufferedInputStream(inputStream))
                while (!isInterrupted) {
                    val sensorStreamEvent = dataStream.readObject() as SensorStreamEvent
                    //Log.d(LOG_TAG, "${sensorStreamEvent.timestamp}: [${sensorStreamEvent.values.joinToString()}]")
                    SensorEventManager.handleSensorEvent(sensorStreamEvent)
                }
            } catch (e: IOException) {
                Log.d(LOG_TAG, "Exception during read from stream", e)
            } catch (e: EOFException) {
                Log.d(LOG_TAG, "The stream was closed unexpectedly")
            }
        }
    }
}