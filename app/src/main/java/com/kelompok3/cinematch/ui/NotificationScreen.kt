package com.kelompok3.cinematch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kelompok3.cinematch.data.NotificationItem
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit,           // Callback untuk aksi klik tombol kembali ke halaman sebelumnya
    onOpenMovie: (String) -> Unit // Callback untuk melempar movieId dan membuka halaman Detail Film
) {

    // State Reactive: Menyimpan daftar list notifikasi untuk dirender ke dalam LazyColumn
    var notifications by remember {
        mutableStateOf<List<NotificationItem>>(emptyList())
    }

    // Side Effect: Berjalan sekali saat halaman ini pertama kali masuk ke dalam komposisi UI
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()

        // 1. FITUR AUTO-READ NOTIFIKASI TERBARU
        // Mengambil 1 data paling baru untuk diubah status baca-nya (isRead = true)
        db.collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING) // Urutkan berdasarkan waktu terbaru
            .limit(1) // Batasi hanya ambil 1 dokumen teratas
            .get()
            .addOnSuccessListener { result ->
                // Jika data ada, langsung update field isRead menjadi true di Firestore online
                result.documents.firstOrNull()?.reference?.update(
                    "isRead",
                    true
                )
            }

        // 2. REAL-TIME LISTENER DENGAN SINKRONISASI URUTAN WAKTU
        // Menempelkan snapshot listener agar data otomatis ter-update di layar tanpa perlu refresh/buka ulang halaman
        db.collection("notifications")
            .orderBy(
                "timestamp",
                Query.Direction.DESCENDING
            ) // FIX: Mengurutkan dari waktu yang paling baru masuk (Paling Atas)
            .addSnapshotListener { snapshot, error ->

                // Antisipasi proteksi jika query Firestore mengalami kendala/error jaringan
                if (error != null) {
                    Log.e("NOTIF_TEST", "LISTENER ERROR = ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d("NOTIF_TEST", "SNAPSHOT SIZE = ${snapshot.documents.size}")

                    // Mengubah (Mapping) data mentah dari dokumen Firestore menjadi list objek data class NotificationItem
                    notifications = snapshot.documents.map { doc ->
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
                }
            }
    }

    // STRUKTUR TAMPILAN LAYAR (SCAFFOLD)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Notifikasi", color = Color.White)
                },
                // Membuat tombol navigasi panah kembali di pojok kiri atas
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                // Mengatur warna latar belakang TopBar menjadi hitam pekat sesuai tema gelap CineMatch
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black) // Background layar gelap total
                .padding(padding)
        ) {

            // PENGKONDISIAN: Jika belum ada data notifikasi sama sekali di database
            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada notifikasi", color = Color.Gray)
                }
            } else {
                // KOMPONEN LIST DAFTAR NOTIFIKASI (Scrollable Efisien)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Loop item notifikasi yang datanya sudah urut dari Firestore
                    items(notifications) { notif ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable {
                                    // DEEP LINKING UI: Jika kolom movieId tidak kosong, klik kartu ini akan mengoper ID ke detail film
                                    if (notif.movieId.isNotEmpty()) {
                                        onOpenMovie(notif.movieId)
                                    }
                                },
                            // EFEK VISUAL STATUS BACA: Notifikasi baru (isRead=false) akan berwarna sedikit abu-abu terang
                            colors = CardDefaults.cardColors(
                                containerColor = if (notif.isRead) Color(0xFF1E1E1E) else Color(
                                    0xFF2D2D2D
                                )
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                // Merender Judul Notifikasi
                                Text(
                                    text = notif.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Merender Isi Pesan Notifikasi
                                Text(
                                    text = notif.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}