package com.deenbase.app.features.quran.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RecitationState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentVerseKey: String = ""   // "surahId:verseNumber"
)

class RecitationViewModel(application: Application) : AndroidViewModel(application) {

    private val player = ExoPlayer.Builder(application).build().apply {
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.value = _state.value.copy(isPlaying = isPlaying)
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                _state.value = _state.value.copy(
                    isBuffering = playbackState == Player.STATE_BUFFERING
                )
                if (playbackState == Player.STATE_ENDED) {
                    _state.value = _state.value.copy(isPlaying = false, currentVerseKey = "")
                }
            }
        })
    }

    private val _state = MutableStateFlow(RecitationState())
    val state: StateFlow<RecitationState> = _state.asStateFlow()

    fun play(surahId: Int, verseNumber: Int) {
        val verseKey = "$surahId:$verseNumber"
        val current  = _state.value

        // Same verse — toggle pause / resume
        if (current.currentVerseKey == verseKey) {
            if (player.isPlaying) player.pause() else player.play()
            return
        }

        // New verse — load and play
        val url = buildUrl(surahId, verseNumber)
        _state.value = current.copy(currentVerseKey = verseKey, isBuffering = true, isPlaying = false)
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.play()
    }

    fun stop() {
        player.stop()
        _state.value = RecitationState()
    }

    // Instant silence — used when leaving the screen so there's no buffer-flush delay.
    // The player is fully released in onCleared() regardless.
    fun pauseImmediate() {
        player.pause()
        _state.value = _state.value.copy(isPlaying = false)
    }

    private fun buildUrl(surahId: Int, verseNumber: Int): String {
        val s = surahId.toString().padStart(3, '0')
        val v = verseNumber.toString().padStart(3, '0')
        return "https://everyayah.com/data/Alafasy_128kbps/$s$v.mp3"
    }

    override fun onCleared() {
        player.release()
        super.onCleared()
    }
}
