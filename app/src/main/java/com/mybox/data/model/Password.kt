package com.mybox.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 密码数据实体
 */
@Entity(tableName = "passwords")
data class Password(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,          // 标题（必填）
    val account: String = "",   // 账号（选填）
    val password: String,       // 密码（必填，加密存储）
    val remark: String = "",    // 备注（选填）
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)