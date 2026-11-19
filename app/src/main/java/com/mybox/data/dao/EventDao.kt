package com.mybox.data.dao

import androidx.room.*
import com.mybox.data.model.Event
import kotlinx.coroutines.flow.Flow

/**
 * 事件数据访问对象
 */
@Dao
interface EventDao {
    
    @Query("SELECT * FROM events ORDER BY date DESC, updatedAt DESC")
    fun getAllEvents(): Flow<List<Event>>
    
    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: Long): Event?
    
    @Query("SELECT * FROM events WHERE description LIKE '%' || :query || '%' ORDER BY date DESC, updatedAt DESC")
    fun searchEvents(query: String): Flow<List<Event>>
    
    @Query("SELECT * FROM events ORDER BY date DESC, updatedAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getEventsPaged(limit: Int, offset: Int): List<Event>
    
    @Query("SELECT COUNT(*) FROM events")
    suspend fun getEventCount(): Int
    
    @Query("SELECT * FROM events WHERE description LIKE '%' || :query || '%' ORDER BY date DESC, updatedAt DESC LIMIT :limit OFFSET :offset")
    suspend fun searchEventsPaged(query: String, limit: Int, offset: Int): List<Event>
    
    @Query("SELECT COUNT(*) FROM events WHERE description LIKE '%' || :query || '%'")
    suspend fun searchEventCount(query: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<Event>)
    
    @Update
    suspend fun updateEvent(event: Event)
    
    @Delete
    suspend fun deleteEvent(event: Event)
    
    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Long)
}