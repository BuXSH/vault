package com.example.vault.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 合并后的账号实体：包含账号信息和平台信息
 * 将原来的Platform信息直接嵌入到Account中，简化数据结构
 */
@Entity(
    tableName = "account",
    indices = [
        Index(value = ["platformName"]), // 为平台名称添加索引，加速按平台查询
        Index(value = ["platformType"]), // 为平台类型添加索引，加速按类型筛选
        Index(value = ["account"]) // 为账号字段添加索引，加速账号查询
    ]
)
data class Account(
    // 主键自增（每条记录唯一标识）
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    
    // 平台相关信息（原Platform实体的字段）
    val platformName: String, // 平台名称（如"微信"、"支付宝"、"MySQL服务器"）
    val platformType: String? = null, // 平台类型（如"社交"、"支付"、"数据库"，可选）
    
    // 账号相关信息（原Account实体的字段）
    val remark: String? = null, // 备注（可选）
    val account: String? = null, // 登录账号（可能是手机号/邮箱）（可选）
    val encryptedPassword: String, // 加密后的密码
    val phone: String? = null, // 手机号（可选）
    val email: String? = null, // 邮箱（可选）
    val idNumber: String? = null // 身份证（可选）
)
