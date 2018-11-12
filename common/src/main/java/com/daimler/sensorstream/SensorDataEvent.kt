package com.daimler.sensorstream

import java.io.Serializable

data class SensorDataEvent(val sensorType: Int, val timestamp: Long, val values: FloatArray) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SensorDataEvent

        if (sensorType != other.sensorType) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sensorType
        result = 31 * result + timestamp.hashCode()
        return result
    }
}