package com.example.daveai.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RiddleDao {
    @Query("SELECT * FROM riddle_table ORDER BY tier ASC")
    fun getAllRiddles(): Flow<List<Riddle>>

    @Query("SELECT * FROM riddle_table WHERE isSolved = 0 AND id NOT IN (:skippedIds) ORDER BY tier ASC LIMIT 1")
    suspend fun getNextUnsolvedRiddle(skippedIds: List<Int> = emptyList()): Riddle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRiddle(riddle: Riddle)

    @Update
    suspend fun updateRiddle(riddle: Riddle)

    @Query("UPDATE riddle_table SET isSolved = 1 WHERE id = :riddleId")
    suspend fun markAsSolved(riddleId: Int)
}
