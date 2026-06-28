package com.pdm0126.outfix

import android.app.Application
import com.pdm0126.outfix.data.local.AppDatabase
import com.pdm0126.outfix.data.repository.GarmentRepository
import com.pdm0126.outfix.data.repository.PlannerRepository

class OutfixApplication : Application() {
    
    companion object {
        lateinit var instance: OutfixApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    val database by lazy { AppDatabase.getDatabase(this) }
    
    val garmentRepository by lazy { GarmentRepository(database.garmentDao()) }
    val plannerRepository by lazy { PlannerRepository(database.plannerDayDao(), database.garmentDao()) }
}
