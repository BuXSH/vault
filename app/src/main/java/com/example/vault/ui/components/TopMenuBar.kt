package com.example.vault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.focus.onFocusChanged
import com.example.vault.data.entity.PlatformType
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem

/**
 * 简化版顶部菜单栏组件
 * 包含左边的菜单按钮、中间的搜索框（限制两个字）和右边的空圆形按钮
 * 
 * @param modifier 修饰符
 * @param backgroundColor 背景颜色
 * @param onMenuClick 菜单按钮点击事件
 * @param onSearchTextChange 搜索文字变化事件
 * @param onRightButtonClick 右侧按钮点击事件
 */
@Composable
fun TopMenuBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    onMenuClick: () -> Unit = {},
    onSearchTextChange: (String) -> Unit = {},
    // 关闭搜索回调：用于结束搜索任务与清空结果
    onSearchCancel: () -> Unit = {},
    // 右侧类型筛选回调：传入选中的类型；为 null 表示“全部”
    onTypeSelected: (PlatformType?) -> Unit = {},
    // 当前筛选的类型；为 null 表示“全部”
    currentType: PlatformType? = null
) {
    var searchText by remember { mutableStateOf("") }
    // 搜索框焦点状态：用于在点击（获得焦点）时显示关闭按钮
    var isSearchFocused by remember { mutableStateOf(false) }
    // 右侧类型菜单展开状态
    var isTypeMenuExpanded by remember { mutableStateOf(false) }
    // 读取系统触觉反馈对象，用于点击筛选按钮时触发振动反馈
    val hapticFeedback = LocalHapticFeedback.current
    // 焦点管理：用于关闭搜索时主动移除焦点，结束编辑状态
    val focusManager = LocalFocusManager.current
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左边：菜单按钮
            // 功能说明：
            // - 点击触发外部传入的 onMenuClick 回调，通常用于打开设置或侧边菜单
            // - 图标使用 Material 默认的 Menu 图标，颜色跟随当前主题的 onSurface
            IconButton(
                onClick = {
                    // 轻按振动反馈：点击菜单按钮提供轻微反馈
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onMenuClick()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "菜单",
                    tint = MaterialTheme.colorScheme.onSurface,
                    // 调大图标尺寸：默认约为 24dp，这里提升到 28dp
                    // 如需更明显的增大，可将 28.dp 调整为 32.dp
                    modifier = Modifier.size(36.dp)
                )
            }
            
            // 中间：搜索框（TextField，无轮廓）
            // 使用 Material3 的 TextField，通过 colors 去掉下划线与容器背景
            TextField(
                // 当前输入框的文本内容（状态变量）
                value = searchText,
                // 文本变化的回调：用于更新本地状态并通知外部触发搜索
                onValueChange = { newText ->
                    // 仅当新文本长度不超过 20 时才更新，避免过长输入影响排版与性能
                    if (newText.length <= 20) {
                        // 更新本地输入状态
                        searchText = newText
                        // 将最新搜索关键字通知外部（例如触发 ViewModel 搜索）
                        onSearchTextChange(newText)
                    }
                },
                // 修饰符：用于控制布局尺寸、外边距与交互行为
                modifier = Modifier
                    // 在父 Row 中占据剩余空间，保证中间区域自适应宽度
                    .weight(1f)
                    // 外边距：增大水平/垂直留白，让轮廓在视觉上更“小更紧凑”
                    .padding(horizontal = 8.dp)
                    // 固定较小高度：避免默认高度导致输入框显得过大
//                    .height(40.dp)
                    // 焦点监听：记录是否获得焦点，用于控制关闭按钮显示与键盘焦点管理
                    .onFocusChanged { isSearchFocused = it.isFocused },
                // 单行输入：适合在顶部栏中展示简短关键字
                singleLine = true,
                // 文本样式：使用较小字号，配合 40dp 高度更协调
                textStyle = MaterialTheme.typography.bodyLarge,
                // 占位符：在输入为空时显示提示文案
                placeholder = {
                    // 使用占位符可确保光标位置正确（光标在占位符之前）
                    Text(
                        // 占位文本内容
                        text = "搜索",
                        // 占位文本样式与正文字体大小保持一致（小字号）
                        style = MaterialTheme.typography.bodyLarge,
                        // 使用 onSurfaceVariant，区分于正文颜色，降低视觉权重
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                // 右侧图标区域：用作关闭按钮，结束搜索任务与清空关键字
                trailingIcon = {
                    // 当输入框获得焦点或文本非空时显示关闭按钮
                    if (isSearchFocused || searchText.isNotEmpty()) {
                        // 图标按钮：承载关闭操作
                        IconButton(
                            // 点击关闭：振动反馈、清空文本、取消搜索、清除焦点与键盘
                            onClick = {
                                // 轻按振动反馈：提升交互手感
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                // 清空输入文本（本地状态）
                                searchText = ""
                                // 通知外层取消搜索任务（如取消协程、清空结果、停止 loading）
                                onSearchCancel()
                                // 通知外层文本变化为空，以恢复展示全部数据
                                onSearchTextChange("")
                                // 清除焦点：关闭键盘并结束编辑状态
                                focusManager.clearFocus()
                            }
                        ) {
                            // 关闭图标：展示“叉号”形态
                            Icon(
                                // 使用 Material 内置的 Close 图标
                                imageVector = Icons.Filled.Close,
                                // 无障碍描述：用于屏幕阅读器提示
                                contentDescription = "关闭搜索",
                                // 图标颜色：采用 onSurfaceVariant 与正文区分
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                // 图标尺寸：保持足够的点击区域与可视清晰度
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                // 配色：移除下划线与容器背景，保持与顶部栏一致的平面风格
                colors = TextFieldDefaults.colors(
//                    focusedIndicatorColor = Color.Transparent, // 获得焦点时隐藏下划线
                    unfocusedIndicatorColor = Color.Transparent, // 失去焦点时隐藏下划线
                    disabledIndicatorColor = Color.Transparent, // 禁用状态也不显示下划线
                    errorIndicatorColor = Color.Transparent, // 错误状态不显示下划线（用其他视觉提示）
                    focusedContainerColor = Color.Transparent, // 获得焦点时容器背景透明
                    unfocusedContainerColor = Color.Transparent, // 失去焦点时容器背景透明
                    disabledContainerColor = Color.Transparent, // 禁用状态容器背景透明
                    errorContainerColor = Color.Transparent, // 错误状态容器背景透明
                    focusedTextColor = MaterialTheme.colorScheme.onSurface, // 焦点中文本颜色
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // 非焦点中文本颜色
                    disabledTextColor = MaterialTheme.colorScheme.onSurface, // 禁用状态文本颜色
                    errorTextColor = MaterialTheme.colorScheme.onSurface // 错误状态文本颜色
                ),
                // 形状：采用 12dp 圆角以匹配顶部栏整体风格（容器透明时主要影响点击区域）
                // 若希望更“紧”，可将 12.dp 调小为 8.dp 或 6.dp
                shape = RoundedCornerShape(16.dp)
            )
            
            // 类型按钮
            // 替换为与 PlatformCard 类型标签一致的样式：白字 + 彩色背景，点击弹出类型选择下拉
            Box {
                // 当前显示文本：选中类型的 displayName 或 “全部”
                val typeText = currentType?.displayName ?: "全部"
                // 当前背景色：选中类型的彩色背景；“全部”使用辅助色
                val bgColor = currentType?.let { platformTypeColor(it) } ?: MaterialTheme.colorScheme.onSurfaceVariant
                Text(
                    text = typeText,
                    fontSize = 18.sp,
                    // 加粗以提升可读性与视觉权重
                    fontWeight = FontWeight.Bold,
                    // 文本颜色改为不同类型对应的颜色；“全部”为辅助色
                    color = currentType?.let { platformTypeColor(it) } ?: MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        // 背景改为白色，突出彩色文字
                        .background(Color.White)
                        // 点击触发振动反馈并展开类型选择菜单
                        .clickable {
                            // 触发振动反馈（点击手感）
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            // 展开类型选择菜单
                            isTypeMenuExpanded = true
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
                // 下拉菜单：第一项为“全部”，其余为各平台类型
                DropdownMenu(
                    expanded = isTypeMenuExpanded,
                    onDismissRequest = { isTypeMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("全部") },
                        onClick = {
                            isTypeMenuExpanded = false
                            onTypeSelected(null)
                        }
                    )
                    PlatformType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName) },
                            onClick = {
                                isTypeMenuExpanded = false
                                onTypeSelected(type)
                            }
                        )
                    }
                }
            }
        }
    }
}

// 与 PlatformCard 保持一致的类型颜色映射
@Composable
private fun platformTypeColor(type: PlatformType): Color {
    return when (type) {
        PlatformType.社交 -> Color(0xFF42A5F5)
        PlatformType.学习 -> Color(0xFF66BB6A)
        PlatformType.工作 -> Color(0xFFAB47BC)
        PlatformType.娱乐 -> Color(0xFFFF7043)
        PlatformType.金融 -> Color(0xFF8D6E63)
        PlatformType.支付 -> Color(0xFFFFC107)
        PlatformType.交通 -> Color(0xFF26C6DA)
        PlatformType.购物 -> Color(0xFFFF5252)
        PlatformType.其他 -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

/**
 * 预览组件
 */
@Preview(showBackground = true)
@Composable
fun SimpleTopMenuBarPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TopMenuBar(
                onMenuClick = { /* 菜单点击事件 */ },
                onSearchTextChange = { /* 搜索文字变化 */ }
            )
        }
    }
}
