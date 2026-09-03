package com.iykyk.collage.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iykyk.collage.model.PersonIdentity
import com.iykyk.collage.model.PipelineStage
import com.iykyk.collage.model.ProcessingProgress
import com.iykyk.collage.model.CollageResult
import com.iykyk.collage.processor.VideoProcessorRepository
import com.iykyk.collage.util.MediaStoreUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val selectedVideoUri: Uri? = null,
    val selectedVideoName: String? = null,
    val isProcessing: Boolean = false,
    val progress: ProcessingProgress = ProcessingProgress(),
    val result: CollageResult? = null,
    val savedGalleryUri: Uri? = null,
    val activeAuditPerson: PersonIdentity? = null,
    val toastMessage: String? = null
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var repository: VideoProcessorRepository? = null

    fun selectVideo(uri: Uri, name: String? = null) {
        _uiState.update {
            it.copy(
                selectedVideoUri = uri,
                selectedVideoName = name ?: "Selected Video",
                result = null,
                savedGalleryUri = null
            )
        }
    }

    fun startProcessing(context: Context) {
        val uri = _uiState.value.selectedVideoUri ?: return
        val repo = VideoProcessorRepository(context.applicationContext)
        this.repository = repo

        _uiState.update { it.copy(isProcessing = true, result = null) }

        viewModelScope.launch {
            
            launch {
                repo.progress.collect { prog ->
                    _uiState.update { state -> state.copy(progress = prog) }
                }
            }

            val collageResult = repo.processVideo(uri)

            _uiState.update {
                it.copy(
                    isProcessing = false,
                    result = collageResult
                )
            }
        }
    }

    fun saveCollageToGallery(context: Context) {
        val bitmap = _uiState.value.result?.collageBitmap ?: return
        val videoName = _uiState.value.selectedVideoName ?: "collage"
        val cleanName = videoName.replace(" ", "_").lowercase()

        viewModelScope.launch {
            val savedUri = MediaStoreUtils.saveBitmapToGallery(context, bitmap, "cameo_$cleanName")
            if (savedUri != null) {
                _uiState.update { it.copy(savedGalleryUri = savedUri, toastMessage = "Collage saved to Gallery!") }
            } else {
                _uiState.update { it.copy(toastMessage = "Failed to save to Gallery.") }
            }
        }
    }

    fun shareCollage(context: Context) {
        val bitmap = _uiState.value.result?.collageBitmap ?: return
        MediaStoreUtils.shareBitmap(context, bitmap)
    }

    fun setAuditPerson(person: PersonIdentity?) {
        _uiState.update { it.copy(activeAuditPerson = person) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun reset() {
        _uiState.update { MainUiState() }
    }

    override fun onCleared() {
        super.onCleared()
        repository?.release()
    }
}
