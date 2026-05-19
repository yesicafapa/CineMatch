package com.kelompok3.cinematch.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "movies") // Tambahkan ini untuk poin Persistence Room
data class Movie(
    @PrimaryKey val id: String = "", // ID dari Firestore atau API menjadi Primary Key di Room
    val title: String = "",
    val category: String = "",
    val description: String = "",
    val rating: Double = 0.0,
    val imageUrl: String = "",
    val trailerUrl: String = ""
) : Parcelable

// Tambahkan Data Class khusus User untuk Edit Profile (Poin Multi-user & Online)
data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "" // Tambahkan ini agar sinkron dengan EditProfile
)