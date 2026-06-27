package com.pdm0126.outfix.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GarmentDao {
    @Query("SELECT * FROM garments ORDER BY createdAt DESC")
    fun getAllGarmentsFlow(): Flow<List<GarmentEntity>>
    
    @Query("SELECT * FROM garments ORDER BY createdAt DESC")
    fun getAllGarments(): List<GarmentEntity>

    @Query("SELECT * FROM garments WHERE isPendingSync = 1")
    fun getPendingSyncGarments(): List<GarmentEntity>

    @Query("SELECT * FROM garments WHERE id = :id LIMIT 1")
    fun getGarmentById(id: String): GarmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGarment(garment: GarmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGarments(garments: List<GarmentEntity>)

    @Query("DELETE FROM garments WHERE id = :id")
    fun deleteGarmentById(id: String)
    
    @Query("DELETE FROM garments")
    fun deleteAllGarments()
}
