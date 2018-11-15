package com.daimler.sensorstream

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.opengl.GLSurfaceView
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.Toast
import com.daasuu.camerarecorder.CameraRecorder
import com.daasuu.camerarecorder.CameraRecorderBuilder
import com.daasuu.camerarecorder.LensFacing
import kotlinx.android.synthetic.main.activity_video_capture.*
import kotlinx.android.synthetic.main.view_video_preview.*
import java.io.File

class VideoCaptureActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    companion object {
        const val LOG_TAG = "VideoCaptureActivity"
        const val EXTRA_VIDEO_LOCATION = "VIDEO_LOCATION"
        const val REQUEST_VIDEO_PERMISSIONS = 1
        val VIDEO_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    }

    private var cameraRecorder: CameraRecorder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_capture)

        if (hasPermissionsGranted(VIDEO_PERMISSIONS)) {
            openCamera()
        } else {
            requestVideoPermissions()
        }
    }

    private fun openCamera() {
        val preview = preview_stub.inflate() as GLSurfaceView
        val videoLocation = intent.getStringExtra(EXTRA_VIDEO_LOCATION)
        val videoFilePath = File(videoLocation, "video.mp4").path
        cameraRecorder = CameraRecorderBuilder(this, preview)
                .lensFacing(LensFacing.BACK)
                .build()
        preview.post {
            cameraRecorder?.start(videoFilePath)
        }
    }

    override fun onStop() {
        preview?.onPause()
        cameraRecorder?.let {
            it.stop()
            it.release()
        }
        super.onStop()
    }

    private fun requestVideoPermissions() {
        if (shouldShowRequestPermissionRationale(VIDEO_PERMISSIONS)) {
            //ConfirmationDialog().show(childFragmentManager, FRAGMENT_DIALOG)
        } else {
            requestPermissions(VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        if (requestCode == REQUEST_VIDEO_PERMISSIONS) {
            if (grantResults.size == VIDEO_PERMISSIONS.size) {
                for (result in grantResults) {
                    if (result != PERMISSION_GRANTED) {
                        Toast.makeText(this, "Please grant the permission", Toast.LENGTH_SHORT).show()
                        return
                    }
                }
                openCamera()
            } else {
                Toast.makeText(this, "Please grant all permissions", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun hasPermissionsGranted(permissions: Array<String>) =
            permissions.none {
                checkSelfPermission(it) != PERMISSION_GRANTED
            }

    private fun shouldShowRequestPermissionRationale(permissions: Array<String>) =
            permissions.any { shouldShowRequestPermissionRationale(it) }


}
