package com.example.daveai.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SemanticMemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: SemanticMemory): Long

    @Update
    suspend fun updateMemory(memory: SemanticMemory)

    @Query("SELECT * FROM semantic_memory ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<SemanticMemory>>

    @Query("SELECT * FROM semantic_memory WHERE content LIKE '%' || :query || '%' OR memory_type LIKE '%' || :query || '%' ORDER BY importance DESC, timestamp DESC LIMIT 10")
    suspend fun findRelevantMemories(query: String): List<SemanticMemory>

    @Query("SELECT * FROM semantic_memory WHERE (content LIKE '%' || :query || '%' OR sentiment LIKE '%' || :query || '%') AND is_archived = 0 ORDER BY importance DESC LIMIT 20")
    suspend fun searchBySemanticMeaning(query: String): List<SemanticMemory>

    @Query("DELETE FROM semantic_memory WHERE id = :id")
    suspend fun deleteMemory(id: Long)
}
