package com.rumir.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rumir.database.dao.TransactionDao
import com.rumir.database.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}
