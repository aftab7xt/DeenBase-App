package com.deenbase.app.update

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deenbase.app.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UpdateUiState(
    val showDialog: Boolean = false,
    val latestVersion: String = "",
    val apkDownloadUrl: String = "",
    val isDownloading: Boolean = false,
    val downloadId: Long = -1L
)

class UpdateViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(UpdateUiState())
    val state: StateFlow<UpdateUiState> = _state.asStateFlow()

    init {
        checkForUpdate()
    }

    private fun checkForUpdate() {
        viewModelScope.launch {
            val info = UpdateChecker.checkForUpdate(BuildConfig.VERSION_NAME)
            if (info != null) {
                _state.value = UpdateUiState(
                    showDialog = true,
                    latestVersion = info.latestVersion,
                    apkDownloadUrl = info.apkDownloadUrl
                )
            }
        }
    }

    fun dismissDialog() {
        _state.value = _state.value.copy(showDialog = false)
    }

    fun startDownload(context: Context) {
        val url = _state.value.apkDownloadUrl
        if (url.isBlank()) return

        _state.value = _state.value.copy(isDownloading = true, showDialog = false)

        val fileName = "DeenBase-v${_state.value.latestVersion}.apk"

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("DeenBase Update")
            .setDescription("Downloading v${_state.value.latestVersion}...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setMimeType("application/vnd.android.package-archive")

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = dm.enqueue(request)
        _state.value = _state.value.copy(downloadId = downloadId)
    }
}
