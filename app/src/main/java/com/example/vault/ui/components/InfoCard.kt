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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Account导入已移除，PlatformCard不再直接使用Account类型



/**
 * 平台卡片组件 - 用于展示平台标题容器
 * @param platformName 平台名称
 * @param accountCount 账号数量
 * @param modifier 修饰符
 */
@Composable
fun PlatformCard(
    platformName: String,
    accountCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 平台名称标题
            Text(
                text = "$platformName (${accountCount}个账号)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 账号信息卡片列表
            // InfoCard组件已移除，需要在主界面中单独添加
        }
    }
}

/**
 * 信息卡片组件，用于展示账号密码信息
 * @param platformName 平台名称
 * @param password 密码
 * @param account 登录账号（可选）
 * @param remark 备注（可选）
 * @param onDelete 删除当前卡片的回调函数（可选）
 * @param onSwipeLeft 向左滑动回调函数（可选）
 * @param onSwipeRight 向右滑动回调函数（可选）
 * @param modifier 修饰符
 */
@Composable
fun InfoCard(
    platformName: String,
    password: String,
    account: String? = null,
    remark: String? = null,
    onDelete: (() -> Unit)? = null,
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // 滑动结束时的处理逻辑
                    }
                ) { change, dragAmount ->
                    // 检测水平滑动
                    val horizontalDrag = dragAmount.x
                    val threshold = 100f // 滑动阈值
                    
                    if (abs(horizontalDrag) > threshold) {
                        if (horizontalDrag > 0) {
                            // 向右滑动
                            onSwipeRight?.invoke()
                        } else {
                            // 向左滑动 - 删除当前InfoCard
                            onDelete?.invoke()
                            onSwipeLeft?.invoke()
                        }
                    }
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 3.dp)
        ) {
            // 备注信息（如果有）
            if (!remark.isNullOrBlank()) {
                InfoRow(
                    label = "备注",
                    value = remark,

                    onCopy = { /* 备注一般不需要复制功能 */ }
                )
            }
            
            // 登录账号信息（如果有）
            if (!account.isNullOrBlank()) {
                InfoRow(
                    label = "账号",
                    value = account,
                    onCopy = { ClipboardUtils.copyToClipboard(context, account, "账号") }
                )
            }

            // 密码信息
            PasswordRow(
                value = password,
                isVisible = isPasswordVisible,
                onVisibilityToggle = { isPasswordVisible = !isPasswordVisible },
                onCopy = { ClipboardUtils.copyToClipboard(context, password, "密码") }
            )
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
    onCopy: () -> Unit,
    label: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            onCopy()
                        }
                    )
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onVisibilityToggle()
                    },
                    onLongPress = {
                        onCopy()
                    }
                )
            }
    ) {
        Text(
            text = if (isVisible) value else "••••••••",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
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
            platformName = "GitHub",
            account = "user@example.com",
            password = "mypassword123",
            remark = "个人开发账号",
            onDelete = { /* 删除当前账号逻辑 */ },
            onSwipeLeft = { /* 向左滑动逻辑 */ },
            onSwipeRight = { /* 向右滑动逻辑 */ }
        )
    }
}

/**
 * 预览组件 - PlatformCard
 */
@Preview(showBackground = true)
@Composable
fun PlatformCardPreview() {
    MaterialTheme {
        PlatformCard(
            platformName = "微信",
            accountCount = 2,
            modifier = Modifier.padding(16.dp)
        )
    }
}