package com.example.vault.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast

/**
 * 剪贴板工具类
 * 提供复制文本到剪贴板的功能，包含振动反馈和Toast提示
 */
object ClipboardUtils {
    
    /**
     * 复制文本到剪贴板的工具函数
     * @param context Android上下文
     * @param text 要复制的文本
     * @param label 剪贴板标签
     */
    fun copyToClipboard(context: Context, text: String, label: String = "复制内容") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        
        // 添加振动反馈
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
        
        Toast.makeText(context, "${label}已复制到剪贴板", Toast.LENGTH_SHORT).show()
    }
}