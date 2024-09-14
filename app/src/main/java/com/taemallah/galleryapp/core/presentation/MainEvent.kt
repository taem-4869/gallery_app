package com.taemallah.galleryapp.core.presentation

import com.taemallah.galleryapp.core.domain.Image

sealed class MainEvent {
    data class UpdateImages(val images: List<Image>): MainEvent()
    data class ShowDisplayImageScreen(val position: Int): MainEvent()
    data class DeleteImage(val position: Int) : MainEvent()
    data class SetIsVisibleAlertDialog(val isVisible: Boolean) : MainEvent()
    data object ShowDisplayLayoutUi : MainEvent()
    data object HideDisplayImageScreen: MainEvent()
}