package com.deenbase.app.update

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deenbase.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UpdateUiState(
    val showDialog: Boolean = false,
    val latestVersion: String = "",
    val apkDownloadUrl: String = "",
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0,
    val downloadId: Long = -1L,
    val isChecking: Boolean = false,
    val noUpdateFound: Boolean = false
)

class UpdateViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(UpdateUiState())
    val state: StateFlow<UpdateUiState> = _state.asStateFlow()

    init {
        checkForUpdate(manual = false)
    }

    fun checkForUpdate(manual: Boolean = false) {
        if (_state.value.isChecking) return
        _state.value = _state.value.copy(isChecking = true, noUpdateFound = false)
        viewModelScope.launch {
            val info = UpdateChecker.checkForUpdate(BuildConfig.VERSION_NAME)

            if (info != null) {
                _state.value = _state.value.copy(
                    showDialog = true,
                    latestVersion = info.latestVersion,
                    apkDownloadUrl = info.apkDownloadUrl,
                    isChecking = false,
                    noUpdateFound = false
                )
            } else {
                _state.value = _state.value.copy(
                    isChecking = false,
                    noUpdateFound = manual // only show "up to date" if user asked
                )
            }
        }
    }

    fun dismissDialog() {
        // Prevent dismissal if a download is currently active
        if (!_state.value.isDownloading) {
            _state.value = _state.value.copy(showDialog = false)
        }
    }

    fun dismissNoUpdate() {
        _state.value = _state.value.copy(noUpdateFound = false)
    }

    fun startDownload(context: Context) {
        val url = _state.value.apkDownloadUrl
        if (url.isBlank()) return

        // Keep the dialog open, but switch to the downloading view
        _state.value = _state.value.copy(isDownloading = true, downloadProgress = 0)

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

        trackDownloadProgress(dm, downloadId)
    }

    private fun trackDownloadProgress(dm: DownloadManager, downloadId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            var finishDownload = false
            while (!finishDownload) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = dm.query(query)
                
                if (cursor != null && cursor.moveToFirst()) {
                    val statusCol = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (statusCol >= 0) {
                        when (cursor.getInt(statusCol)) {
                            DownloadManager.STATUS_FAILED -> {
                                finishDownload = true
                                _state.value = _state.value.copy(isDownloading = false)
                            }
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                finishDownload = true
                                // Dismiss dialog automatically when done so the package installer can take over
                                _state.value = _state.value.copy(
                                    isDownloading = false, 
                                    showDialog = false, 
                                    downloadProgress = 100
                                )
                            }
                            DownloadManager.STATUS_RUNNING -> {
                                val totalCol = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                                val downloadedCol = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                                
                                if (totalCol >= 0 && downloadedCol >= 0) {
                                    val total = cursor.getLong(totalCol)
                                    val downloaded = cursor.getLong(downloadedCol)
                                    if (total > 0) {
                                        val progress = ((downloaded * 100L) / total).toInt()
                                        _state.value = _state.value.copy(downloadProgress = progress)
                                    }
                                }
                            }
                        }
                    }
                    cursor.close()
                }
                delay(500) // Poll every half-second
            }
        }
    }
}
