package com.kelompok3.cinematch.ui

import android.content.Intent
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(movie: Movie, onBack: () -> Unit, onNavigateToLogin: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Inisialisasi Database Lokal (Room) dan Firebase Auth
    val dbRoom = remember { AppDatabase.getDatabase(context) }
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUser = auth.currentUser

    // State Reactive: Menandai apakah film ini sudah ada di daftar favorit lokal user atau belum
    var isFavorite by remember { mutableStateOf(false) }

    // VALIDASI STATUS FAVORIT: Cek ke SQLite lokal apakah film ini sudah di-bookmark oleh user saat ini
    LaunchedEffect(currentUser) {
        if (currentUser != null && movie.id.isNotEmpty()) {
            isFavorite = dbRoom.movieDao().isFavorite(movie.id, currentUser.uid) > 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Film", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CineBlack)
            )
        },
        // FLOATING ACTION BUTTON (LOGIKA BOOKMARK SQLITE)
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (currentUser != null) {
                        // Jalankan Coroutine Scope karena operasi Database Room wajib berjalan di background thread (Asynchronous)
                        scope.launch {
                            val fav = FavoriteMovie(
                                movie.id, currentUser.uid, movie.title,
                                movie.category, movie.description, movie.rating,
                                movie.imageUrl, movie.trailerUrl
                            )
                            // Jika sudah favorit maka hapus dari local DB, jika belum maka masukkan ke local DB
                            if (isFavorite) dbRoom.movieDao().deleteFavorite(fav)
                            else dbRoom.movieDao().insertFavorite(fav)

                            // Balikkan status state untuk memperbarui visual warna ikon jantung secara real-time
                            isFavorite = !isFavorite
                        }
                    } else {
                        onNavigateToLogin() // Tendang ke halaman login jika user anonim mencoba mem-favoritkan
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
            AsyncImage(
                model = movie.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(350.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = movie.title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating Admin",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = movie.rating.toString(),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "•", color = Color.Gray, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = movie.category,
                        color = CinePink,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // TOMBOL TONTON TRAILER (LOGIKA INTENT & HISTORI FIRESTORE)
                Button(
                    onClick = {
                        if (movie.trailerUrl.isNotEmpty()) {
                            if (currentUser != null) {
                                val dbFirestore = FirebaseFirestore.getInstance()
                                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                val currentDate = sdf.format(Date())

                                val historyData = hashMapOf(
                                    "movieTitle" to movie.title,
                                    "watchDate" to currentDate,
                                    "timestamp" to com.google.firebase.Timestamp.now()
                                )

                                // Catat aktivitas tontonan ke sub-collection 'history' di bawah dokumen user terkait
                                dbFirestore.collection("users")
                                    .document(currentUser.uid)
                                    .collection("history")
                                    .add(historyData)
                            }
                            // LOGIKA INTENT IMPLISIT: Membuka aplikasi eksternal (seperti YouTube atau Browser) untuk memutar URL trailer
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(movie.trailerUrl))
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CinePink),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tonton Trailer", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Sinopsis", color = CinePink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(movie.description, color = Color.LightGray, lineHeight = 20.sp)

                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(24.dp))

                // Memanggil Komponen Area Komentar
                CommentSection(movieId = movie.id)

                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

// REUSABLE AREA COMPONENT: Mengelola input ulasan, rating bintang, dan sinkronisasi list komentar film
@Composable
fun CommentSection(movieId: String) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // State Input Kendali Form Ulasan
    var commentText by remember { mutableStateOf("") }
    var ratingSelected by remember { mutableIntStateOf(0) }
    var comments by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var editingCommentId by remember { mutableStateOf<String?>(null) }

    var myName by remember { mutableStateOf("User") }
    var myPhoto by remember { mutableStateOf("") }

    // REAL-TIME LISTENER PROFILE DATA: Mengambil info profil user yang sedang login untuk dicantumkan di kotak input
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    myName = doc.getString("name") ?: "User"
                    myPhoto = doc.getString("photoUrl") ?: ""
                }
            }
        }
    }

    // REAL-TIME LISTENER COMMENTS: Menarik daftar ulasan film ini dari Firestore secara real-time berdasarkan waktu terbaru
    LaunchedEffect(movieId) {
        db.collection("movies").document(movieId).collection("comments")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    comments = snapshot.documents.map { it.id to (it.data ?: emptyMap()) }
                }
            }
    }

    Column {
        Text("Ulasan Pengguna (${comments.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserAvatar(myPhoto, size = 40.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(myName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        // RATING SELECTION (LOOP 5 BINTANG INTERAKTIF)
                        Row {
                            for (i in 1..5) {
                                Icon(
                                    imageVector = if (i <= ratingSelected) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (i <= ratingSelected) Color(0xFFFFD700) else Color.Gray,
                                    modifier = Modifier.size(24.dp).clickable { ratingSelected = i }
                                )
                            }
                        }
                    }
                }

                TextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Bagaimana pendapatmu tentang film ini?", color = Color.Gray, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = CinePink
                    )
                )

                if (commentText.isNotEmpty() || ratingSelected > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { commentText = ""; ratingSelected = 0; editingCommentId = null }) {
                            Text("Batal", color = Color.Gray)
                        }
                        // LOGIKA TOMBOL SUBMIT (KIRIM ATAU UPDATE DATA)
                        Button(
                            onClick = {
                                val colRef = db.collection("movies").document(movieId).collection("comments")
                                val data = hashMapOf(
                                    "userId" to (currentUser?.uid ?: ""),
                                    "commentText" to commentText,
                                    "rating" to ratingSelected,
                                    "timestamp" to com.google.firebase.Timestamp.now()
                                )

                                // UPSERT LOGIKA: Jika sedang mengedit, tembak fungsi update(). Jika baru, tembak fungsi add().
                                if (editingCommentId != null) {
                                    colRef.document(editingCommentId!!).update(data as Map<String, Any>)
                                    editingCommentId = null
                                } else {
                                    colRef.add(data)
                                }
                                commentText = ""; ratingSelected = 0
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CinePink),
                            enabled = ratingSelected > 0, // Validasi: Wajib isi bintang minimal 1 baru tombol menyala
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (editingCommentId != null) "Simpan" else "Kirim")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ITERASI RENDERING LIST: Melakukan perulangan komponen ulasan berdasarkan list data dari database
        comments.forEach { (id, data) ->
            val userIdFromComment = data["userId"].toString()
            CommentItem(
                db = db,
                userId = userIdFromComment,
                commentData = data,
                isOwner = currentUser?.uid == userIdFromComment, // Cek kepemilikan ulasan untuk memunculkan tombol opsi edit/hapus
                onEdit = {
                    editingCommentId = id
                    commentText = data["commentText"].toString()
                    ratingSelected = (data["rating"] as? Long)?.toInt() ?: 0
                },
                onDelete = {
                    db.collection("movies").document(movieId).collection("comments").document(id).delete()
                }
            )
        }
    }
}

