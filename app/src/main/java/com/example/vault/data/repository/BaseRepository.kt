package com.example.vault.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 基础仓库类，封装公共操作（如线程切换）
 */
abstract class BaseRepository {
    /**
     * 在 IO 线程执行挂起函数
     */
    protected suspend fun <T> ioDispatcher(block: suspend () -> T): T {
        return withContext(Dispatchers.IO) {
            block()
        }
    }
}