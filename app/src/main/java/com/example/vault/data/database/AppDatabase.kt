package com.example.vault.data.database

import androidx.room.Database
import androidx.room.TypeConverters
import androidx.room.RoomDatabase
import com.example.vault.data.dao.AccountDao
import com.example.vault.data.dao.PlatformDao
import com.example.vault.data.entity.Account
import com.example.vault.data.entity.Platform
import com.example.vault.data.entity.PlatformTypeConverters

// 声明数据库包含的实体类，版本号更新为10（新增 Platform.sortIndex 字段）
@Database(
    entities = [Account::class, Platform::class],
    version = 10,
    exportSchema = false // 开发阶段可不导出数据库schema,正式应用/需要升级后续设为true
)
@TypeConverters(PlatformTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    // 提供DAO实例的抽象方法，Room会自动生成实现
    abstract fun accountDao(): AccountDao
    abstract fun platformDao(): PlatformDao
}
