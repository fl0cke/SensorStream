package com.daimler.sensorstream

import java.io.Serializable

enum class DisplayMode : Serializable {
    NOTHING,
    LIVE_PREVIEW,
    TAGGING,
    VIDEO_CAPTURE,
}