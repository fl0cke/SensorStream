package com.daimler.sensorstream

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import kotlinx.android.synthetic.main.activity_video_capture.*

class VideoCaptureActivity : AppCompatActivity() {

    companion object {
        const val LOG_TAG = "VideoCaptureActivity"
        const val EXTRA_VIDEO_LOCATION = "VIDEO_LOCATION"
        const val REQUEST_VIDEO_CAPTURE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val videoLocation = intent.getStringExtra(EXTRA_VIDEO_LOCATION)

        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            takeVideoIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Log.d(LOG_TAG, intent.data?.toString())
        }
    }

}
