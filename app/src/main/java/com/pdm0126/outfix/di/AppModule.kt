package com.pdm0126.outfix.di

import android.content.Context
import com.pdm0126.outfix.data.local.AppDatabase
import com.pdm0126.outfix.data.local.GarmentDao
import com.pdm0126.outfix.data.local.PlannerDayDao
import com.pdm0126.outfix.data.repository.GarmentRepository
import com.pdm0126.outfix.data.repository.LentRepository
import com.pdm0126.outfix.data.repository.PlannerRepository
import com.pdm0126.outfix.data.api.AuthApi
import com.pdm0126.outfix.data.api.RetrofitClient
import com.pdm0126.outfix.data.prefs.SessionManager
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideGarmentDao(db: AppDatabase): GarmentDao = db.garmentDao()

    @Provides
    @Singleton
    fun providePlannerDayDao(db: AppDatabase): PlannerDayDao = db.plannerDayDao()

    @Provides
    @Singleton
    fun provideGarmentRepository(garmentDao: GarmentDao): GarmentRepository {
        return GarmentRepository(garmentDao)
    }

    @Provides
    @Singleton
    fun providePlannerRepository(
        plannerDayDao: PlannerDayDao,
        garmentDao: GarmentDao
    ): PlannerRepository {
        return PlannerRepository(plannerDayDao, garmentDao)
    }

    @Provides
    @Singleton
    fun provideLentRepository(@ApplicationContext context: Context): LentRepository {
        return LentRepository(context)
    }

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideAuthApi(): AuthApi {
        return RetrofitClient.authApi
    }
}
