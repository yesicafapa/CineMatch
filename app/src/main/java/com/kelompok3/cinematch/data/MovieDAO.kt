package com.kelompok3.cinematch.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDAO {
    // Menyimpan film ke daftar favorit (Jika ID sama, data akan diperbarui)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(movie: FavoriteMovie)

    // Mengambil semua film favorit milik user tertentu secara Real-time
    // Menggunakan Flow agar UI otomatis terupdate saat data di database berubah
    @Query("SELECT * FROM favorites WHERE userId = :uid")
    fun getFavoritesByUser(uid: String): Flow<List<FavoriteMovie>>

    // Menghapus film dari daftar favorit berdasarkan objek FavoriteMovie
    @Delete
    suspend fun deleteFavorite(movie: FavoriteMovie)

    // Mengecek status favorit film
    // Room mengembalikan Int (0 atau 1) untuk COUNT, yang nanti divalidasi di Repository
    @Query("SELECT COUNT(*) FROM favorites WHERE id = :movieId AND userId = :uid")
    suspend fun isFavorite(movieId: String, uid: String): Int
}