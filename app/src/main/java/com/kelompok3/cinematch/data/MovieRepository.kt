package com.kelompok3.cinematch.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class MovieRepository(
    private val movieDao: MovieDAO,
    private val firestore: FirebaseFirestore
) {

    // --- LOGIKA ROOM (OFFLINE & FAVORIT) ---

    fun getFavoritesByUser(userId: String): Flow<List<FavoriteMovie>> {
        return movieDao.getFavoritesByUser(userId)
    }

    suspend fun addMovieToFavorite(movie: FavoriteMovie) {
        movieDao.insertFavorite(movie)
    }

    suspend fun removeMovieFromFavorite(movie: FavoriteMovie) {
        movieDao.deleteFavorite(movie)
    }

    suspend fun isFavorite(movieId: String, userId: String): Boolean {
        return movieDao.isFavorite(movieId, userId) > 0
    }

    // --- LOGIKA FIREBASE (ONLINE - MOVIES) ---

    suspend fun getMoviesFromFirestore(): List<FavoriteMovie> {
        return try {
            val snapshot = firestore.collection("movies").get().await()
            snapshot.toObjects(FavoriteMovie::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- LOGIKA USER PROFILE (BARU - ONLINE & MULTI USER) ---

    /**
     * Mengambil data profil user berdasarkan UID dari Firestore.
     */
    suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            document.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Menyimpan atau memperbarui data profil user di Firestore.
     * Memenuhi kriteria Online & Multi-user (Poin 1 & 3 UAS).
     */
    suspend fun updateUserProfile(userId: String, name: String, email: String) {
        try {
            val userProfile = UserProfile(uid = userId, name = name, email = email)
            firestore.collection("users").document(userId)
                .set(userProfile, com.google.firebase.firestore.SetOptions.merge())
                .await()
        } catch (e: Exception) {
            throw e
        }
    }
    private val db = FirebaseFirestore.getInstance()

    fun getNotifications(
        onResult: (List<NotificationItem>) -> Unit
    ) {

        db.collection("notifications")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->

                if (snapshot != null) {

                    val notifications = snapshot.documents.map { doc ->

                        NotificationItem(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            message = doc.getString("message") ?: "",
                            movieId = doc.getString("movieId") ?: "",
                            movieTitle = doc.getString("movieTitle") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            category = doc.getString("category") ?: "",
                            type = doc.getString("type") ?: "",
                            isRead = doc.getBoolean("isRead") ?: false
                        )
                    }

                    onResult(notifications.reversed())
                }
            }
    }
}