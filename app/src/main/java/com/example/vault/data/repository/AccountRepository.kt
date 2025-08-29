package com.example.vault.data.repository

import com.example.vault.data.dao.AccountDao
import com.example.vault.data.entity.Account
import kotlinx.coroutines.flow.Flow

/**
 * 账号仓库：处理账号相关的数据逻辑
 * 基于合并后的Account实体，提供完整的账号和平台数据管理
 */
class AccountRepository(
    private val accountDao: AccountDao
) : BaseRepository() {

    /**
     * 观察所有账号（按 ID 降序，最新添加的在前）
     * @return 所有账号的Flow列表
     */
    fun observeAllAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccounts()
    }

    /**
     * 按平台名称观察账号
     * @param platformName 平台名称
     * @return 指定平台的账号Flow列表
     */
    fun observeAccountsByPlatformName(platformName: String): Flow<List<Account>> {
        return accountDao.getAccountsByPlatformName(platformName)
    }

    /**
     * 按平台类型观察账号
     * @param platformType 平台类型
     * @return 指定平台类型的账号Flow列表
     */
    fun observeAccountsByPlatformType(platformType: String): Flow<List<Account>> {
        return accountDao.getAccountsByPlatformType(platformType)
    }

    /**
     * 全文搜索账号
     * 搜索范围包括：platformName、account、remark、phone、email
     * @param keyword 搜索关键词
     * @return 匹配的账号Flow列表
     */
    fun searchAccounts(keyword: String): Flow<List<Account>> {
        return accountDao.searchAccounts(keyword)
    }

    /**
     * 获取所有不同的平台类型
     * @return 平台类型列表
     */
    fun observeAllPlatformTypes(): Flow<List<String>> {
        return accountDao.getAllPlatformTypes()
    }

    /**
     * 新增或修改账号（根据ID判断）
     * @param account 要保存的账号对象
     */
    suspend fun saveAccount(account: Account) {
        ioDispatcher {
            if (account.id == 0) {
                accountDao.insert(account)
            } else {
                accountDao.update(account)
            }
        }
    }

    /**
     * 删除单个账号
     * @param account 要删除的账号对象
     */
    suspend fun deleteAccount(account: Account) {
        ioDispatcher {
            accountDao.delete(account)
        }
    }

    /**
     * 批量删除账号
     * @param accounts 要删除的账号列表
     */
    suspend fun deleteAccounts(accounts: List<Account>) {
        ioDispatcher {
            accounts.forEach { accountDao.delete(it) }
        }
    }

    /**
     * 删除所有账号
     */
    suspend fun deleteAllAccounts() {
        ioDispatcher {
            accountDao.deleteAllAccounts()
        }
    }
}