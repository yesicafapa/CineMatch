package com.kelompok3.cinematch.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kelompok3.cinematch.data.AppDatabase
import com.kelompok3.cinematch.data.FavoriteMovie
import com.kelompok3.cinematch.data.Movie
import com.kelompok3.cinematch.ui.theme.CineBlack
import com.kelompok3.cinematch.ui.theme.CinePink
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(movie: Movie, onBack: () -> Unit, onNavigateToLogin: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUser = auth.currentUser

    var isFavorite by remember { mutableStateOf(false) }

    // Sinkronisasi status favorit dari Room
    LaunchedEffect(currentUser) {
        if (currentUser != null && movie.id.isNotEmpty()) {
            isFavorite = db.movieDao().isFavorite(movie.id, currentUser.uid) > 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Film", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CineBlack)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (currentUser != null) {
                        if (movie.id.isEmpty()) return@FloatingActionButton // Proteksi ID kosong

                        scope.launch {
                            val fav = FavoriteMovie(
                                movie.id, currentUser.uid, movie.title,
                                movie.category, movie.description, movie.rating,
                                movie.imageUrl, movie.trailerUrl
                            )
                            if (isFavorite) db.movieDao().deleteFavorite(fav)
                            else db.movieDao().insertFavorite(fav)
                            isFavorite = !isFavorite
                        }
                    } else {
                        onNavigateToLogin()
                    }
                },
                containerColor = if (isFavorite) CinePink else Color.White
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite) Color.White else CineBlack
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CineBlack)
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Poster Film
            AsyncImage(
                model = movie.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(400.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(20.dp)) {
                Text(movie.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(movie.category, color = CinePink, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(16.dp))

                // Tombol Trailer
                Button(
                    onClick = {
                        if (movie.trailerUrl.isNotEmpty()) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(movie.trailerUrl)))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CinePink)
                ) {
                    Text("Tonton Trailer", color = Color.White)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Sinopsis", color = CinePink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(movie.description, color = Color.LightGray)

                Spacer(modifier = Modifier.height(32.dp))

                // --- FITUR KOMENTAR ---
                // Kirim ID Film ke bagian komentar
                CommentSection(movieId = movie.id)

                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
fun CommentSection(movieId: String) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var commentText by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var editingCommentId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(movieId) {
        if (movieId.isNotEmpty()) {
            db.collection("movies").document(movieId).collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        comments = snapshot.documents.map { it.id to (it.data ?: emptyMap()) }
                    }
                }
        }
    }

    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            "Komentar (${comments.size})",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Input Komentar ala YouTube
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar User yang sedang login
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(CinePink, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    currentUser?.email?.take(1)?.uppercase() ?: "U",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            TextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Tambahkan komentar...", color = Color.Gray, fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = CinePink,
                    focusedIndicatorColor = CinePink
                )
            )
        }

        if (commentText.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = {
                    commentText = ""
                    editingCommentId = null
                }) {
                    Text("Batal", color = Color.White)
                }
                Button(
                    onClick = {
                        val colRef = db.collection("movies").document(movieId).collection("comments")
                        if (editingCommentId != null) {
                            colRef.document(editingCommentId!!).update("commentText", commentText)
                            editingCommentId = null
                        } else {
                            val newComment = hashMapOf(
                                "userId" to (currentUser?.uid ?: ""),
                                "userName" to (currentUser?.email?.substringBefore("@") ?: "User"),
                                "commentText" to commentText,
                                "timestamp" to com.google.firebase.Timestamp.now()
                            )
                            colRef.add(newComment)
                        }
                        commentText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinePink),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(if (editingCommentId != null) "Simpan" else "Komentar")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // List Komentar ala YouTube
        comments.forEach { (commentId, data) ->
            val ownerId = data["userId"].toString()
            var showMenu by remember { mutableStateOf(false) }

            Row(modifier = Modifier.padding(bottom = 20.dp).fillMaxWidth()) {
                // Avatar Pengirim
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.DarkGray, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        data["userName"].toString().take(1).uppercase(),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "@${data["userName"]}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = data["commentText"].toString(),
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Tombol Titik Tiga (Hanya untuk pemilik)
                if (currentUser?.uid == ownerId) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color.DarkGray)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White) },
                                onClick = {
                                    showMenu = false
                                    editingCommentId = commentId
                                    commentText = data["commentText"].toString()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Hapus", color = Color.Red) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    db.collection("movies").document(movieId)
                                        .collection("comments").document(commentId).delete()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}