package com.kelompok3.cinematch.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Versi 2 dengan skema baru
@Database(entities = [FavoriteMovie::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDAO

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cinematch_db" // Nama database yang harus dicari di App Inspection
                )
                    // Menghapus data lama jika ada perubahan versi agar tidak crash
                    .fallbackToDestructiveMigration()

                    // TAMBAHKAN INI: Membolehkan akses di main thread sementara agar
                    // database cepat terdeteksi saat inisialisasi di MainActivity.
                    .allowMainThreadQueries()

                    .build()
                instance = newInstance
                newInstance
            }
        }
    }
}