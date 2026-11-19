package com.mybox.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.mybox.data.model.Password
import kotlinx.coroutines.flow.Flow

/**
 * 密码数据访问对象
 */
@Dao
interface PasswordDao {
    
    @Query("SELECT * FROM passwords ORDER BY updatedAt DESC")
    fun getAllPasswords(): Flow<List<Password>>
    
    @Query("SELECT * FROM passwords WHERE id = :id")
    suspend fun getPasswordById(id: Long): Password?
    
    @Query("SELECT * FROM passwords WHERE title LIKE '%' || :query || '%' OR account LIKE '%' || :query || '%' OR remark LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchPasswords(query: String): Flow<List<Password>>
    
    @Query("SELECT COUNT(*) FROM passwords")
    suspend fun getPasswordCount(): Int

    @Query("SELECT * FROM passwords ORDER BY updatedAt DESC")
    fun getPasswordsPaged(): PagingSource<Int, Password>
    
    @Query("SELECT * FROM passwords WHERE title LIKE '%' || :query || '%' OR account LIKE '%' || :query || '%' OR remark LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchPasswordsPaged(query: String): PagingSource<Int, Password>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(password: Password): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPasswords(passwords: List<Password>)
    
    @Update
    suspend fun updatePassword(password: Password)
    
    @Delete
    suspend fun deletePassword(password: Password)
    
    @Query("DELETE FROM passwords WHERE id = :id")
    suspend fun deletePasswordById(id: Long)
}