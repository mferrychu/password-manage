package com.mybox.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mybox.data.dao.EventDao
import com.mybox.data.dao.MasterPasswordDao
import com.mybox.data.dao.PasswordDao
import com.mybox.data.model.Event
import com.mybox.data.model.MasterPassword
import com.mybox.data.model.Password

/**
 * 应用数据库
 */
@Database(
    entities = [
        Password::class,
        Event::class,
        MasterPassword::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun passwordDao(): PasswordDao
    abstract fun eventDao(): EventDao
    abstract fun masterPasswordDao(): MasterPasswordDao
}