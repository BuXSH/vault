package com.example.vault.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "account",
    indices = [
        Index(value = ["account"]), // 为账号字段添加索引，加速账号查询
        Index(value = ["platformId"]) // 为平台外键添加索引，提升按平台查询性能
    ],
    foreignKeys = [
        ForeignKey(
            entity = Platform::class, // 关联的父实体（对应 Platform 表）
            parentColumns = ["id"], // 父表的关联字段（Platform 表的 id 主键）
            childColumns = ["platformId"], // 子表的关联字段（Account 表的 platformId 外键）
            onDelete = ForeignKey.CASCADE // 级联删除：如果 Platform 记录被删除，关联的 Account 也会被删除
        )
    ]
)
data class Account(
    // 主键自增（每条记录唯一标识）
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // 关联平台 ID（外键）
    val platformId: Int,
      
    // 账号相关信息
    val remark: String? = null, // 备注（可选）
    val account: String? = null, // 登录账号（可能是手机号/邮箱）（可选）
    val password: String, // 加密后的密码
    val payPassword: String? = null, // 支付密码（可选）
    val phone: String? = null, // 手机号（可选）
    val email: String? = null, // 邮箱（可选）
    val idNumber: String? = null // 身份证（可选）
)
