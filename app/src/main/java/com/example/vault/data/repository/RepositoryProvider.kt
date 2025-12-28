package com.example.vault.data.repository

import android.content.Context
import com.example.vault.data.database.DatabaseSingleton
import com.example.vault.data.dao.AccountDao
import com.example.vault.data.dao.PlatformDao

/**
 * 仓库提供者：统一创建和管理仓库实例
 */
object RepositoryProvider {
    // 初始化账号仓库
    fun provideAccountRepository(context: Context): AccountRepository {
        val db = DatabaseSingleton.getInstance(context)
        val accountDao: AccountDao = db.accountDao()
        return AccountRepository(accountDao)
    }

    // 初始化平台仓库
    fun providePlatformRepository(context: Context): PlatformRepository {
        val db = DatabaseSingleton.getInstance(context)
        val platformDao: PlatformDao = db.platformDao()
        return PlatformRepository(platformDao)
    }
}
