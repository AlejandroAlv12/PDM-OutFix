package com.pdm0126.outfix.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannerDayDao {

    @Query("SELECT * FROM planner_days")
    fun getAllFlow(): Flow<List<PlannerDayEntity>>

    @Query("SELECT * FROM planner_days")
    fun getAll(): List<PlannerDayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertDay(day: PlannerDayEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(days: List<PlannerDayEntity>)

    @Query("DELETE FROM planner_days WHERE dayKey = :dayKey")
    fun deleteDay(dayKey: String)
}
