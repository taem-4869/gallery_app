package com.taemallah.galleryapp.core.presentation

import com.taemallah.galleryapp.core.domain.Image

data class MainState(
    val images: List<Image> = emptyList(),
    val isVisibleAlertDialog: Boolean = false,
    val isVisibleDisplayScreenUi:  Boolean = true,
)
