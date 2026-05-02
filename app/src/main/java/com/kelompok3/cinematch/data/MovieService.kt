package com.kelompok3.cinematch.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MovieService {
    private val db = FirebaseFirestore.getInstance()
    private val collectionName = "movies"

    // 1. Mengambil semua data film (Fix ID)
    suspend fun getAllMovies(): List<Movie> {
        return try {
            val snapshot = db.collection(collectionName).get().await()
            snapshot.documents.mapNotNull { document ->
                Movie(
                    id = document.id, // ID dari Firebase Document
                    title = document.getString("title") ?: "",
                    category = document.getString("category") ?: "",
                    description = document.getString("description") ?: "",
                    rating = document.getDouble("rating") ?: 0.0,
                    imageUrl = document.getString("imageUrl") ?: "",
                    trailerUrl = document.getString("trailerUrl") ?: ""
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 2. Mengambil film berdasarkan kategori
    suspend fun getMoviesByCategory(category: String): List<Movie> {
        return try {
            val querySnapshot = db.collection(collectionName)
                .whereEqualTo("category", category)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                Movie(
                    id = document.id,
                    title = document.getString("title") ?: "",
                    category = document.getString("category") ?: "",
                    description = document.getString("description") ?: "",
                    rating = document.getDouble("rating") ?: 0.0,
                    imageUrl = document.getString("imageUrl") ?: "",
                    trailerUrl = document.getString("trailerUrl") ?: ""
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 3. FUNGSI BARU: Hapus Komentar
    suspend fun deleteComment(movieId: String, commentId: String): Boolean {
        return try {
            db.collection(collectionName).document(movieId)
                .collection("comments").document(commentId)
                .delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 4. FUNGSI BARU: Edit Komentar
    suspend fun updateComment(movieId: String, commentId: String, newText: String): Boolean {
        return try {
            db.collection(collectionName).document(movieId)
                .collection("comments").document(commentId)
                .update("text", newText).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}