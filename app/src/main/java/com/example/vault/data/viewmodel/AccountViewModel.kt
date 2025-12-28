package com.example.vault.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.vault.data.entity.Account
import com.example.vault.data.entity.Platform
import com.example.vault.data.entity.PlatformType
import com.example.vault.data.repository.AccountRepository
import com.example.vault.data.repository.PlatformRepository
import com.example.vault.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

/**
 * AccountViewModel：基于仓库分离的重构版本
 * - 使用 Flow 组合平台与账号数据进行分组
 * - 平台类型由 PlatformRepository 提供
 * - 避免 observeForever，改用 Flow/LiveData 暴露 UI 状态
 */
class AccountViewModel(application: Application) : AndroidViewModel(application) {

    private val accountRepository: AccountRepository = RepositoryProvider.provideAccountRepository(application)
    private val platformRepository: PlatformRepository = RepositoryProvider.providePlatformRepository(application)

    // 原始数据流
    private val accountsFlow = accountRepository.observeAllAccounts()
    private val platformsFlow = platformRepository.observeAllPlatforms()

    // 原始账号列表（按 ID 降序）
    val allAccounts: LiveData<List<Account>> = accountsFlow.asLiveData()
    val allPlatforms: LiveData<List<Platform>> = platformsFlow.asLiveData()

    // 按平台名分组的账号列表
    val groupedAccounts = combine(accountsFlow, platformsFlow) { accounts, platforms ->
        val platformById = platforms.associateBy { it.id }
        accounts.groupBy { acc -> platformById[acc.platformId]?.platformName ?: "未知平台" }
    }.asLiveData()

    // 平台类型列表
    val platformTypes: LiveData<List<PlatformType>> = platformRepository.observeAllPlatformTypes().asLiveData()

    // 搜索结果（StateFlow 便于与平台流组合）
    private val searchResultsFlow = MutableStateFlow<List<Account>>(emptyList())
    val searchResults: LiveData<List<Account>> = searchResultsFlow.asLiveData()
    // 当前搜索协程任务：用于取消进行中的搜索
    private var searchJob: Job? = null

    // 按平台名分组的搜索结果
    val groupedSearchResults = combine(searchResultsFlow, platformsFlow) { results, platforms ->
        val platformById = platforms.associateBy { it.id }
        results.groupBy { acc -> platformById[acc.platformId]?.platformName ?: "未知平台" }
    }.asLiveData()

    // 加载与错误状态
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> = _statusMessage

