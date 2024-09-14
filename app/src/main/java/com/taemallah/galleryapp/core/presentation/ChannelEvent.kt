package com.taemallah.galleryapp.core.presentation

import com.taemallah.galleryapp.core.domain.Image
import com.taemallah.galleryapp.utils.Routs

sealed class ChannelEvent {
    data class Navigate(val route : Routs): ChannelEvent()
    data object BackRequest: ChannelEvent()
    data class DeleteImage(val image : Image): ChannelEvent()
}