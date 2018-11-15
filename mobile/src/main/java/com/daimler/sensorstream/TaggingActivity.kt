package com.daimler.sensorstream

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_tagging.*
import java.io.File
import java.io.PrintWriter

class TaggingActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TAG_LOCATION = "EXTRA_TAG_LOCATION"
        const val LOG_TAG = "TaggingActivity"
    }

    private lateinit var tagFileWriter: PrintWriter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tagging)

        val tagLocation = intent.getStringExtra(EXTRA_TAG_LOCATION)
        val file = File(tagLocation, "tags.csv")
        tagFileWriter = PrintWriter(file)

        container.views.forEach {
            if (it is Button) {
                it.setOnClickListener(::onButtonClicked)
            }
        }
    }

    private fun onButtonClicked(view: View) {
        val action = view.tag
        tagFileWriter.print(System.nanoTime())
        tagFileWriter.print(';')
        tagFileWriter.print(action)
        tagFileWriter.print('\n')
    }

    override fun onDestroy() {
        tagFileWriter.close()
        super.onDestroy()
    }
}
