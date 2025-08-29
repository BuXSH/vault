package com.example.vault.data.repository

import android.content.Context
import com.example.vault.data.database.DatabaseSingleton
import com.example.vault.data.dao.AccountDao

/**
 * 仓库提供者：统一创建和管理仓库实例
 */
object RepositoryProvider {
    // 初始化账号仓库
    fun provideAccountRepository(context: Context): AccountRepository {
        val accountDao: AccountDao = DatabaseSingleton.getInstance(context).accountDao()
        return AccountRepository(accountDao)
    }
}