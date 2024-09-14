package com.taemallah.galleryapp.utils

import kotlinx.serialization.Serializable

sealed class Routs {
    @Serializable
    data object MainScreen: Routs()
    @Serializable
    data class DisplayScreen(val startPosition: Int): Routs()
}