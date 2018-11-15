package com.daimler.sensorstream

import android.hardware.Sensor
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.daimler.sensorstream.service.SensorEventStreamingService
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.lang.IllegalArgumentException

class MainActivity : AppCompatActivity() {

    companion object {
        const val LOG_TAG = "MainActivity"
    }

    private val messageClient by lazy(LazyThreadSafetyMode.NONE) {
        Wearable.getMessageClient(this)
    }
    private val nodeClient by lazy(LazyThreadSafetyMode.NONE) {
        Wearable.getNodeClient(this)
    }
    private val channelClient by lazy(LazyThreadSafetyMode.NONE) {
        Wearable.getChannelClient(this)
    }
    private var streamingThread: Thread? = null

    private val channelCallback = object : ChannelClient.ChannelCallback() {
        override fun onChannelClosed(channel: ChannelClient.Channel, p1: Int, p2: Int) {
            Log.d(LOG_TAG, "Channel Callback: channel closed")
            onStreamClosed()
        }

        override fun onChannelOpened(channel: ChannelClient.Channel) {
            Log.d(LOG_TAG, "Channel Callback: channel opened")
        }

        override fun onInputClosed(p0: ChannelClient.Channel, p1: Int, p2: Int) {
            Log.d(LOG_TAG, "Channel Callback: input closed")
        }

        override fun onOutputClosed(p0: ChannelClient.Channel, p1: Int, p2: Int) {
            Log.d(LOG_TAG, "Channel Callback: output closed")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_start.setOnClickListener(::onStartButtonClicked)
        btn_stop.setOnClickListener(::onStopButtonClicked)
        channelClient.registerChannelCallback(channelCallback)
    }

    override fun onDestroy() {
        channelClient.unregisterChannelCallback(channelCallback)
        super.onDestroy()
    }

    private fun onStartButtonClicked(view: View) {
        // check if the recording name is blank
        if (recording_name.text.isBlank()) {
            Toast.makeText(this, "Please enter a recording name", Toast.LENGTH_SHORT).show()
            return
        }
        disableAllInputs()
        nodeClient.connectedNodes.addOnSuccessListener {
            val node = it.firstOrNull { it.isNearby }
            if (node == null) {
                Toast.makeText(this, "No nearby node found", Toast.LENGTH_SHORT).show()
                btn_start.isEnabled = true
            } else {
                openChannel(node)
            }
        }.addOnFailureListener {
            Log.d(LOG_TAG, "Unable to get connected nodes", it)
            btn_start.isEnabled = true
            Toast.makeText(this, "Unable to start streaming", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStopButtonClicked(view: View) {
        // interrupting the thread causes the channel to be closed automatically
        // any UI related actions are executed in the onChannelClosed callback
        streamingThread?.interrupt()
        streamingThread = null
    }

    private fun openChannel(node: Node) {
        val selectedSensorTypes = getSelectedSensorTypes()
        val path = selectedSensorTypes.joinToString(separator = "+")
        channelClient.openChannel(
                node.id,
                path
        ).addOnFailureListener {
            Log.d(LOG_TAG, "failed to open channel to node ${node.id}")
            enableAllInputs()
        }.addOnSuccessListener {
            Log.d(LOG_TAG, "successfully opened channel to node ${node.id}")
            startStreaming(selectedSensorTypes, it)
        }
    }

    private fun startStreaming(selectedSensorTypes: List<Int>, channel: ChannelClient.Channel) {
        val recordingName = recording_name.text.toString()
        val recordingTime = System.currentTimeMillis()
        val directory = File(getExternalFilesDir(null), "${recordingName}_$recordingTime")
        directory.mkdir()

        // create a new streaming thread and run it
        streamingThread?.interrupt()
        streamingThread = SensorDataStreamingThread(selectedSensorTypes, directory, channel).apply { start() }

        val fragment = when (display_mode.selectedItemPosition) {
            0 -> {
                LivePreviewFragment.newInstance(selectedSensorTypes)
            }
            1 -> {
                LivePreviewFragment.newInstance(selectedSensorTypes)

            }
            2 -> {
                LivePreviewFragment.newInstance(selectedSensorTypes)

            }
            3 -> {
                LivePreviewFragment.newInstance(selectedSensorTypes)
            }
            else -> throw IllegalArgumentException()
        }
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment, "asd").commitNow()
        showAndEnableStopButton()
    }

    private fun onStreamClosed() {
        // remove the fragment
        enableAllInputs()
        showAndEnableStartButton()
        supportFragmentManager.fragments.forEach {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun getSelectedSensorTypes(): List<Int> {
        val selectedSensorTypes: MutableList<Int> = mutableListOf()
        if (sensor_accelerometer.isChecked) {
            selectedSensorTypes.add(Sensor.TYPE_ACCELEROMETER)
        }
        if (sensor_gyro.isChecked) {
            selectedSensorTypes.add(Sensor.TYPE_GYROSCOPE)
        }
        if (sensor_magnetic_field.isChecked) {
            selectedSensorTypes.add(Sensor.TYPE_MAGNETIC_FIELD)
        }
        if (sensor_pressure.isChecked) {
            selectedSensorTypes.add(Sensor.TYPE_PRESSURE)
        }
        if (sensor_gravity.isChecked) {
            selectedSensorTypes.add(Sensor.TYPE_GRAVITY)
        }
        if (sensor_linear_acceleration.isChecked) {
            selectedSensorTypes.add(Sensor.TYPE_LINEAR_ACCELERATION)
        }
        if (sensor_rotation_vector.isChecked) {
            selectedSensorTypes.add(Sensor.TYPE_ROTATION_VECTOR)
        }
        if (sensor_game_rotation_vector.isChecked) {
            selectedSensorTypes.add(Sensor.TYPE_GAME_ROTATION_VECTOR)
        }
        if (sensor_geomagnetic_rotation_vector.isChecked) {
            selectedSensorTypes.add(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
        }
        return selectedSensorTypes
    }

    private fun disableAllInputs() {
        // disable all checkboxes
        sensor_accelerometer.isEnabled = false
        sensor_gyro.isEnabled = false
        sensor_magnetic_field.isEnabled = false
        sensor_pressure.isEnabled = false
        sensor_gravity.isEnabled = false
        sensor_linear_acceleration.isEnabled = false
        sensor_game_rotation_vector.isEnabled = false
        sensor_rotation_vector.isEnabled = false
        sensor_geomagnetic_rotation_vector.isEnabled = false
        // disable the other inputs
        recording_name.isEnabled = false
        display_mode.isEnabled = false
        btn_start.isEnabled = false
    }

    private fun enableAllInputs() {
        // disable all checkboxes
        sensor_accelerometer.isEnabled = true
        sensor_gyro.isEnabled = true
        sensor_magnetic_field.isEnabled = true
        sensor_pressure.isEnabled = true
        sensor_gravity.isEnabled = true
        sensor_linear_acceleration.isEnabled = true
        sensor_game_rotation_vector.isEnabled = true
        sensor_rotation_vector.isEnabled = true
        sensor_geomagnetic_rotation_vector.isEnabled = true
        // disable the other inputs
        recording_name.isEnabled = true
        display_mode.isEnabled = true
        btn_start.isEnabled = true
    }

    private fun showAndEnableStartButton() {
        btn_start.visibility = View.VISIBLE
        btn_start.isEnabled = true
        btn_stop.visibility = View.GONE
    }

    private fun showAndEnableStopButton() {
        btn_stop.visibility = View.VISIBLE
        btn_stop.isEnabled = true
        btn_start.visibility = View.GONE
    }

    // A SensorDataStreamingThread streams sensor data from an input stream and
    inner class SensorDataStreamingThread(
            private val selectedSensorTypes: List<Int>,
            private val directory: File,
            private val channel: ChannelClient.Channel) : Thread() {
        override fun run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
            val inputStream = Tasks.await(channelClient.getInputStream(channel))
            val dataStream = ObjectInputStream(BufferedInputStream(inputStream))
            val fileWriters = HashMap<Int, PrintWriter>()
            try {
                selectedSensorTypes.associateTo(fileWriters) {
                    val sensorName = SensorMetaData.forType(it).name
                    val file = File(directory, "$sensorName.csv")
                    file.createNewFile()
                    it to file.printWriter()
                }
                while (!isInterrupted) {
                    val sensorDataEvent = dataStream.readObject() as SensorDataEvent
                    // write the sensor data to a file
                    val printWriter = fileWriters[sensorDataEvent.sensorType]!!
                    printWriter.print(sensorDataEvent.timestamp)
                    printWriter.print(SensorEventStreamingService.STRING_SEPARATOR)
                    sensorDataEvent.values.joinTo(printWriter, separator = SensorEventStreamingService.STRING_SEPARATOR, postfix = "\n")
                    // broadcast the message to the UI
                    Log.d(LOG_TAG, "asda")
                    SensorEventManager.handleSensorDataEvent(sensorDataEvent)
                }

            } catch (e: IOException) {
                if (e !is EOFException)
                    Log.d(SensorEventStreamingService.LOG_TAG, "Exception during IO operation", e)
            } catch (e: SecurityException) {
                Log.d(SensorEventStreamingService.LOG_TAG, "Exception during file creation", e)
            }

            // close everything
            dataStream.close()
            fileWriters.values.forEach {
                it.close()
            }
            channelClient.close(channel)
        }
    }
}
