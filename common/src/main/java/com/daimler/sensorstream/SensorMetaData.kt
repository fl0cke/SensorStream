package com.daimler.sensorstream

import android.hardware.Sensor

class SensorMetaData(val type: Int, val name: String, val valueCount: Int, val uncalibrated: Boolean, val axisLabels: Array<String>) {

    companion object {

        // TODO value count bei rotation vector true?

        private val mapping = mapOf(
                Sensor.TYPE_ACCELEROMETER to SensorMetaData(
                        Sensor.TYPE_ACCELEROMETER, "accelerometer", 3, false, arrayOf("X", "Y", "Z")),
                Sensor.TYPE_MAGNETIC_FIELD to SensorMetaData(
                        Sensor.TYPE_MAGNETIC_FIELD, "magnetic_field", 3, false, arrayOf("X", "Y", "Z")),
                Sensor.TYPE_GYROSCOPE to SensorMetaData(
                        Sensor.TYPE_GYROSCOPE, "gyroscope", 3, false, arrayOf("X", "Y", "Z")),
                Sensor.TYPE_PRESSURE to SensorMetaData(
                        Sensor.TYPE_PRESSURE, "pressure", 1, false, arrayOf("P")),
                Sensor.TYPE_GRAVITY to SensorMetaData(
                        Sensor.TYPE_GRAVITY, "gravity", 3, false, arrayOf("X", "Y", "Z")),
                Sensor.TYPE_LINEAR_ACCELERATION to SensorMetaData(
                        Sensor.TYPE_LINEAR_ACCELERATION, "linear_acceleration", 3, false, arrayOf("X", "Y", "Z")),
                Sensor.TYPE_ROTATION_VECTOR to SensorMetaData(
                        Sensor.TYPE_ROTATION_VECTOR, "rotation_vector", 5, false, arrayOf("X", "Y", "Z", "Theta", "Acc")),
                Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED to SensorMetaData(
                        Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED, "magnetic_field_uncalibrated", 6, true, arrayOf("X", "Y", "Z", "Bx", "By", "Bz")),
                Sensor.TYPE_GAME_ROTATION_VECTOR to SensorMetaData(
                        Sensor.TYPE_GAME_ROTATION_VECTOR, "game_rotation_vector", 4, false, arrayOf("X", "Y", "Z", "Theta")),
                Sensor.TYPE_GYROSCOPE_UNCALIBRATED to SensorMetaData(
                        Sensor.TYPE_GYROSCOPE_UNCALIBRATED, "gyroscope_uncalibrated", 6, true, arrayOf("X", "Y", "Z", "Bx", "By", "Bz")),
                Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR to SensorMetaData(
                        Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR, "geomagnetic_rotation_vector", 5, false, arrayOf("X", "Y", "Z", "Theta", "Acc")),
                Sensor.TYPE_HEART_RATE to SensorMetaData(
                        Sensor.TYPE_HEART_RATE, "heart_rate", 1, false, arrayOf("R")),
                Sensor.TYPE_ACCELEROMETER_UNCALIBRATED to SensorMetaData(
                        Sensor.TYPE_ACCELEROMETER_UNCALIBRATED, "acceleration_uncalibrated", 6, true, arrayOf("X", "Y", "Z", "Bx", "By", "Bz"))
        )


        fun forType(sensorType: Int): SensorMetaData {
            return mapping[sensorType]!!
        }

        val all
            get() = mapping.values

    }

}