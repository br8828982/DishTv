package com.noor.dishtv.data.database

import androidx.room.*
import com.noor.dishtv.data.model.Channel
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    
    @Query("SELECT * FROM channels ORDER BY sortOrder ASC, name ASC")
    fun getAllChannels(): Flow<List<Channel>>
    
    @Query("SELECT * FROM channels WHERE isEnabled = 1 ORDER BY sortOrder ASC, name ASC")
    fun getEnabledChannels(): Flow<List<Channel>>
    
    @Query("SELECT * FROM channels WHERE isFavorite = 1 ORDER BY sortOrder ASC, name ASC")
    fun getFavoriteChannels(): Flow<List<Channel>>
    
    @Query("SELECT * FROM channels WHERE `group` = :group ORDER BY sortOrder ASC, name ASC")
    fun getChannelsByGroup(group: String): Flow<List<Channel>>
    
    @Query("SELECT DISTINCT `group` FROM channels WHERE `group` IS NOT NULL ORDER BY `group` ASC")
    fun getAllGroups(): Flow<List<String>>
    
    @Query("SELECT * FROM channels WHERE id = :id")
    suspend fun getChannelById(id: Long): Channel?
    
    @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%' OR `group` LIKE '%' || :query || '%'")
    fun searchChannels(query: String): Flow<List<Channel>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: Channel): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<Channel>): List<Long>
    
    @Update
    suspend fun updateChannel(channel: Channel)
    
    @Delete
    suspend fun deleteChannel(channel: Channel)
    
    @Query("DELETE FROM channels WHERE id = :id")
    suspend fun deleteChannelById(id: Long)
    
    @Query("DELETE FROM channels")
    suspend fun deleteAllChannels()
    
    @Query("UPDATE channels SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)
    
    @Query("UPDATE channels SET lastPlayedPosition = :position, lastPlayedTimestamp = :timestamp WHERE id = :id")
    suspend fun updatePlaybackPosition(id: Long, position: Long, timestamp: Long)
    
    @Query("UPDATE channels SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)
    
    @Query("SELECT COUNT(*) FROM channels")
    suspend fun getChannelCount(): Int
    
    @Query("SELECT COUNT(*) FROM channels WHERE isEnabled = 1")
    suspend fun getEnabledChannelCount(): Int
    
    @Query("SELECT COUNT(*) FROM channels WHERE isFavorite = 1")
    suspend fun getFavoriteChannelCount(): Int
}