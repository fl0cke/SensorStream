package com.daimler.sensorstream

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.wearable.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.lang.IllegalArgumentException

class MainActivity : WearableActivity(), SensorEventListener {
    companion object {
        const val LOG_TAG = "WEAR"
        const val CAPABILITY_STREAM_SENSOR_DATA = "stream_sensor_data"
    }

    private val channelClient by lazy(LazyThreadSafetyMode.NONE) {
        Wearable.getChannelClient(this)
    }
    private val capabilityClient by lazy(LazyThreadSafetyMode.NONE) {
        Wearable.getCapabilityClient(this)
    }
    private val sensorManager by lazy(LazyThreadSafetyMode.NONE) {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }


    private var channel: ChannelClient.Channel? = null
    private var outputStream: ObjectOutputStream? = null
    private var highFreqModeEnabled = false

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
        Log.d(LOG_TAG, "start")
        channelClient.registerChannelCallback(callback)
    }

    override fun onStop() {
        Log.d(LOG_TAG, "stop")
        channelClient.unregisterChannelCallback(callback)
        stopStreaming()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_start.setOnClickListener {
            onStartButtonClicked()
        }
        btn_stop.setOnClickListener {
            stopStreaming()
        }

    }

    private fun onStartButtonClicked() {
        // check if the recording name is blank
        if (recording_name.text.isBlank()) {
            Toast.makeText(this, "Please enter a recording name", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(LOG_TAG, "Starting streaming...")
        btn_start.isEnabled = false
        capabilityClient.getCapability(
                CAPABILITY_STREAM_SENSOR_DATA,
                CapabilityClient.FILTER_REACHABLE
        ).addOnSuccessListener { capabilityInfo ->
            val nearbyNode = capabilityInfo.nodes.firstOrNull { it.isNearby }
            if (nearbyNode == null) {
                Toast.makeText(this, "No nearby node found", Toast.LENGTH_SHORT).show()
                btn_start.isEnabled = true
            } else {
                openChannel(nearbyNode)
            }
        }.addOnFailureListener {
            Log.d(LOG_TAG, "Unable to start streaming", it)
            btn_start.isEnabled = true
            Toast.makeText(this, "Unable to start streaming", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openChannel(node: Node) {
        Log.d(LOG_TAG, "opening channel to node ${node.id}")
        channelClient.openChannel(
                node.id,
                "sensor_data"
        ).addOnFailureListener {
            Log.d(LOG_TAG, "failed to open channel to node ${node.id}")
            btn_start.isEnabled = true
        }.onSuccessTask {
            Log.d(LOG_TAG, "successfully opened channel to node ${node.id}")
            channel = it
            channelClient.getOutputStream(it!!)
        }.addOnSuccessListener {
            Log.d(LOG_TAG, "successfully opened output stream to node ${node.id}")
            outputStream = ObjectOutputStream(
                    if (highFreqModeEnabled) BufferedOutputStream(it)
                    else it)
            startStreaming()
        }.addOnFailureListener {
            Log.d(LOG_TAG, "failed to open output stream to node ${node.id}")
            btn_start.isEnabled = true
        }
    }

    private fun closeChannel() {
        channel?.let {
            outputStream?.close()
            channelClient.close(it)
            channel = null
            outputStream = null
        }
    }

    private fun startStreaming() {
        val displayMode = when (display_mode.selectedItemPosition) {
            0 -> DisplayMode.NOTHING
            1 -> DisplayMode.LIVE_PREVIEW
            2 -> DisplayMode.TAGGING
            3 -> DisplayMode.VIDEO_CAPTURE
            else -> throw IllegalArgumentException()
        }

        // don't enable high frequency mode if we are doing a live preview
        highFreqModeEnabled = displayMode != DisplayMode.LIVE_PREVIEW

        val sensorSelection: MutableList<Int> = mutableListOf()

        // conditionally enable each sensor
        if (sensor_accelerometer.isChecked) {
            sensorSelection.add(Sensor.TYPE_ACCELEROMETER)
            enableSensor(Sensor.TYPE_ACCELEROMETER)
        }
        if (sensor_gyro.isChecked) {
            sensorSelection.add(Sensor.TYPE_GYROSCOPE)
            enableSensor(Sensor.TYPE_GYROSCOPE)
        }
        if (sensor_magnetic_field.isChecked) {
            sensorSelection.add(Sensor.TYPE_MAGNETIC_FIELD)
            enableSensor(Sensor.TYPE_MAGNETIC_FIELD)
        }
        if (sensor_pressure.isChecked) {
            sensorSelection.add(Sensor.TYPE_PRESSURE)
            enableSensor(Sensor.TYPE_PRESSURE)
        }
        if (sensor_gravity.isChecked) {
            sensorSelection.add(Sensor.TYPE_GRAVITY)
            enableSensor(Sensor.TYPE_GRAVITY)
        }
        if (sensor_linear_acceleration.isChecked) {
            sensorSelection.add(Sensor.TYPE_LINEAR_ACCELERATION)
            enableSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        }
        if (sensor_rotation_vector.isChecked) {
            sensorSelection.add(Sensor.TYPE_ROTATION_VECTOR)
            enableSensor(Sensor.TYPE_ROTATION_VECTOR)
        }
        if (sensor_game_rotation_vector.isChecked) {
            sensorSelection.add(Sensor.TYPE_GAME_ROTATION_VECTOR)
            enableSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
        }
        if (sensor_geomagnetic_rotation_vector.isChecked) {
            sensorSelection.add(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
            enableSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
        }
        /*  if (sensor_magnetic_field_uncalibrated.isChecked) {
              sensorSelection.add(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED)
              enableSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED)
          }
          if (sensor_gyroscope_uncalibrated.isChecked) {
              sensorSelection.add(Sensor.TYPE_GYROSCOPE_UNCALIBRATED)
              enableSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED)
          }
          if (sensor_acceleration_uncalibrated.isChecked) {
              sensorSelection.add(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED)
              enableSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED)
          }*/

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
        /*   sensor_acceleration_uncalibrated.isEnabled = false
           sensor_gyroscope_uncalibrated.isEnabled = false
           sensor_magnetic_field_uncalibrated.isEnabled = false*/

        // show the stop button
        btn_start.visibility = View.GONE
        btn_start.isEnabled = true
        btn_stop.visibility = View.VISIBLE

        // disable the other inputs
        recording_name.isEnabled = false
        display_mode.isEnabled = false

        val recordingName = recording_name.text.toString()

        // send the
        outputStream?.writeUnshared(SensorStreamOpenedEvent(
                sensorSelection.toIntArray(), recordingName, displayMode))
    }

    private fun stopStreaming() {
        disableAllSensors()
        closeChannel()

        // enable all checkboxes
        sensor_accelerometer.isEnabled = true
        sensor_gyro.isEnabled = true
        sensor_magnetic_field.isEnabled = true
        sensor_pressure.isEnabled = true
        sensor_gravity.isEnabled = true
        sensor_linear_acceleration.isEnabled = true
        sensor_game_rotation_vector.isEnabled = true
        sensor_rotation_vector.isEnabled = true
        sensor_geomagnetic_rotation_vector.isEnabled = true
        /*sensor_acceleration_uncalibrated.isEnabled = true
        sensor_gyroscope_uncalibrated.isEnabled = true
        sensor_magnetic_field_uncalibrated.isEnabled = true*/

        // show the start button
        btn_start.visibility = View.VISIBLE
        btn_stop.visibility = View.GONE

        // enable the other inputs
        recording_name.isEnabled = true
        display_mode.isEnabled = true
    }

    private fun enableSensor(type: Int) {
        val sensor = sensorManager.getDefaultSensor(type)
        val delay = if (highFreqModeEnabled)
            SensorManager.SENSOR_DELAY_FASTEST else SensorManager.SENSOR_DELAY_UI
        sensorManager.registerListener(this, sensor, delay)
    }

    private fun disableAllSensors() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.d(LOG_TAG, "Accuracy of sensor ${sensor.name} changed to $accuracy")
    }

    override fun onSensorChanged(event: SensorEvent) {
        val sensorStreamEvent = SensorDataEvent(event.sensor.type, event.timestamp, event.values.copyOf())
        println(event.values.joinToString() {
            String.format("%.2f", it)
        })
        try {
            outputStream?.writeUnshared(sensorStreamEvent)
        } catch (e: IOException) {
            Log.d(LOG_TAG, "Unable to send sensor event", e)
        }
    }

}
