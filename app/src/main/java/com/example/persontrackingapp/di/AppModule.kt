package com.example.persontrackingapp.di

import android.content.Context
import androidx.room.Room
import com.example.persontrackingapp.data.local.LocationDao
import com.example.persontrackingapp.data.local.LocationDatabase
import com.example.persontrackingapp.data.local.PreferencesManager
import com.example.persontrackingapp.data.repository.LocationRepositoryImpl
import com.example.persontrackingapp.domain.repository.LocationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideLocationDatabase(
        @ApplicationContext context: Context
    ): LocationDatabase {
        return Room.databaseBuilder(
            context,
            LocationDatabase::class.java,
            "location_database"
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideLocationDao(database: LocationDatabase): LocationDao {
        return database.locationDao()
    }
    
    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }
    
    @Provides
    @Singleton
    fun provideLocationRepository(
        locationDao: LocationDao,
        preferencesManager: PreferencesManager,
        @ApplicationContext context: Context
    ): LocationRepository {
        return LocationRepositoryImpl(locationDao, preferencesManager, context)
    }
} 