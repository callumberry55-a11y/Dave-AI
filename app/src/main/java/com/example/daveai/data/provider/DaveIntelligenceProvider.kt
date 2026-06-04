package com.example.daveai.data.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.example.daveai.data.db.DaveDatabase

class DaveIntelligenceProvider : ContentProvider() {

    companion object {
        private const val MEMORIES = 1
        private const val RELATIONSHIP = 2
        
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        fun getAuthority(packageName: String): String = "$packageName.intelligence"

        fun initializeMatcher(packageName: String) {
            val authority = getAuthority(packageName)
            uriMatcher.addURI(authority, "memories", MEMORIES)
            uriMatcher.addURI(authority, "relationship", RELATIONSHIP)
        }
    }

    override fun onCreate(): Boolean {
        context?.let { ctx ->
            initializeMatcher(ctx.packageName)
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val db = context?.let { DaveDatabase.getDatabase(it) } ?: return null
        return when (uriMatcher.match(uri)) {
            MEMORIES -> {
                // Return cursor from Room database
                // Since Room doesn't directly return Cursors for complex queries easily without a DAO method,
                // we might need to add a raw query method to the DAO or use the underlying SQLite database.
                db.openHelper.readableDatabase.query(
                    "SELECT * FROM semantic_memory WHERE is_archived = 0",
                    emptyArray()
                )
            }
            RELATIONSHIP -> {
                db.openHelper.readableDatabase.query(
                    "SELECT * FROM relationship_ledger",
                    emptyArray()
                )
            }
            else -> null
        }
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            MEMORIES -> "vnd.android.cursor.dir/vnd.com.example.daveai.memory"
            RELATIONSHIP -> "vnd.android.cursor.item/vnd.com.example.daveai.relationship"
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
