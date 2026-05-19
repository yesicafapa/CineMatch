package com.kelompok3.cinematch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth

// Import Data
import com.kelompok3.cinematch.data.AppDatabase
import com.kelompok3.cinematch.data.Movie

// Import UI
import com.kelompok3.cinematch.ui.LoginScreen
import com.kelompok3.cinematch.ui.RegisterScreen
import com.kelompok3.cinematch.ui.HomeScreen
import com.kelompok3.cinematch.ui.DetailScreen
import com.kelompok3.cinematch.ui.FavoriteScreen
import com.kelompok3.cinematch.ui.ProfileScreen
import com.kelompok3.cinematch.ui.EditProfileScreen
import com.kelompok3.cinematch.ui.theme.CineMatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Memastikan database lokal Room siap
        AppDatabase.getDatabase(this)

        setContent {
            CineMatchTheme {
                val navController = rememberNavController()
                val auth = FirebaseAuth.getInstance()

                // Menentukan halaman pertama berdasarkan status login
                val startDest = if (auth.currentUser != null) "home" else "login"

                // State untuk menampung movie yang dipilih saat navigasi ke Detail
                var selectedMovie by remember { mutableStateOf<Movie?>(null) }

                NavHost(navController = navController, startDestination = startDest) {

                    // --- LOGIN ---
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToRegister = { navController.navigate("register") }
                        )
                    }

                    // --- REGISTER ---
                    composable("register") {
                        RegisterScreen(
                            onRegisterSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onBackToLogin = { navController.popBackStack() }
                        )
                    }

                    // --- HOME ---
                    composable("home") {
                        HomeScreen(
                            onMovieClick = { movie ->
                                selectedMovie = movie
                                navController.navigate("detail")
                            },
                            onNavigateToFavorite = { navController.navigate("favorite") },
                            onNavigateToProfile = { navController.navigate("profile") },
                            onLogout = {
                                auth.signOut()
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }

                    // --- DETAIL MOVIE ---
                    composable("detail") {
                        val currentMovie = selectedMovie
                        if (currentMovie != null) {
                            DetailScreen(
                                movie = currentMovie,
                                onBack = { navController.popBackStack() },
                                onNavigateToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        } else {
                            // Jika movie null, balik ke home
                            LaunchedEffect(Unit) { navController.navigate("home") }
                        }
                    }

                    // --- FAVORITE ---
                    composable("favorite") {
                        FavoriteScreen(
                            onBack = { navController.popBackStack() },
                            onMovieClick = { favMovie ->
                                selectedMovie = Movie(
                                    id = favMovie.id,
                                    title = favMovie.title,
                                    category = favMovie.category,
                                    description = favMovie.description,
                                    rating = favMovie.rating,
                                    imageUrl = favMovie.imageUrl,
                                    trailerUrl = favMovie.trailerUrl
                                )
                                navController.navigate("detail")
                            }
                        )
                    }

                    // --- PROFILE VIEW ---
                    composable("profile") {
                        ProfileScreen(
                            onBack = { navController.popBackStack() },
                            onNavigateToEditProfile = { navController.navigate("edit_profile") }
                        )
                    }

                    // --- EDIT PROFILE (KOREKSI DI SINI) ---
                    composable("edit_profile") {
                        EditProfileScreen(
                            onBack = { navController.popBackStack() },
                            onLogoutToLogin = {
                                // Arahkan ke login dan bersihkan semua history navigasi
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}