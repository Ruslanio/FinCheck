package com.financetracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.financetracker.database.dao.CategoryDao
import com.financetracker.database.dao.TransactionDao
import com.financetracker.database.entity.CategoryEntity
import com.financetracker.database.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
}
