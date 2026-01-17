package com.oatrice.jarwise.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.oatrice.jarwise.data.repository.SlipRepository
import com.oatrice.jarwise.data.service.SlipDetectorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

import com.oatrice.jarwise.data.model.DetectedSlip

class SlipViewModel(
    private val repository: SlipRepository,
    private val slipDetector: SlipDetectorService
) : ViewModel() {

    private val _buckets = MutableStateFlow<List<SlipRepository.ImageBucket>>(emptyList())
    val buckets: StateFlow<List<SlipRepository.ImageBucket>> = _buckets.asStateFlow()

    private val _selectedBucketId = MutableStateFlow<String?>(null)
    val selectedBucketId: StateFlow<String?> = _selectedBucketId.asStateFlow()

    private val _recentImages = MutableStateFlow<List<DetectedSlip>>(emptyList())
    val recentImages: StateFlow<List<DetectedSlip>> = _recentImages.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanStats = MutableStateFlow("")
    val scanStats: StateFlow<String> = _scanStats.asStateFlow()


    init {
        loadImages(null)
        loadBuckets()
        observeChanges()
    }

    private fun loadBuckets() {
        viewModelScope.launch(Dispatchers.IO) {
            _buckets.value = repository.getImageBuckets()
        }
    }

    fun selectBucket(bucketId: String?) {
        _selectedBucketId.value = bucketId
        loadImages(bucketId)
    }



    private fun loadImages(bucketId: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _isScanning.value = true
            _scanStats.value = "Scanning..."
            
            val images = if (bucketId != null) {
                repository.getImagesInBucket(bucketId, limit = 1000)
            } else {
                repository.getRecentImages(limit = 50)
            }

            var foundCount = 0
            val totalCount = images.size
            
            // Check each image for Slip properties
            val slipList = images.mapIndexedNotNull { index, uri ->
                 val result = slipDetector.detectSlip(uri)
                 val fileName = repository.getFileName(uri) ?: "Unknown"
                 android.util.Log.d("SlipCheck", "Checked [$fileName] -> IsSlip: ${result.isSlip}, Type: ${result.detectedType}, Conf: ${result.confidence}")
                 
                 // Update progress UI
                 _scanStats.value = "Scanning: ${index + 1} / $totalCount"

                 if (result.isSlip) {
                     foundCount++
                     DetectedSlip(uri, result)
                 } else {
                     null
                 }
            }
            
            _recentImages.value = slipList
            _scanStats.value = "Scanned: $totalCount, Slips Found: $foundCount"
            _isScanning.value = false
        }
    }
    
    private fun observeChanges() {
        viewModelScope.launch {
            repository.observeNewImages().collect {
                // When a new image is detected, refresh current view
                loadImages(_selectedBucketId.value)
                loadBuckets() // Also refresh buckets
            }
        }
    }
    
    fun refreshImages() {
        loadImages(_selectedBucketId.value)
        loadBuckets()
    }

    class Factory(
        private val repository: SlipRepository,
        private val slipDetector: SlipDetectorService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SlipViewModel::class.java)) {
                return SlipViewModel(repository, slipDetector) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
