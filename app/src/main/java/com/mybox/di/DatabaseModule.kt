package com.mybox.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mybox.data.database.AppDatabase
import com.mybox.data.preferences.AppPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE events ADD COLUMN color TEXT NOT NULL DEFAULT '#FFFFFF'")
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mybox_database"
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providePasswordDao(database: AppDatabase) = database.passwordDao()

    @Provides
    @Singleton
    fun provideEventDao(database: AppDatabase) = database.eventDao()

    @Provides
    @Singleton
    fun provideMasterPasswordDao(database: AppDatabase) = database.masterPasswordDao()

    @Provides
    @Singleton
    fun provideAppPreferences(@ApplicationContext context: Context) = AppPreferences(context)
}
