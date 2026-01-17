package com.oatrice.jarwise.data.service

import android.net.Uri
import com.oatrice.jarwise.data.model.ParsedSlip

interface SlipDetectorService {
    /**
     * Analyzes the image at the given Uri and determines if it is a transaction slip.
     */
    suspend fun detectSlip(uri: Uri): SlipDetectionResult
}

data class SlipDetectionResult(
    val isSlip: Boolean,
    val confidence: Float = 0f, // 0.0 - 1.0
    val detectedType: SlipType = SlipType.UNKNOWN,
    val rawText: String? = null,
    val parsedData: ParsedSlip? = null
)

enum class SlipType {
    UNKNOWN,
    QR_SLIP,     // Detected via Unique QR Pattern
    OCR_SLIP     // Detected via Keywords
}
