package com.example.vault.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.vault.data.entity.Account
import com.example.vault.data.repository.AccountRepository
import com.example.vault.data.repository.RepositoryProvider
import kotlinx.coroutines.launch

/**
 * 账号管理的ViewModel
 * 负责处理UI状态管理和业务逻辑
 */
class AccountViewModel(application: Application) : AndroidViewModel(application) {
    
    // 仓库实例
    private val accountRepository: AccountRepository = RepositoryProvider.provideAccountRepository(application)
    
    // 所有账号列表的LiveData
    val allAccounts: LiveData<List<Account>> = accountRepository.observeAllAccounts().asLiveData()
    
    // 按平台分组的所有账号LiveData
    private val _groupedAccounts = MutableLiveData<Map<String, List<Account>>>()
    val groupedAccounts: LiveData<Map<String, List<Account>>> = _groupedAccounts
    
    // 搜索结果的LiveData
    private val _searchResults = MutableLiveData<List<Account>>()
    val searchResults: LiveData<List<Account>> = _searchResults
    
    // 按平台分组的搜索结果LiveData
    private val _groupedSearchResults = MutableLiveData<Map<String, List<Account>>>()
    val groupedSearchResults: LiveData<Map<String, List<Account>>> = _groupedSearchResults
    
    // 按平台名称搜索结果的LiveData
    private val _platformNameResults = MutableLiveData<List<Account>>()
    val platformNameResults: LiveData<List<Account>> = _platformNameResults
    
    // 按平台类型搜索结果的LiveData
    private val _platformTypeResults = MutableLiveData<List<Account>>()
    val platformTypeResults: LiveData<List<Account>> = _platformTypeResults
    
    // 所有平台类型的LiveData
    private val _platformTypes = MutableLiveData<List<String>>()
    val platformTypes: LiveData<List<String>> = _platformTypes
    
    // 加载状态
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    // 错误消息
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // 状态消息
    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> = _statusMessage
    
    init {
        // 监听所有账号数据变化，自动更新分组数据
        allAccounts.observeForever { accounts ->
            _groupedAccounts.value = groupAccountsByPlatform(accounts)
        }
    }
    
    /**
     * 保存账号（新增或更新）
     * @param account 要保存的账号对象
     */
    fun saveAccount(account: Account) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                accountRepository.saveAccount(account)
                _statusMessage.value = "账号保存成功"
                
            } catch (e: Exception) {
                _errorMessage.value = "保存账号失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 删除账号
     * @param account 要删除的账号对象
     */
    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                accountRepository.deleteAccount(account)
                _statusMessage.value = "账号删除成功"
                
            } catch (e: Exception) {
                _errorMessage.value = "删除账号失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 批量删除账号
     * @param accounts 要删除的账号列表
     */
    fun deleteAccounts(accounts: List<Account>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                accountRepository.deleteAccounts(accounts)
                _statusMessage.value = "批量删除成功"
                
            } catch (e: Exception) {
                _errorMessage.value = "批量删除失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 清空所有账号
     */
    fun deleteAllAccounts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                accountRepository.deleteAllAccounts()
                _statusMessage.value = "所有账号已清空"
                
            } catch (e: Exception) {
                _errorMessage.value = "清空账号失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 按平台分组账号列表
     * @param accounts 账号列表
     * @return 按平台名称分组的账号Map
     */
    private fun groupAccountsByPlatform(accounts: List<Account>): Map<String, List<Account>> {
        return accounts.groupBy { it.platformName }
            .toSortedMap() // 按平台名称排序
    }
    
    /**
     * 搜索账号（全文搜索）
     * @param keyword 搜索关键词
     */
    fun searchAccounts(keyword: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                accountRepository.searchAccounts(keyword).collect { results ->
                    _searchResults.value = results
                    _groupedSearchResults.value = groupAccountsByPlatform(results)
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "搜索失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 按平台名称搜索账号
     * @param platformName 平台名称
     */
    fun searchAccountsByPlatformName(platformName: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                accountRepository.observeAccountsByPlatformName(platformName).collect { results ->
                    _platformNameResults.value = results
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "按平台名称搜索失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 按平台类型搜索账号
     * @param platformType 平台类型
     */
    fun searchAccountsByPlatformType(platformType: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                accountRepository.observeAccountsByPlatformType(platformType).collect { results ->
                    _platformTypeResults.value = results
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "按平台类型搜索失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    

    
    /**
     * 获取所有平台类型
     */
    fun loadAllPlatformTypes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                accountRepository.observeAllPlatformTypes().collect { types ->
                    _platformTypes.value = types
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "获取平台类型失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 清除搜索结果
     */
    fun clearSearchResults() {
        _searchResults.value = emptyList()
        _groupedSearchResults.value = emptyMap()
    }
    
    /**
     * 清除平台名称搜索结果
     */
    fun clearPlatformNameResults() {
        _platformNameResults.value = emptyList()
    }
    
    /**
     * 清除平台类型搜索结果
     */
    fun clearPlatformTypeResults() {
        _platformTypeResults.value = emptyList()
    }
    
    /**
     * 清除所有分组数据
     */
    fun clearGroupedData() {
        _groupedAccounts.value = emptyMap()
        _groupedSearchResults.value = emptyMap()
    }
    
    /**
     * 清除平台类型列表
     */
    fun clearPlatformTypes() {
        _platformTypes.value = emptyList()
    }
    
    /**
     * 清除错误消息
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    /**
     * 清除状态消息
     */
    fun clearStatusMessage() {
        _statusMessage.value = null
    }
    
    /**
     * 获取所有平台名称列表
     * @return 平台名称列表
     */
    fun getPlatformNames(): List<String> {
        return groupedAccounts.value?.keys?.toList() ?: emptyList()
    }
    
    /**
     * 获取指定平台的账号数量
     * @param platformName 平台名称
     * @return 账号数量
     */
    fun getAccountCountByPlatform(platformName: String): Int {
        return groupedAccounts.value?.get(platformName)?.size ?: 0
    }
    
    /**
     * 获取总平台数量
     * @return 平台数量
     */
    fun getPlatformCount(): Int {
        return groupedAccounts.value?.size ?: 0
    }
    
    /**
     * 检查指定平台是否存在账号
     * @param platformName 平台名称
     * @return 是否存在账号
     */
    fun hasPlatformAccounts(platformName: String): Boolean {
        return groupedAccounts.value?.get(platformName)?.isNotEmpty() == true
    }
}