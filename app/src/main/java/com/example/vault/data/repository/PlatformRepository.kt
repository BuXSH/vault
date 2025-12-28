package com.example.vault.data.repository

import com.example.vault.data.dao.PlatformDao
import com.example.vault.data.entity.Platform
import com.example.vault.data.entity.PlatformType
import kotlinx.coroutines.flow.Flow

/**
 * 平台仓库：处理平台相关的数据逻辑
 */
class PlatformRepository(
    private val platformDao: PlatformDao
) : BaseRepository() {

    /** 获取所有平台（按ID升序） */
    fun observeAllPlatforms(): Flow<List<Platform>> = platformDao.getAllPlatforms()

    /** 按名称查询平台 */
    fun observePlatformsByName(name: String): Flow<List<Platform>> = platformDao.getPlatformsByName(name)

    /** 按类型查询平台 */
    fun observePlatformsByType(type: PlatformType): Flow<List<Platform>> = platformDao.getPlatformsByType(type)

    /** 获取所有不同的平台类型 */
    fun observeAllPlatformTypes(): Flow<List<PlatformType>> = platformDao.getAllPlatformTypes()

    /** 保存平台（新增或更新） */
    suspend fun savePlatform(platform: Platform) {
        ioDispatcher {
            if (platform.id == 0) {
                platformDao.insert(platform)
            } else {
                platformDao.update(platform)
            }
        }
    }

    /** 删除平台 */
    suspend fun deletePlatform(platform: Platform) {
        ioDispatcher { platformDao.delete(platform) }
    }

    /**
     * [排序] 将所有平台的 sortIndex 整体 +1（用于插入新平台到最上方）
     */
    suspend fun bumpAllSortIndices() {
        ioDispatcher { platformDao.bumpAllSortIndices() }
    }

    /**
     * 批量更新平台排序索引（按照传入顺序0..N-1写入）
     * @param idsInOrder 平台ID按目标顺序排列
     */
    suspend fun updateSortIndices(idsInOrder: List<Int>) {
        ioDispatcher {
            idsInOrder.forEachIndexed { index, id ->
                platformDao.updateSortIndex(id, index)
            }
        }
    }
}

