package com.kelompok3.cinematch

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Ambil Judul dan Isi Pesan yang kamu ketik di Firebase Console
        val title = remoteMessage.notification?.title ?: "CineMatch"
        val body = remoteMessage.notification?.body ?: "Ada notifikasi baru masuk!"

        showNotification(title, body)
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "cinematch_notification"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Membuat Notification Channel khusus Android Oreo (API 26) ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "CineMatch Notification Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        // Aksi standar: Jika notifikasi diklik, aplikasi akan terbuka ke halaman utama (HomeScreen)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Membangun tampilan notifikasi sistem
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Memakai icon default launcher proyekmu
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent) // Menghubungkan aksi klik pembuka aplikasi
            .setAutoCancel(true) // Notifikasi otomatis hilang setelah diklik pengguna
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Membuat notifikasi langsung muncul di atas layar
            .build()

        // Tembakkan notifikasi ke bar status HP pakai ID acak berdasarkan waktu agar tidak saling menimpa
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}