package com.aria.assistant.ui.screens.music

import androidx.lifecycle.ViewModel
import com.aria.assistant.music.MusicController
import com.aria.assistant.music.MusicOpenResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val musicController: MusicController
) : ViewModel() {
    fun playPause() = musicController.playPause()
    fun next() = musicController.next()
    fun previous() = musicController.previous()
    fun search(query: String): MusicOpenResult = musicController.searchAndOpen(query)
}
