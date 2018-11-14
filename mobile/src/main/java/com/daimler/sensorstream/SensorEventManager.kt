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

    private val observers: MutableList<SensorDataObserver> = mutableListOf()

    fun registerObserver(sensorDataObserver: SensorDataObserver) {
        observers.add(sensorDataObserver)
    }

    fun unregisterObserver(sensorDataObserver: SensorDataObserver) {
        observers.remove(sensorDataObserver)
    }

    fun handleSensorDataEvent(sensorDataEvent: SensorDataEvent) {
        // TODO: geht das auch irgendwie besser?
        mainHandler.post {
            observers.forEach { it.onSensorDataReceived(sensorDataEvent) }
        }
    }


}