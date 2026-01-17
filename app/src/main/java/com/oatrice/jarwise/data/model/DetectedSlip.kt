package com.oatrice.jarwise.data.model

import android.net.Uri
import com.oatrice.jarwise.data.service.SlipDetectionResult

data class DetectedSlip(
    val uri: Uri,
    val result: SlipDetectionResult
)
