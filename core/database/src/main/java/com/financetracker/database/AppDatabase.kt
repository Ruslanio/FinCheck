package com.financetracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.financetracker.database.dao.TransactionDao
import com.financetracker.database.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}
