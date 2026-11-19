package com.mybox.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 主密码数据实体
 */
@Entity(tableName = "master_password")
data class MasterPassword(
    @PrimaryKey
    val id: String = "master",          // 固定ID
    val passwordHash: String,           // 主密码哈希值
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)