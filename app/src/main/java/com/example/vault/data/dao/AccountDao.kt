package com.example.vault.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.vault.data.entity.Account
import kotlinx.coroutines.flow.Flow

/**
 * 账号数据访问对象
 * 提供对合并后Account实体的数据库操作方法
 */
@Dao
interface AccountDao {
    
    /**
     * 插入新账号
     * @param account 要插入的账号对象
     */
    @Insert
    suspend fun insert(account: Account)

    /**
     * 更新指定账号（根据账号的id进行匹配更新）
     * @param account 要更新的账号对象
     */
    @Update
    suspend fun update(account: Account)

    /**
     * 删除指定账号（根据账号的id进行匹配删除）
     * @param account 要删除的账号对象
     */
    @Delete
    suspend fun delete(account: Account)

    /**
     * 查询所有账号（按自增ID降序排序，即最新添加的账号排在前面）
     * @return 所有账号的Flow列表
     */
    @Query("SELECT * FROM account ORDER BY id DESC")
    fun getAllAccounts(): Flow<List<Account>>

    /**
     * 根据平台名称查询账号
     * @param platformName 平台名称
     * @return 指定平台的账号Flow列表
     */
    @Query("SELECT * FROM account WHERE platformName = :platformName ORDER BY id DESC")
    fun getAccountsByPlatformName(platformName: String): Flow<List<Account>>

    /**
     * 根据平台类型查询账号
     * @param platformType 平台类型
     * @return 指定平台类型的账号Flow列表
     */
    @Query("SELECT * FROM account WHERE platformType = :platformType ORDER BY id DESC")
    fun getAccountsByPlatformType(platformType: String): Flow<List<Account>>

    /**
     * 全文搜索账号（搜索多个字段）
     * 搜索范围包括：platformName（平台名称）、account（账号）、remark（备注）、phone（手机号）、email（邮箱）
     * @param keyword 搜索关键词
     * @return 匹配的账号Flow列表
     */
    @Query("SELECT * FROM account WHERE platformName LIKE '%' || :keyword || '%' OR account LIKE '%' || :keyword || '%' OR remark LIKE '%' || :keyword || '%' OR phone LIKE '%' || :keyword || '%' OR email LIKE '%' || :keyword || '%' ORDER BY id DESC")
    fun searchAccounts(keyword: String): Flow<List<Account>>

    /**
     * 获取所有不同的平台类型
     * @return 平台类型列表
     */
    @Query("SELECT DISTINCT platformType FROM account WHERE platformType IS NOT NULL ORDER BY platformType ASC")
    fun getAllPlatformTypes(): Flow<List<String>>

    /**
     * 删除所有账号
     */
    @Query("DELETE FROM account")
    suspend fun deleteAllAccounts()
}