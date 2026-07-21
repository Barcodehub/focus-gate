package com.example.focusgate.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `tasks` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `title` TEXT NOT NULL,
                `description` TEXT,
                `points` INTEGER NOT NULL,
                `completed` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `lastCompletedAt` INTEGER,
                `lastResetAt` INTEGER
            )
            """.trimIndent()
        )
    }
}