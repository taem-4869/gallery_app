package com.taemallah.galleryapp.core.domain

import android.net.Uri
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date


data class Image(
    val id: Long,
    val name: String,
    val dateTaken: Long,
    val uri: Uri,
){
    fun getFormattedDateTaken(): String {
        return Date(dateTaken).toString()
    }
}