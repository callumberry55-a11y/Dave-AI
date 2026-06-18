package com.example.daveai.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE userEmail = :userEmail ORDER BY createdAt DESC")
    fun getConversationsForUser(userEmail: String): Flow<List<ConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversationById(id: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)

    @Query("""
        SELECT messages.* FROM messages
        JOIN messages_fts ON messages.id = messages_fts.rowid
        WHERE messages_fts MATCH :query
    """)
    suspend fun searchMessages(query: String): List<MessageEntity>
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories WHERE userEmail = :userEmail")
    fun getMemoriesForUser(userEmail: String): Flow<List<MemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemoryById(id: String)

    @Query("""
        SELECT memories.* FROM memories
        JOIN memories_fts ON memories.id = memories_fts.rowid
        WHERE memories_fts MATCH :query
    """)
    suspend fun searchMemories(query: String): List<MemoryEntity>

    @Query("SELECT * FROM memories")
    suspend fun getAllMemories(): List<MemoryEntity>

    suspend fun findSimilarMemories(targetVector: FloatArray, limit: Int = 5): List<MemoryEntity> {
        val allMemories = getAllMemories()
        return com.example.daveai.util.VectorUtils.findMostSimilar(
            target = targetVector,
            items = allMemories,
            vectorExtractor = { it.vectorEmbedding },
            limit = limit
        )
    }
}

@Dao
interface MemoryLinkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemoryLink(link: MemoryLinkEntity)

    @Query("SELECT * FROM memory_links WHERE messageId = :messageId")
    suspend fun getLinksForMessage(messageId: String): List<MemoryLinkEntity>
}
