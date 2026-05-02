package com.kelompok3.cinematch.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize // Memungkinkan data dikirim antar Activity/Fragment dengan mudah
@Entity(tableName = "favorites")
data class FavoriteMovie(
    @PrimaryKey
    val id: String,          // ID unik dari Document Firestore
    val userId: String,      // ID User dari Firebase Auth
    val title: String,
    val category: String,
    val description: String,
    val rating: Double,
    val imageUrl: String,
    val trailerUrl: String
) : Parcelable