package com.oatrice.jarwise.ui.migration

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oatrice.jarwise.data.repository.MigrationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.oatrice.jarwise.utils.AppLogger

class MigrationViewModel(
    private val repository: MigrationRepository,
    private val logger: AppLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow<MigrationUiState>(MigrationUiState.Idle)
    val uiState: StateFlow<MigrationUiState> = _uiState.asStateFlow()

    private val _mmbakUri = MutableStateFlow<Uri?>(null)
    val mmbakUri: StateFlow<Uri?> = _mmbakUri.asStateFlow()

    private val _xlsUri = MutableStateFlow<Uri?>(null)
    val xlsUri: StateFlow<Uri?> = _xlsUri.asStateFlow()
    
    // File names for display
    private val _mmbakFileName = MutableStateFlow<String?>(null)
    val mmbakFileName: StateFlow<String?> = _mmbakFileName.asStateFlow()
    
    private val _xlsFileName = MutableStateFlow<String?>(null)
    val xlsFileName: StateFlow<String?> = _xlsFileName.asStateFlow()

    fun setMmbakFile(uri: Uri, name: String?) {
        _mmbakUri.value = uri
        _mmbakFileName.value = name
        resetStateIfError()
    }

    fun setXlsFile(uri: Uri, name: String?) {
        _xlsUri.value = uri
        _xlsFileName.value = name
         resetStateIfError()
    }
    
    private fun resetStateIfError() {
        if (_uiState.value is MigrationUiState.Error) {
            _uiState.value = MigrationUiState.Idle
        }
    }

    fun startMigration() {
        val mmbak = _mmbakUri.value
        val xls = _xlsUri.value

        if (mmbak == null || xls == null) {
            _uiState.value = MigrationUiState.Error("Please select both files.")
            logger.e("Migration", "Attempted start without files.")
            return
        }

        viewModelScope.launch {
            _uiState.value = MigrationUiState.Uploading
            logger.d("Migration", "Starting upload. Mmbak: ${_mmbakFileName.value}, Xls: ${_xlsFileName.value}")
            val result = repository.uploadMigrationFiles(mmbak, xls)
            result.onSuccess { response ->
                logger.d("Migration", "API Response: $response")
                if (response.status.equals("success", ignoreCase = true)) {
                    val msg = response.message
                    logger.d("Migration", "Migration Success: $msg")
                    _uiState.value = MigrationUiState.Success(msg)
                } else {
                    val msg = response.message
                    logger.e("Migration", "Migration Logic Error: $msg")
                     _uiState.value = MigrationUiState.Error(msg)
                }
            }.onFailure { error ->
                logger.e("Migration", "Upload failed", error)
                _uiState.value = MigrationUiState.Error(error.message ?: "Unknown error occurred")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = MigrationUiState.Idle
        _mmbakUri.value = null
        _xlsUri.value = null
        _mmbakFileName.value = null
        _xlsFileName.value = null
    }
}

sealed class MigrationUiState {
    data object Idle : MigrationUiState()
    data object Uploading : MigrationUiState()
    data class Success(val message: String) : MigrationUiState()
    data class Error(val message: String) : MigrationUiState()
}
