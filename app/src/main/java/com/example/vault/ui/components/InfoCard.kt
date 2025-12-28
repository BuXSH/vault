package com.example.vault.ui.components

import com.example.vault.utils.ClipboardUtils
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Account导入已移除，PlatformCard不再直接使用Account类型
// 颜色类型，用于将背景色统一调整为白色
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.fragment.app.FragmentActivity
import android.widget.Toast
import com.example.vault.showBiometricPrompt




/**
 * 信息卡片小组件，用于展示主要账号密码信息
 * @param password 密码
 * @param account 登录账号（可选）
 * @param remark 备注（可选）
 * @param modifier 修饰符
 */
@Composable
fun InfoCard(
    password: String,
    account: String? = null,
    remark: String? = null,
    onMoreClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    // 轻按振动反馈句柄
    val haptic = LocalHapticFeedback.current
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            // 将卡片背景色统一改为白色
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 16.dp, bottom = 4.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (!remark.isNullOrBlank()) {
                    InfoRow(
                        label = "备注",
                        value = remark
                    )
                }
                if (!account.isNullOrBlank()) {
                    InfoRow(
                        label = "账号",
                        value = account,
                        onCopy = { ClipboardUtils.copyToClipboard(context, account, "账号") }
                    )
                }
                PasswordRow(
                    value = password,
                    isVisible = isPasswordVisible,
                    onVisibilityToggle = { isPasswordVisible = !isPasswordVisible },
                    onCopy = { ClipboardUtils.copyToClipboard(context, password, "密码") }
                )
            }
            IconButton(onClick = { 
                // 点击更多按钮先触发轻按振动反馈，提升交互手感
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                // 点击更多按钮进行二次确认（生物识别），通过后再执行更多操作
                val activity = context as? FragmentActivity
                if (activity == null) {
                    // 兜底：当前环境非 FragmentActivity，不支持生物识别确认
                    Toast.makeText(context, "当前环境不支持生物识别确认", Toast.LENGTH_SHORT).show()
                } else {
                    activity.showBiometricPrompt { success ->
                        if (success) {
                            onMoreClick?.invoke()
                        } else {
                            Toast.makeText(context, "验证失败或已取消", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }) {
                Icon(imageVector = Icons.Filled.ExpandMore, contentDescription = "更多")
            }
        }
    }
}

/**
 * 信息行组件，用于显示标签和值，支持长按复制
 * @param label 标签（可选）
 * @param value 值
 * @param onCopy 复制回调
 */
@Composable
private fun InfoRow(
    value: String,
    onCopy: (() -> Unit)? = null, // 改为可空函数类型 + 默认null
    label: String? = null
) {
    // 轻/重按振动反馈句柄
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp)
            // 将信息行的背景色统一改为白色，保持与卡片一致
            .background(Color.White)
    ) {
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                // 仅当 onCopy 不为空时，才添加长按事件
                .run {
                    if (onCopy != null) {
                        this.pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    // 重按振动反馈：复制触发时给予更明显的反馈
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onCopy()
                                }
                            )
                        }
                    } else {
                        this // 无事件，返回原 Modifier
                    }
                }
        )
    }
}

/**
 * 密码行组件，支持显示/隐藏密码
 * @param value 密码值
 * @param isVisible 是否可见
 * @param onVisibilityToggle 可见性切换回调
 * @param onCopy 复制回调
 */
@Composable
private fun PasswordRow(
    value: String,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    onCopy: () -> Unit
) {
    // 轻/重按振动反馈句柄
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp)
            // 将密码行的背景色统一改为白色，保持与卡片一致
            .background(Color.White)

    ) {
        Text(
            text = if (isVisible) value else "••••••••",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            // 轻按振动反馈：切换密码可见性时提供轻微反馈
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onVisibilityToggle()
                        },
                        onLongPress = {
                            // 重按振动反馈：复制密码时提供明显反馈
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCopy()
                        }
                    )
                }
        )
    }
}

/**
 * 预览组件 - InfoCard
 */
@Preview(showBackground = true)
@Composable
fun InfoCardPreview() {
    MaterialTheme {
        InfoCard(
            password = "mypassword123"
        )
    }
}

