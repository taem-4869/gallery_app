package com.taemallah.galleryapp.core.presentation

import android.provider.MediaStore
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taemallah.galleryapp.core.domain.Image
import com.taemallah.galleryapp.utils.Routs
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class MainViewModel: ViewModel() {
    private val _mediaStoreGeneration = MutableStateFlow(0L)
    private val _images = MutableStateFlow(emptyList<Image>())
    private val _state = MutableStateFlow(MainState())
    val state = combine(_images, _state){images, state ->
        state.copy(
            images = images
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), MainState())

    private val _eventsChannel = Channel<ChannelEvent>()
    val eventsChannel = _eventsChannel.receiveAsFlow()

    fun onEvent(event: MainEvent){
        when(event){
            is MainEvent.ShowDisplayImageScreen -> {
                viewModelScope.launch {
                    _eventsChannel.send(ChannelEvent.Navigate(Routs.DisplayScreen(event.position))).also {
                        showDisplayLayoutUi()
                    }
                }
            }
            is MainEvent.UpdateImages -> {
                _images.update { event.images }
            }
            MainEvent.HideDisplayImageScreen -> {
                viewModelScope.launch {
                    _eventsChannel.send(ChannelEvent.BackRequest)
                }
            }
            is MainEvent.DeleteImage -> {
                viewModelScope.launch {
                    _eventsChannel.send(ChannelEvent.DeleteImage(_images.value[event.position]))
                }
            }
            is MainEvent.SetIsVisibleAlertDialog -> {
                _state.update { it.copy(isVisibleAlertDialog = event.isVisible) }
            }
            is MainEvent.ShowDisplayLayoutUi -> {
                showDisplayLayoutUi()
            }
        }
    }

    private var isVisibleDisplayLayoutUiTimer = Timer()
    private fun showDisplayLayoutUi(){
        _state.update { it.copy(isVisibleDisplayScreenUi = true) }
        isVisibleDisplayLayoutUiTimer.cancel()
        isVisibleDisplayLayoutUiTimer = Timer()
        isVisibleDisplayLayoutUiTimer.schedule(
            object : TimerTask(){
                override fun run() {
                    _state.update { it.copy(isVisibleDisplayScreenUi = false) }
                }
            },
            3000
        )
    }

    fun isMediaStoreGenerationChanged(generation: Long): Boolean {
        if (_mediaStoreGeneration.value!=generation){
            _mediaStoreGeneration.update { generation }
            return true
        }else{
            return false
        }
    }
}
