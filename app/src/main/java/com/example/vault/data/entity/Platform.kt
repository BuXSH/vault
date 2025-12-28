package com.example.vault.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(
    tableName = "platform",
    indices = [
        Index(value = ["platformName"], unique = true), // 平台名称唯一，避免重复插入
        Index(value = ["platformType"]) // 为平台类型添加索引，加速按类型筛选
    ]
)
data class Platform(
    // 主键自增（每条记录唯一标识）
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    
    // 平台相关信息
    val platformName: String, // 平台名称（如"微信"、"支付宝"、"MySQL服务器"）
    val platformType: PlatformType? = null, // 平台类型（枚举，可选）
    // 用户自定义排序索引：越小越靠前。支持拖拽重排后持久化顺序
    val sortIndex: Int = 0
    
)

enum class PlatformType(val displayName: String) {
    社交("社交"),
    学习("学习"),
    工作("工作"),
    娱乐("娱乐"),
    金融("金融"),
    支付("支付"),
    交通("交通"),
    购物("购物"),
    其他("其他");

    companion object {
        fun fromString(value: String): PlatformType? {
            return values().firstOrNull { it.displayName == value.trim() }
        }
    }
}

object PlatformTypeConverters {
    @TypeConverter
    @JvmStatic
    fun toString(type: PlatformType?): String? = type?.displayName

    @TypeConverter
    @JvmStatic
    fun fromString(value: String?): PlatformType? = value?.let { PlatformType.fromString(it) }
}
