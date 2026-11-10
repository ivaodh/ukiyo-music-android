package app.ukiyo.music.data.download

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DownloadEntity::class], version = 1, exportSchema = false)
abstract class UkiyoDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
}
