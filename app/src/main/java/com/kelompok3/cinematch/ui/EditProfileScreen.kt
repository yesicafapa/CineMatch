package com.kelompok3.cinematch.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kelompok3.cinematch.ui.theme.CineBlack
import com.kelompok3.cinematch.ui.theme.CinePink
import java.io.ByteArrayOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onLogoutToLogin: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var base64Image by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } // State tambahan biar gak double klik

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream: InputStream? = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)
            val byteArray = outputStream.toByteArray()
            base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)
        }
    }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    name = doc.getString("name") ?: ""
                    base64Image = doc.getString("photoUrl") ?: ""
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profil", color = Color.White, fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = if (base64Image.isEmpty()) "https://cdn-icons-png.flaticon.com/512/3135/3135715.png"
                    else Base64.decode(base64Image, Base64.DEFAULT),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(2.dp, CinePink, CircleShape),
                    contentScale = ContentScale.Crop
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(34.dp)
                        .offset(x = (-4).dp, y = (-4).dp),
                    shape = CircleShape,
                    color = CinePink,
                    shadowElevation = 4.dp
                ) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.padding(7.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = CinePink,
                    unfocusedBorderColor = Color.DarkGray
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)

            Text("Ganti Kata Sandi (Opsional)", color = CinePink, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))

            OutlinedTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                label = { Text("Password Lama") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Password Baru") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmNewPassword,
                onValueChange = { confirmNewPassword = it },
                label = { Text("Konfirmasi Password Baru") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    if (user != null && !isLoading) {
                        isLoading = true
                        val updates = hashMapOf<String, Any>(
                            "name" to name,
                            "photoUrl" to base64Image
                        )

                        // GUNAKAN .set(updates, SetOptions.merge()) AGAR JIKA DOKUMEN BELUM ADA AKAN DIBUATKAN
                        db.collection("users").document(user.uid)
                            .set(updates, SetOptions.merge())
                            .addOnSuccessListener {
                                if (newPassword.isNotEmpty()) {
                                    if (newPassword != confirmNewPassword) {
                                        Toast.makeText(context, "Konfirmasi password salah!", Toast.LENGTH_SHORT).show()
                                        isLoading = false
                                        return@addOnSuccessListener
                                    }
                                    if (oldPassword.isEmpty()) {
                                        Toast.makeText(context, "Masukkan password lama!", Toast.LENGTH_SHORT).show()
                                        isLoading = false
                                        return@addOnSuccessListener
                                    }

                                    val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
                                    user.reauthenticate(credential).addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            user.updatePassword(newPassword).addOnSuccessListener {
                                                auth.signOut()
                                                Toast.makeText(context, "Sandi diubah. Silakan Login kembali", Toast.LENGTH_LONG).show()
                                                onLogoutToLogin()
                                            }
                                        } else {
                                            isLoading = false
                                            Toast.makeText(context, "Gagal verifikasi password lama!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Profil Berhasil Diupdate!", Toast.LENGTH_SHORT).show()
                                    onBack()
                                }
                            }
                            .addOnFailureListener {
                                isLoading = false
                                Toast.makeText(context, "Gagal update Firestore: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CinePink),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Simpan Perubahan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}