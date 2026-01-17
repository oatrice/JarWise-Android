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

class SlipViewModel(
    private val repository: SlipRepository,
    private val slipDetector: SlipDetectorService
) : ViewModel() {

    private val _buckets = MutableStateFlow<List<SlipRepository.ImageBucket>>(emptyList())
    val buckets: StateFlow<List<SlipRepository.ImageBucket>> = _buckets.asStateFlow()

    private val _selectedBucketId = MutableStateFlow<String?>(null)
    val selectedBucketId: StateFlow<String?> = _selectedBucketId.asStateFlow()

    private val _recentImages = MutableStateFlow<List<Uri>>(emptyList())
    val recentImages: StateFlow<List<Uri>> = _recentImages.asStateFlow()

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
            val images = if (bucketId != null) {
                repository.getImagesInBucket(bucketId)
            } else {
                repository.getRecentImages()
            }
            // TODO: filtering logic (SlipDetector) will go here later
            _recentImages.value = images
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
