package com.mybox.data.dao

import androidx.room.*
import com.mybox.data.model.MasterPassword

/**
 * 主密码数据访问对象
 */
@Dao
interface MasterPasswordDao {
    
    @Query("SELECT * FROM master_password WHERE id = 'master'")
    suspend fun getMasterPassword(): MasterPassword?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setMasterPassword(masterPassword: MasterPassword)
    
    @Query("DELETE FROM master_password")
    suspend fun deleteMasterPassword()
}