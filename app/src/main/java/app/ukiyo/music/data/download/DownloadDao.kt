package app.ukiyo.music.data.download

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    suspend fun all(): List<DownloadEntity>

    @Query("SELECT * FROM downloads WHERE trackKey = :key LIMIT 1")
    suspend fun find(key: String): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DownloadEntity)

    @Query("DELETE FROM downloads WHERE trackKey = :key")
    suspend fun delete(key: String)

    @Query("SELECT COALESCE(SUM(sizeBytes), 0) FROM downloads")
    suspend fun totalBytes(): Long

    @Query("SELECT * FROM downloads ORDER BY lastPlayedAt ASC, createdAt ASC LIMIT 1")
    suspend fun oldest(): DownloadEntity?

    @Query("UPDATE downloads SET lastPlayedAt = :at WHERE trackKey = :key")
    suspend fun touch(key: String, at: Long)

    @Query("DELETE FROM downloads")
    suspend fun clear()
}
