package app.ukiyo.music.data.download

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val trackKey: String,
    val title: String,
    val artist: String,
    val album: String?,
    val durationMs: Long?,
    val thumbnailUrl: String?,
    val videoId: String?,
    val filePath: String,
    val quality: String,
    val sizeBytes: Long,
    val createdAt: Long,
    val lastPlayedAt: Long
)
