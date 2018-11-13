package com.daimler.sensorstream

import java.io.Serializable

class SensorStreamOpenedEvent(val selectedSensorTypes: IntArray, val recordingName: String, val showRealtimePlot: Boolean) : Serializable