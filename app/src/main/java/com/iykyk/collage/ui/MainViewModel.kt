package com.iykyk.collage.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iykyk.collage.collage.CollageRenderer
import com.iykyk.collage.model.LayoutTemplate
import com.iykyk.collage.model.PersonIdentity
import com.iykyk.collage.model.PipelineStage
import com.iykyk.collage.model.ProcessingProgress
import com.iykyk.collage.model.CollageResult
import com.iykyk.collage.processor.VideoProcessorRepository
import com.iykyk.collage.util.MediaStoreUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MainUiState(
    val selectedVideoUri: Uri? = null,
    val selectedVideoName: String? = null,
    val isProcessing: Boolean = false,
    val progress: ProcessingProgress = ProcessingProgress(),
    val result: CollageResult? = null,
    val savedGalleryUri: Uri? = null,
    val activeAuditPerson: PersonIdentity? = null,
    val toastMessage: String? = null,
    val selectedTemplate: LayoutTemplate = LayoutTemplate.EDITORIAL
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var repository: VideoProcessorRepository? = null
    private var collageRenderer: CollageRenderer? = null

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
        this.collageRenderer = CollageRenderer(context.applicationContext)

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
                    result = collageResult,
                    selectedTemplate = LayoutTemplate.EDITORIAL
                )
            }
        }
    }

    fun changeLayoutTemplate(context: Context, template: LayoutTemplate) {
        val currentResult = _uiState.value.result ?: return
        if (template == _uiState.value.selectedTemplate) return

        val renderer = collageRenderer ?: CollageRenderer(context.applicationContext).also {
            collageRenderer = it
        }

        viewModelScope.launch {
            val newBitmap = withContext(Dispatchers.Default) {
                renderer.renderCollage(
                    identities = currentResult.identities,
                    layoutTemplate = template
                )
            }

            _uiState.update {
                it.copy(
                    selectedTemplate = template,
                    result = currentResult.copy(
                        collageBitmap = newBitmap,
                        layoutTemplate = template
                    ),
                    savedGalleryUri = null
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
