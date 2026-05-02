package com.kelompok3.cinematch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.kelompok3.cinematch.data.Movie
import com.kelompok3.cinematch.data.MovieService
import com.kelompok3.cinematch.ui.theme.CineBlack
import com.kelompok3.cinematch.ui.theme.CinePink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMovieClick: (Movie) -> Unit,
    onNavigateToFavorite: () -> Unit,
    onLogout: () -> Unit
) {
    val movieService = remember { MovieService() }
    var allMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var displayedMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Logika Kategori
    val categories = listOf("Semua", "Action", "Horor", "Sejarah", "Sci-Fi")
    var selectedCategory by remember { mutableStateOf("Semua") }

    // Logika Menu Profil
    var showProfileMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        allMovies = movieService.getAllMovies()
        displayedMovies = allMovies
        isLoading = false
    }

    // Filter berdasarkan kategori
    LaunchedEffect(selectedCategory, allMovies) {
        displayedMovies = if (selectedCategory == "Semua") {
            allMovies
        } else {
            allMovies.filter { it.category.contains(selectedCategory, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CineMatch", color = CinePink, fontWeight = FontWeight.Bold) },
                actions = {
                    // Tombol Favorit
                    IconButton(onClick = onNavigateToFavorite) {
                        Icon(Icons.Default.Favorite, contentDescription = "Favorit", tint = CinePink)
                    }
                    // Menu Profil
                    IconButton(onClick = { showProfileMenu = true }) {
                        Icon(Icons.Default.Person, contentDescription = "Profil", tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = showProfileMenu,
                        onDismissRequest = { showProfileMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Profil") },
                            onClick = {
                                showProfileMenu = false
                                // Tambahkan navigasi edit profil di sini nanti
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Keluar (Logout)") },
                            onClick = {
                                showProfileMenu = false
                                FirebaseAuth.getInstance().signOut()
                                onLogout()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CineBlack)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(CineBlack).padding(padding)) {

            // --- MENU KATEGORI ---
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                containerColor = CineBlack,
                contentColor = CinePink,
                edgePadding = 16.dp,
                divider = {}
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = {
                            Text(
                                text = category,
                                color = if (selectedCategory == category) CinePink else Color.Gray,
                                fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CinePink)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedMovies) { movie ->
                        MovieCard(movie = movie, onClick = { onMovieClick(movie) })
                    }
                }
            }
        }
    }
}

@Composable
fun MovieCard(movie: Movie, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
        ) {
            AsyncImage(
                model = movie.imageUrl,
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = movie.title,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = movie.category,
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall
        )
    }
}