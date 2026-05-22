package com.example.daveai.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RelationshipDao {
    @Query("SELECT * FROM relationship_ledger WHERE id = 1")
    suspend fun getRelationshipLedger(): RelationshipEntity?
    
    @Query("SELECT * FROM relationship_ledger WHERE id = 1")
    fun getRelationshipLedgerSync(): RelationshipEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateLedger(ledger: RelationshipEntity)
}
