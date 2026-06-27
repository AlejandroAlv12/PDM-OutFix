package com.pdm0126.outfix

import android.app.Application
import com.pdm0126.outfix.data.local.AppDatabase
import com.pdm0126.outfix.data.repository.GarmentRepository

class OutfixApplication : Application() {
    
    companion object {
        lateinit var instance: OutfixApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    // Single instance of database
    val database by lazy { AppDatabase.getDatabase(this) }
    
    // Single instance of repository
    val garmentRepository by lazy { GarmentRepository(database.garmentDao()) }
}
