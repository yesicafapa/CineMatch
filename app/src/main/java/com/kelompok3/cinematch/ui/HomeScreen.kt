package com.kelompok3.cinematch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.material.icons.filled.Notifications

// Daftar kategori disamakan persis dengan yang ada di panel Admin
val MOVIE_CATEGORIES = listOf(
    "Action", "Animasi", "Drama", "Fiksi Ilmiah (Sci-Fi)", "Fantasi",
    "Horor", "Komedi", "Misteri (Thriller)", "Petualangan", "Romantis",
    "Sejarah", "Teka-teki (Mystery)"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMovieClick: (Movie) -> Unit,
    onNavigateToFavorite: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onLogout: () -> Unit
) {
    val movieService = remember { MovieService() }
    var allMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var displayedMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Gabungkan "Semua" dengan list kategori admin
    val categories = remember { listOf("Semua") + MOVIE_CATEGORIES }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var showProfileMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        allMovies = movieService.getAllMovies()
        displayedMovies = allMovies
        isLoading = false
    }

    // Filter logika pencarian kategori (menggunakan equals/exact match agar lebih akurat)
    LaunchedEffect(selectedCategory, allMovies) {
        displayedMovies = if (selectedCategory == "Semua") {
            allMovies
        } else {
            allMovies.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CineMatch", color = CinePink, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = {
                            onNavigateToNotification()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notification",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onNavigateToFavorite) {
                        Icon(Icons.Default.Favorite, contentDescription = "Favorit", tint = CinePink)
                    }
                    Box {
                        IconButton(onClick = { showProfileMenu = true }) {
                            Icon(Icons.Default.Person, contentDescription = "Profil", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showProfileMenu,
                            onDismissRequest = { showProfileMenu = false },
                            modifier = Modifier.background(CineBlack)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Profil Saya", color = Color.White) },
                                onClick = {
                                    showProfileMenu = false
                                    onNavigateToProfile()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Keluar (Logout)", color = Color.Red) },
                                onClick = {
                                    showProfileMenu = false
                                    FirebaseAuth.getInstance().signOut()
                                    onLogout()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CineBlack)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(CineBlack).padding(padding)) {

            // Menampilkan seluruh kategori dengan indikator garis bawah (indicator) bawaan material3
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory).takeIf { it >= 0 } ?: 0,
                containerColor = CineBlack,
                contentColor = CinePink,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    val index = categories.indexOf(selectedCategory).takeIf { it >= 0 } ?: 0
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                        color = CinePink
                    )
                },
                divider = { HorizontalDivider(color = Color(0xFF2A2A2A)) }
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
            } else if (displayedMovies.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Belum ada film untuk kategori ini",
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
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

        // Judul Film
        Text(
            text = movie.title,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Baris Rating & Kategori
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Rating",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = movie.rating.toString(),
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "•",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = movie.category,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}