package com.kelompok3.cinematch.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class MovieRepository(
    private val movieDao: MovieDAO,       // Untuk akses Room (Lokal)
    private val firestore: FirebaseFirestore // Untuk akses Firebase (Cloud)
) {

    // --- LOGIKA ROOM (OFFLINE & FAVORIT) ---

    /**
     * Mengambil daftar film favorit dari database lokal Room.
     * Menggunakan Flow agar UI otomatis terupdate secara real-time.
     */
    fun getFavoritesByUser(userId: String): Flow<List<FavoriteMovie>> {
        return movieDao.getFavoritesByUser(userId)
    }

    /**
     * Menambahkan film ke dalam database lokal Room.
     */
    suspend fun addMovieToFavorite(movie: FavoriteMovie) {
        movieDao.insertFavorite(movie)
    }

    /**
     * Menghapus film dari daftar favorit di database lokal Room.
     */
    suspend fun removeMovieFromFavorite(movie: FavoriteMovie) {
        movieDao.deleteFavorite(movie)
    }

    /**
     * Mengecek status favorit film.
     * Mengonversi hasil Int dari DAO (0 atau 1) menjadi Boolean (True atau False).
     */
    suspend fun isFavorite(movieId: String, userId: String): Boolean {
        // Fix: Tambahkan pengecekan > 0 untuk mencocokkan tipe data Boolean
        return movieDao.isFavorite(movieId, userId) > 0
    }

    // --- LOGIKA FIREBASE (ONLINE) ---

    /**
     * Mengambil data film dari koleksi "movies" di Firestore.
     */
    suspend fun getMoviesFromFirestore(): List<FavoriteMovie> {
        return try {
            val snapshot = firestore.collection("movies").get().await()
            snapshot.toObjects(FavoriteMovie::class.java)
        } catch (e: Exception) {
            // Jika offline atau error, kembalikan list kosong
            emptyList()
        }
    }
}