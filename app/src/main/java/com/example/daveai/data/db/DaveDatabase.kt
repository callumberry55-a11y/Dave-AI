package com.example.daveai.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom

@Database(
    entities = [
        ChatMessageEntity::class, 
        ChatSessionEntity::class, 
        Riddle::class, 
        SemanticMemory::class, 
        RelationshipEntity::class, 
        NotificationEntity::class, 
        SecurityEvent::class,
        UserEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        MemoryEntity::class,
        MemoryLinkEntity::class,
        MessageFtsEntity::class,
        MemoryFtsEntity::class
    ],
    version = 24,
    exportSchema = false
)
@TypeConverters(DataConverters::class)
abstract class DaveDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun riddleDao(): RiddleDao
    abstract fun semanticMemoryDao(): SemanticMemoryDao
    abstract fun relationshipDao(): RelationshipDao
    abstract fun notificationDao(): NotificationDao
    abstract fun securityEventDao(): SecurityEventDao

    abstract fun userDao(): UserDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun memoryDao(): MemoryDao
    abstract fun memoryLinkDao(): MemoryLinkDao

    companion object {
        @Volatile
        private var INSTANCE: DaveDatabase? = null

        fun getDatabase(context: Context): DaveDatabase {
            return INSTANCE ?: synchronized(this) {
                // Initialize SQLCipher libraries
                System.loadLibrary("sqlcipher")

                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                val sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    "dave_secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )

                var dbKey = sharedPreferences.getString("db_key", null)
                if (dbKey == null) {
                    val random = SecureRandom()
                    val bytes = ByteArray(32)
                    random.nextBytes(bytes)
                    dbKey = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                    sharedPreferences.edit().putString("db_key", dbKey).apply()
                }
                
                val factory = SupportFactory(dbKey.toByteArray(Charsets.UTF_8))

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DaveDatabase::class.java,
                    "dave_database_encrypted" // Renamed to avoid opening old plaintext DB
                )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration(dropAllTables = true) // Wipes plaintext DB on upgrade
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
