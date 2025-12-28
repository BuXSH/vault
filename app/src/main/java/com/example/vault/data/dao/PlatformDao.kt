package com.example.vault.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.example.vault.data.entity.Platform
import com.example.vault.data.entity.PlatformType
import kotlinx.coroutines.flow.Flow

/**
 * 平台数据访问对象
 * 提供对 Platform 实体的数据库操作方法
 */
@Dao
interface PlatformDao {

    /**
     * 插入新平台
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(platform: Platform)

    /**
     * 更新平台
     */
    @Update
    suspend fun update(platform: Platform)

    /**
     * 删除平台
     */
    @Delete
    suspend fun delete(platform: Platform)

    /**
     * 查询所有平台（按 sortIndex 升序；相同索引按ID升序）
     */
    @Query("SELECT * FROM platform ORDER BY sortIndex ASC, id ASC")
    fun getAllPlatforms(): Flow<List<Platform>>

    /**
     * 根据名称查询平台
     */
    @Query("SELECT * FROM platform WHERE platformName = :platformName ORDER BY sortIndex ASC, id ASC")
    fun getPlatformsByName(platformName: String): Flow<List<Platform>>

    /**
     * 根据类型查询平台
     */
    @Query("SELECT * FROM platform WHERE platformType = :platformType ORDER BY sortIndex ASC, id ASC")
    fun getPlatformsByType(platformType: PlatformType): Flow<List<Platform>>

    /**
     * 获取所有不同的平台类型
     * @return 平台类型列表
     */
    @Query("SELECT DISTINCT platformType FROM platform WHERE platformType IS NOT NULL ORDER BY platformType ASC")
    fun getAllPlatformTypes(): Flow<List<PlatformType>>

    /**
     * 更新单个平台的排序索引
     * @param id 平台ID
     * @param sortIndex 新的排序索引
     */
    @Query("UPDATE platform SET sortIndex = :sortIndex WHERE id = :id")
    suspend fun updateSortIndex(id: Int, sortIndex: Int)

    /**
     * [排序] 将所有已存在平台的 sortIndex 整体 +1
     * 让后续插入的新平台以 sortIndex=0 位于最上方
     */
    @Query("UPDATE platform SET sortIndex = sortIndex + 1")
    suspend fun bumpAllSortIndices()

    /**
     * [排序] 以事务方式插入新平台到最上方
     * 先将所有已有平台的 sortIndex 整体 +1，再插入新平台（sortIndex=0）
     * 保证两步操作的原子性，避免并发下出现顺序错误
     */
    @Transaction
    suspend fun insertAtTop(platform: Platform) {
        // 先整体 +1，让后续插入的新平台处于最上方
        bumpAllSortIndices()
        // 插入新平台（默认 sortIndex=0）
        insert(platform)
    }
}
