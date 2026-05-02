package com.kelompok3.cinematch.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// UBAH version dari 1 ke 2 karena skema FavoriteMovie sudah berubah (tambah imageUrl, trailerUrl, dll)
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
                    "cinematch_db"
                )
                    // Baris ini akan menghapus database lama yang strukturnya berbeda (versi 1)
                    // dan membuat ulang yang baru (versi 2) secara otomatis tanpa crash.
                    .fallbackToDestructiveMigration()
                    .build()
                instance = newInstance
                newInstance
            }
        }
    }
}