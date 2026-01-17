package com.oatrice.jarwise.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.oatrice.jarwise.data.repository.SlipRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SlipViewModel(private val repository: SlipRepository) : ViewModel() {

    private val _recentImages = MutableStateFlow<List<Uri>>(emptyList())
    val recentImages: StateFlow<List<Uri>> = _recentImages.asStateFlow()

    init {
        loadRecentImages()
        observeChanges()
    }

    private fun loadRecentImages() {
        viewModelScope.launch {
            _recentImages.value = repository.getRecentImages()
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

    class Factory(private val repository: SlipRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SlipViewModel::class.java)) {
                return SlipViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