// REUSABLE ITEM COMPONENT: Merender layout baris per baris ulasan dari pengguna lain beserta kontrol menunya
@Composable
fun CommentItem(
    db: FirebaseFirestore,
    userId: String,
    commentData: Map<String, Any>,
    isOwner: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var userName by remember { mutableStateOf("Memuat...") }
    var userPhoto by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    // CROSS-COLLECTION QUERY: Mengambil info nama asli & foto pengguna langsung dari root collection 'users'
    // berdasarkan userId yang tertera di dalam dokumen komentar terkait (mencegah nama anonim)
    LaunchedEffect(userId) {
        db.collection("users").document(userId).addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
                userName = doc.getString("name") ?: "User"
                userPhoto = doc.getString("photoUrl") ?: ""
            }
        }
    }

    Row(modifier = Modifier.padding(bottom = 24.dp).fillMaxWidth()) {
        UserAvatar(userPhoto, size = 44.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                val r = (commentData["rating"] as? Long)?.toInt() ?: 0
                for (i in 1..5) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i <= r) Color(0xFFFFD700) else Color(0xFF333333),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Text(commentData["commentText"].toString(), color = Color.LightGray, fontSize = 14.sp, lineHeight = 20.sp)
        }

        // AKSI MENU EDIT/HAPUS: Hanya akan dirender dan muncul jika ulasan tersebut adalah milik user yang sedang login saat ini
        if (isOwner) {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFF2A2A2A))
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit", color = Color.White) },
                        onClick = { showMenu = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus", color = Color.Red) },
                        onClick = { showMenu = false; onDelete() }
                    )
                }
            }
        }
    }
}

// UTILITY COMPONENT: Dekoder gambar string Base64 dari Firestore kembali menjadi bentuk bitmap/gambar avatar lingkaran
@Composable
fun UserAvatar(base64String: String, size: androidx.compose.ui.unit.Dp) {
    AsyncImage(
        model = if (base64String.isEmpty()) "https://cdn-icons-png.flaticon.com/512/3135/3135715.png"
        else Base64.decode(base64String, Base64.DEFAULT),
        contentDescription = null,
        modifier = Modifier.size(size).clip(CircleShape).background(Color.DarkGray),
        contentScale = ContentScale.Crop
    )
}