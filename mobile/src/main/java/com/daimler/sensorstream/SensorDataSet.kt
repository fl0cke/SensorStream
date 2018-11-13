package com.daimler.sensorstream

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import kotlin.collections.ArrayList

class SensorDataSet(label: String, private val maxEntryCount: Int) : LineDataSet(ArrayList<Entry>(), label) {

    private var pooledEntry: Entry? = null

    fun addSensorReading(x: Float, y: Float) {
        val entry = pooledEntry ?: Entry()
        entry.x = x
        entry.y = y

        if (mValues.size == maxEntryCount) {
            pooledEntry = (mValues as ArrayList).removeAt(0)
        }

        mValues.add(entry)
        calcMinMax()
    }

    override fun clear() {
        pooledEntry = null
        super.clear()
    }

}