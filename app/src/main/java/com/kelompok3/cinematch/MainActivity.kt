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
import com.kelompok3.cinematch.ui.NotificationScreen
import com.kelompok3.cinematch.ui.theme.CineMatchTheme

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Memastikan database lokal Room siap
        AppDatabase.getDatabase(this)

        // Minta izin memunculkan notifikasi untuk Android 13 ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }

        // Ambil fcmToken agar HP terdaftar di Firebase Console
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .update("fcmToken", token)
                }
            }

        setContent {
            CineMatchTheme {
                val navController = rememberNavController()
                val auth = FirebaseAuth.getInstance()

                // Menentukan halaman pertama berdasarkan status login
                val startDest = if (auth.currentUser != null) "home" else "login"

                // State menampung movie terpilih saat masuk ke halaman detail
                var selectedMovie by remember { mutableStateOf<Movie?>(null) }

                NavHost(navController = navController, startDestination = startDest) {

                    // --- NOTIFIKASI ---
                    composable("notification") {
                        NotificationScreen(
                            onBack = { navController.popBackStack() },
                            onOpenMovie = { movieId ->
                                FirebaseFirestore.getInstance()
                                    .collection("movies")
                                    .document(movieId)
                                    .get()
                                    .addOnSuccessListener { doc ->
                                        if (doc.exists()) {
                                            selectedMovie = Movie(
                                                id = doc.id,
                                                title = doc.getString("title") ?: "",
                                                category = doc.getString("category") ?: "",
                                                description = doc.getString("description") ?: "",
                                                rating = doc.getDouble("rating") ?: 0.0,
                                                imageUrl = doc.getString("imageUrl") ?: "",
                                                trailerUrl = doc.getString("trailerUrl") ?: ""
                                            )
                                            navController.navigate("detail")
                                        }
                                    }
                            }
                        )
                    }

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
                            onNavigateToNotification = { navController.navigate("notification") },
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

                    // --- EDIT PROFILE ---
                    composable("edit_profile") {
                        EditProfileScreen(
                            onBack = { navController.popBackStack() },
                            onLogoutToLogin = {
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