    /** 全文搜索（平台名、账号、备注、电话、邮箱） */
    fun searchAccounts(keyword: String) {
        // 取消上一个搜索任务，避免并发
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                accountRepository.searchAccounts(keyword).collect { results ->
                    searchResultsFlow.value = results
                }
            } catch (e: Exception) {
                _errorMessage.value = "搜索失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** 按平台名称筛选 */
    fun searchAccountsByPlatformName(platformName: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                accountRepository.observeAccountsByPlatformName(platformName).collect { results ->
                    searchResultsFlow.value = results
                }
            } catch (e: Exception) {
                _errorMessage.value = "按平台名称搜索失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** 按平台类型筛选 */
    fun searchAccountsByPlatformType(platformType: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val typeEnum = PlatformType.fromString(platformType)
                if (typeEnum == null) {
                    searchResultsFlow.value = emptyList()
                } else {
                    accountRepository.observeAccountsByPlatformType(typeEnum).collect { results ->
                        searchResultsFlow.value = results
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "按平台类型搜索失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** 获取所有平台类型（一次性触发型调用场景） */
    fun loadAllPlatformTypes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                // 已通过 platformTypes LiveData 暴露，无需手动赋值；此处保留触发形式以兼容旧调用
                platformRepository.observeAllPlatformTypes().collect { /* no-op */ }
            } catch (e: Exception) {
                _errorMessage.value = "获取平台类型失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** 保存账号 */
    fun saveAccount(account: Account) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                accountRepository.saveAccount(account)
                _statusMessage.value = "保存成功"
            } catch (e: Exception) {
                _errorMessage.value = "保存失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 保存平台并保存账号：若平台不存在则创建，返回平台ID后保存账号
     */
    fun saveAccountWithPlatform(
        platformName: String,
        platformType: String?,
        remark: String?,
        accountName: String?,
        password: String,
        payPassword: String?,
        phone: String?,
        email: String?,
        idNumber: String?
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // 1) 查找平台（按名称），如不存在则创建
                val existing = platformRepository.observePlatformsByName(platformName).first()
                val platformId: Int = if (existing.isNotEmpty()) {
                    // 如果传入了类型，则优先匹配类型一致的；否则取第一个
                    val typeEnum = platformType?.let { PlatformType.fromString(it) }
                    val matched = existing.firstOrNull { it.platformType == typeEnum } ?: existing.first()
                    matched.id
                } else {
                    // 创建新平台
                    val typeEnum = platformType?.let { PlatformType.fromString(it) }
                    // [排序] 新增平台时将所有已有平台的 sortIndex +1，使新平台以 sortIndex=0 显示在最上方
                    platformRepository.bumpAllSortIndices()
                    val newPlatform = Platform(platformName = platformName, platformType = typeEnum, sortIndex = 0)
                    platformRepository.savePlatform(newPlatform)
                    // 再次查询获取ID（基于名称匹配）
                    val afterInsert = platformRepository.observePlatformsByName(platformName).first()
                    val inserted = afterInsert.firstOrNull { it.platformType == typeEnum } ?: afterInsert.first()
                    inserted.id
                }

                // 2) 保存账号
                val acc = Account(
                    platformId = platformId,
                    remark = remark,
                    account = accountName,
                    password = password,
                    payPassword = payPassword, // 设置可选的支付密码
                    phone = phone,
                    email = email,
                    idNumber = idNumber
                )
                accountRepository.saveAccount(acc)
                _statusMessage.value = "保存成功"
            } catch (e: Exception) {
                _errorMessage.value = "保存失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** 删除账号 */
    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                accountRepository.deleteAccount(account)
                // 删除账号后，如果该平台下已无账号，则删除对应平台
                val remaining = accountRepository.observeAccountsByPlatformId(account.platformId).first()
                if (remaining.isEmpty()) {
                    val platform = platformsFlow.first().firstOrNull { it.id == account.platformId }
                    if (platform != null) {
                        platformRepository.deletePlatform(platform)
                    }
                }
                _statusMessage.value = "删除成功"
            } catch (e: Exception) {
                _errorMessage.value = "删除失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** 清空搜索结果 */
    fun clearSearchResults() {
        searchResultsFlow.value = emptyList()
    }
    /** 取消进行中的搜索任务，并清空 loading 与结果 */
    fun cancelSearch() {
        searchJob?.cancel()
        _isLoading.value = false
        clearSearchResults()
    }

    /** 清空错误/状态 */
    fun clearErrorMessage() { _errorMessage.value = null }
    fun clearStatusMessage() { _statusMessage.value = null }

    /**
     * 更新平台类型（长按 PlatformCard 右侧类型后选择）
     * @param platform 目标平台对象
     * @param newType 新的平台类型
     */
    fun updatePlatformType(platform: Platform, newType: PlatformType) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                // 保存更新后的平台（仅更新类型字段，其余保持不变）
                val updated = platform.copy(platformType = newType)
                platformRepository.savePlatform(updated)
                _statusMessage.value = "平台类型已更新"
            } catch (e: Exception) {
                _errorMessage.value = "更新平台类型失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 批量更新平台顺序（拖拽重排后持久化）
     * @param idsInOrder 平台ID按目标顺序排列
     */
    fun reorderPlatforms(idsInOrder: List<Int>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                platformRepository.updateSortIndices(idsInOrder)
                _statusMessage.value = "排序已更新"
            } catch (e: Exception) {
                _errorMessage.value = "更新排序失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
