package com.kelompok3.cinematch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelompok3.cinematch.data.FavoriteMovie
import com.kelompok3.cinematch.data.MovieRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MovieViewModel(private val repository: MovieRepository) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // State untuk mengecek apakah film sudah favorit atau belum
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    // Mendapatkan daftar favorit hanya untuk user yang sedang login
    fun getFavoriteMovies(): Flow<List<FavoriteMovie>> {
        val userId = auth.currentUser?.uid ?: ""
        return repository.getFavoritesByUser(userId)
    }

    // Fungsi untuk menambah atau menghapus favorit (Toggle)
    fun toggleFavorite(movie: FavoriteMovie) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            viewModelScope.launch {
                val exists = repository.isFavorite(movie.id, userId)
                if (exists) {
                    repository.removeMovieFromFavorite(movie)
                    _isFavorite.value = false
                } else {
                    repository.addMovieToFavorite(movie.copy(userId = userId))
                    _isFavorite.value = true
                }
            }
        }
    }

    // Mengecek status favorit saat masuk ke halaman detail
    fun checkFavoriteStatus(movieId: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            viewModelScope.launch {
                _isFavorite.value = repository.isFavorite(movieId, userId)
            }
        } else {
            _isFavorite.value = false
        }
    }
}