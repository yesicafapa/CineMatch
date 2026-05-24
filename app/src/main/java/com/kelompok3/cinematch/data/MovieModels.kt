package com.kelompok3.cinematch.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val category: String = "",
    val description: String = "",
    val rating: Double = 0.0,
    val imageUrl: String = "",
    val trailerUrl: String = ""
) : Parcelable

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = ""
)

data class NotificationItem(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val movieId: String = "",
    val movieTitle: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val type: String = "",
    val isRead: Boolean = false
)