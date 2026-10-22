package com.example.approomproduto.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.approomproduto.dao.ProductDao
import com.example.approomproduto.model.Product

@Database(entities = [Product::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}

