package com.kelompok3.cinematch.ui

import android.util.Base64
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kelompok3.cinematch.ui.theme.CineBlack
import com.kelompok3.cinematch.ui.theme.CinePink

data class WatchHistory(
    val title: String = "",
    val watchDate: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    var name by remember { mutableStateOf("Memuat...") }
    var base64Image by remember { mutableStateOf("") }
    val email = auth.currentUser?.email ?: "-"

    val watchHistoryList = remember { mutableStateListOf<WatchHistory>() }

    LaunchedEffect(userId) {
        if (userId != null) {
            // 1. Ambil Data Profil User (Hanya Name dan PhotoUrl)
            db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    name = doc.getString("name") ?: "User Baru"
                    base64Image = doc.getString("photoUrl") ?: ""
                }
            }

            // 2. Ambil Data Riwayat (SnapshotListener agar Real-time)
            db.collection("users").document(userId).collection("history")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Urutkan dari yang terbaru
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FIRESTORE", "Error ambil history: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        watchHistoryList.clear()
                        for (doc in snapshot.documents) {
                            val h = WatchHistory(
                                title = doc.getString("movieTitle") ?: "Film",
                                watchDate = doc.getString("watchDate") ?: ""
                            )
                            watchHistoryList.add(h)
                        }
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Saya", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CineBlack)
            )
        },
        containerColor = CineBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Foto Profil
            AsyncImage(
                model = if (base64Image.isEmpty()) "https://cdn-icons-png.flaticon.com/512/3135/3135715.png"
                else Base64.decode(base64Image, Base64.DEFAULT),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nama Lengkap
            Text(
                text = name,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            // Email (Sebagai pengganti username untuk identitas)
            Text(
                text = email,
                color = CinePink,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Edit
            Button(
                onClick = onNavigateToEditProfile,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CinePink),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Edit Profil", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(35.dp))

            // Bagian Riwayat Tontonan
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.History, null, tint = CinePink, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Riwayat Tontonan",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (watchHistoryList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada film yang ditonton", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(watchHistoryList) { history ->
                        Card(
                            modifier = Modifier.width(170.dp).height(95.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp).fillMaxSize(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = history.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = history.watchDate,
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}