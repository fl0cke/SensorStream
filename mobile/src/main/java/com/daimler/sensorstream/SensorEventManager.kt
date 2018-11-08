package com.daimler.sensorstream

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.os.Handler
import android.os.Looper


object SensorEventManager {

    interface Observer {
        fun onSensorEventReceived(sensorEvent: SensorStreamEvent)
    }

    var mainHandler = Handler(Looper.getMainLooper())

    private var observer: Observer? = null

    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer) {
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            // ignore
            return
        }

        // TODO handle lifecycle
        this.observer = observer
    }

    fun handleSensorEvent(sensorEvent: SensorStreamEvent) {
        // TODO: geht das auch irgendwie besser?
        mainHandler.post {
            observer?.onSensorEventReceived(sensorEvent)
        }
    }

}