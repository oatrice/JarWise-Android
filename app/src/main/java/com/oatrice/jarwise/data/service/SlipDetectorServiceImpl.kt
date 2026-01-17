package com.oatrice.jarwise.data.service

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException

class SlipDetectorServiceImpl(private val context: Context) : SlipDetectorService {

    private val barcodeScanner by lazy { BarcodeScanning.getClient() }
    private val textRecognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    override suspend fun isSlip(uri: Uri): Boolean {
        try {
            val image = InputImage.fromFilePath(context, uri)

            // 1. Check for QR Code (First Pass) - Fast & High Confidence
            val barcodes = Tasks.await(barcodeScanner.process(image))
            if (barcodes.isNotEmpty()) {
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (SlipDetectionLogic.followsSlipQrPattern(rawValue)) {
                        return true
                    }
                }
            }

            // 2. Check for Keywords (OCR Pass) - Slower but necessary if no QR
            val visionText = Tasks.await(textRecognizer.process(image))
            val fullText = visionText.text
            if (SlipDetectionLogic.containsSlipKeywords(fullText)) {
                return true
            }

            return false

        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
