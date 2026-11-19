package com.mybox.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 事件数据实体
 */
@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,           // 日期（格式：YYYY-MM-DD，必填）
    val description: String,     // 事件说明（必填）
    val color: String = "#FFFFFF",  // 卡片颜色（十六进制，默认白色）
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)