package com.example.vault.data.database

import android.content.Context
import androidx.room.Room

// 数据库单例管理类
object DatabaseSingleton {
    // 懒加载数据库实例，确保线程安全
    @Volatile
    private var INSTANCE: AppDatabase? = null

    // 获取数据库实例的方法
    fun getInstance(context: Context): AppDatabase {
        // 双重校验锁，避免多线程同时创建实例
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,  // 使用应用上下文，避免内存泄漏
                AppDatabase::class.java,   // 数据库类
                "vault_database"           // 数据库文件名（存储在设备中的名称）
            )
                .fallbackToDestructiveMigration() // 允许破坏性迁移，清除旧数据重新创建
                .build()
            INSTANCE = instance
            instance
        }
    }
}