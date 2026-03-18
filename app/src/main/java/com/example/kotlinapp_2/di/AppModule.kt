package com.example.kotlinapp_2.di

import android.content.Context
import androidx.room.Room
import com.example.kotlinapp_2.data.local.NoteDao
import com.example.kotlinapp_2.data.local.NoteDatabase
import com.example.kotlinapp_2.data.repository.NoteRepository
import com.example.kotlinapp_2.data.repository.NoteRepositoryImpl
import com.example.kotlinapp_2.utils.Constants
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNoteDatabase(@ApplicationContext context: Context): NoteDatabase {
        return Room.databaseBuilder(
            context,
            NoteDatabase::class.java,
            Constants.DATABASE_NAME,
        ).build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(noteDatabase: NoteDatabase): NoteDao = noteDatabase.noteDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        noteRepositoryImpl: NoteRepositoryImpl,
    ): NoteRepository
}
