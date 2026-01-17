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
    private val parser = SlipParser()

    override suspend fun detectSlip(uri: Uri): SlipDetectionResult {
        try {
            val image = InputImage.fromFilePath(context, uri)

            // 1. Run OCR (Needed for Keywords check AND Data Extraction)
            val visionText = Tasks.await(textRecognizer.process(image))
            val fullText = visionText.text
            android.util.Log.d("SlipCheck", "OCR Text: ${fullText.take(100)}...")

            // 2. Check for QR Code
            val barcodes = Tasks.await(barcodeScanner.process(image))
            var qrCodeContent: String? = null
            var isQrSlip = false

            for (barcode in barcodes) {
                val rawValue = barcode.rawValue
                android.util.Log.d("SlipCheck", "QR Found: $rawValue")
                if (SlipDetectionLogic.followsSlipQrPattern(rawValue)) {
                    isQrSlip = true
                    qrCodeContent = rawValue
                    break // Stop after first valid QR
                }
            }

            // 3. Determine Result
            if (isQrSlip) {
                val parsed = parser.parse(fullText)
                return SlipDetectionResult(
                    isSlip = true,
                    confidence = 1.0f,
                    detectedType = SlipType.QR_SLIP,
                    rawText = qrCodeContent, // Keep QR content as rawText for QR slips? Or prefer fullText? 
                    // Usually rawText in result implied "evidence". Let's keep QR content for QR slip, but add parsedData.
                    parsedData = parsed
                )
            }

            if (SlipDetectionLogic.containsSlipKeywords(fullText)) {
                val parsed = parser.parse(fullText)
                return SlipDetectionResult(
                    isSlip = true,
                    confidence = 0.8f,
                    detectedType = SlipType.OCR_SLIP,
                    rawText = fullText,
                    parsedData = parsed
                )
            }

            return SlipDetectionResult(isSlip = false)

        } catch (e: IOException) {
            e.printStackTrace()
            return SlipDetectionResult(isSlip = false)
        } catch (e: Exception) {
            e.printStackTrace()
            return SlipDetectionResult(isSlip = false)
        }
    }
}
