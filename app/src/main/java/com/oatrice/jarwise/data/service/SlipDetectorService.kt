package com.oatrice.jarwise.data.service

import android.net.Uri

interface SlipDetectorService {
    /**
     * Analyzes the image at the given Uri and determines if it is a transaction slip.
     */
    suspend fun isSlip(uri: Uri): Boolean
}
