package com.daimler.sensorstream.service

import android.app.Service
import android.content.Intent
import android.util.Log
import com.daimler.sensorstream.*
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import java.io.*
import java.util.concurrent.atomic.AtomicReference


class SensorEventStreamingService : Service(), MessageClient.OnMessageReceivedListener {

    companion object {
        const val LOG_TAG = "SensorEventStreamingService"
        const val STRING_SEPARATOR = ";"
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
            val fileWriters = HashMap<Int, PrintWriter>()
            val dataStream = ObjectInputStream(BufferedInputStream(inputStream))
            try {
                // read an object containing the selected sensors first
                val sensorStreamOpenedEvent = dataStream.readObject() as SensorStreamOpenedEvent

                // create the folder structure
                val dir = File(getExternalFilesDir(null),
                        "${sensorStreamOpenedEvent.recordingName}_${System.currentTimeMillis() / 1000}")
                dir.mkdir()

                // open files
                sensorStreamOpenedEvent.selectedSensorTypes.associateTo(fileWriters) {
                    val file = File(dir, "$it.csv")
                    file.createNewFile()
                    it to file.printWriter()
                }

                startDisplayActivity(sensorStreamOpenedEvent)

                while (!isInterrupted) {
                    val sensorDataEvent = dataStream.readObject() as SensorDataEvent
                    // write the sensor data to a file
                    val printWriter = fileWriters[sensorDataEvent.sensorType]!!
                    printWriter.print(sensorDataEvent.timestamp)
                    printWriter.print(STRING_SEPARATOR)
                    sensorDataEvent.values.joinTo(printWriter, separator = STRING_SEPARATOR, postfix = "\n")
                    // broadcast the message to the UI
                    Log.d(LOG_TAG, SensorEventManager.observer.toString())
                    SensorEventManager.handleSensorDataEvent(sensorDataEvent)
                }

            } catch (e: IOException) {
                if (e !is EOFException)
                    Log.d(LOG_TAG, "Exception during IO operation", e)
            } catch (e: SecurityException) {
                Log.d(LOG_TAG, "Exception during file creation", e)
            }

            // close everything
            dataStream.close()
            fileWriters.values.forEach {
                it.close()
            }

            // TODO: broadcast? bound service? observer?
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)

        }
    }

    private fun startDisplayActivity(sensorStreamOpenedEvent: SensorStreamOpenedEvent) {
        val intent = when (sensorStreamOpenedEvent.displayMode) {
            DisplayMode.NOTHING -> Intent(applicationContext, PlaceholderActivity::class.java)
            DisplayMode.LIVE_PREVIEW ->
                Intent(applicationContext, LivePreviewActivity::class.java).apply {
                    putExtra(LivePreviewActivity.EXTRA_SENSOR_TYPES, sensorStreamOpenedEvent.selectedSensorTypes)
                }
            DisplayMode.TAGGING ->
                Intent(applicationContext, TaggingActivity::class.java).apply {
                }
            DisplayMode.VIDEO_CAPTURE ->
                Intent(applicationContext, VideoCaptureActivity::class.java).apply {
                }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}