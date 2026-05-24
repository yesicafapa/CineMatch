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
import com.kelompok3.cinematch.data.NotificationItem
import android.util.Log
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    onOpenMovie: (String) -> Unit
) {

    var notifications by remember {
        mutableStateOf<List<NotificationItem>>(emptyList())
    }

    LaunchedEffect(Unit) {

        // TEST baca data langsung
        FirebaseFirestore.getInstance()
            .collection("notifications")
            .get()
            .addOnSuccessListener { result ->

                Log.d(
                    "NOTIF_TEST",
                    "JUMLAH DATA = ${result.size()}"
                )

                result.documents.forEach { doc ->
                    Log.d(
                        "NOTIF_TEST",
                        "DOC = ${doc.id} | ${doc.getString("title")}"
                    )
                }
            }
            .addOnFailureListener {

                Log.e(
                    "NOTIF_TEST",
                    "ERROR = ${it.message}"
                )
            }

        val db = FirebaseFirestore.getInstance()

        db.collection("notifications")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->

                result.documents.firstOrNull()?.reference?.update(
                    "isRead",
                    true
                )
            }

        // Listener realtime
        FirebaseFirestore.getInstance()
            .collection("notifications")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {

                    Log.e(
                        "NOTIF_TEST",
                        "LISTENER ERROR = ${error.message}"
                    )

                    return@addSnapshotListener
                }

                if (snapshot != null) {

                    Log.d(
                        "NOTIF_TEST",
                        "SNAPSHOT SIZE = ${snapshot.documents.size}"
                    )

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Notifikasi")
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding)
        ) {

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {

                items(notifications) { notif ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {

                                if (notif.movieId.isNotEmpty()) {

                                    onOpenMovie(notif.movieId)
                                }
                            }
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                text = notif.title,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = notif.message
                            )
                        }
                    }
                }
            }
        }
    }
}