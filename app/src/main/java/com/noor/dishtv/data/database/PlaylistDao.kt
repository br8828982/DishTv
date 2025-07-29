package com.noor.dishtv.data.database

import androidx.room.*
import com.noor.dishtv.data.model.Playlist
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    
    @Query("SELECT * FROM playlists ORDER BY sortOrder ASC, name ASC")
    fun getAllPlaylists(): Flow<List<Playlist>>
    
    @Query("SELECT * FROM playlists WHERE isEnabled = 1 ORDER BY sortOrder ASC, name ASC")
    fun getEnabledPlaylists(): Flow<List<Playlist>>
    
    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Long): Playlist?
    
    @Query("SELECT * FROM playlists WHERE name LIKE '%' || :query || '%'")
    fun searchPlaylists(query: String): Flow<List<Playlist>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylists(playlists: List<Playlist>): List<Long>
    
    @Update
    suspend fun updatePlaylist(playlist: Playlist)
    
    @Delete
    suspend fun deletePlaylist(playlist: Playlist)
    
    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylistById(id: Long)
    
    @Query("DELETE FROM playlists")
    suspend fun deleteAllPlaylists()
    
    @Query("UPDATE playlists SET lastUpdated = :timestamp, channelCount = :channelCount WHERE id = :id")
    suspend fun updatePlaylistStats(id: Long, timestamp: Long, channelCount: Int)
    
    @Query("UPDATE playlists SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updatePlaylistStatus(id: Long, isEnabled: Boolean)
    
    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistCount(): Int
    
    @Query("SELECT COUNT(*) FROM playlists WHERE isEnabled = 1")
    suspend fun getEnabledPlaylistCount(): Int
}