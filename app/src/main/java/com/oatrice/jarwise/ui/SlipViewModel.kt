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

    private val _recentImages = MutableStateFlow<List<Uri>>(emptyList())
    val recentImages: StateFlow<List<Uri>> = _recentImages.asStateFlow()

    init {
        loadRecentImages()
        observeChanges()
    }

    private fun loadRecentImages() {
        viewModelScope.launch(Dispatchers.IO) {
            val allImages = repository.getRecentImages()
            // Temporarily disable strict filtering to verify permission fix
            // Just log the detection result for now
            // val slipImages = allImages.filter { uri -> slipDetector.isSlip(uri) }
            
            _recentImages.value = allImages
        }
    }
    
    private fun observeChanges() {
        viewModelScope.launch {
            repository.observeNewImages().collect {
                // When a new image is detected, refresh the whole list
                loadRecentImages()
            }
        }
    }
    
    fun refreshImages() {
        loadRecentImages()
    }

    fun addSelectedImages(uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentList = _recentImages.value
            _recentImages.value = uris + currentList
        }
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
