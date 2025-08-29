package com.example.vault.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.vault.data.dao.AccountDao
import com.example.vault.data.entity.Account

// 声明数据库包含的实体类，版本号更新为3
@Database(
    entities = [Account::class],
    version = 4,
    exportSchema = false // 开发阶段可不导出数据库schema,正式应用/需要升级后续设为true
)
abstract class AppDatabase : RoomDatabase() {
    // 提供DAO实例的抽象方法，Room会自动生成实现
    abstract fun accountDao(): AccountDao
}