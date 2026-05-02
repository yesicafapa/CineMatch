package com.kelompok3.cinematch.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Movie(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val description: String = "",
    val rating: Double = 0.0,
    val imageUrl: String = "",
    val trailerUrl: String = "" // Pastikan ini ada untuk fitur trailer
) : Parcelable