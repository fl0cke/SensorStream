package com.daimler.sensorstream

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import android.os.Handler
import android.os.Looper


object SensorEventManager : LifecycleObserver {

    interface SensorDataObserver {
        fun onSensorDataReceived(sensorDataEvent: SensorDataEvent)
    }

    var mainHandler = Handler(Looper.getMainLooper())

    var observer: SensorDataObserver? = null

    fun handleSensorDataEvent(sensorDataEvent: SensorDataEvent) {
        // TODO: geht das auch irgendwie besser?
        mainHandler.post {
            observer?.onSensorDataReceived(sensorDataEvent)
        }
    }

}