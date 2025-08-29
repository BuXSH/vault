package com.example.vault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * 简化版顶部菜单栏组件
 * 包含左边的菜单按钮、中间的搜索框（限制两个字）和右边的空圆形按钮
 * 
 * @param modifier 修饰符
 * @param backgroundColor 背景颜色
 * @param height 菜单栏高度
 * @param onMenuClick 菜单按钮点击事件
 * @param onSearchTextChange 搜索文字变化事件
 * @param onRightButtonClick 右侧按钮点击事件
 * @param searchPlaceholder 搜索框占位符文字
 */
@Composable
fun TopMenuBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    height: Int = 56,
    onMenuClick: () -> Unit = {},
    onSearchTextChange: (String) -> Unit = {},
    onRightButtonClick: () -> Unit = {},
    searchPlaceholder: String = "搜索"
) {
    var searchText by remember { mutableStateOf("") }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左边：菜单按钮
            // 后续的其他设置功能
            IconButton(
                onClick = onMenuClick
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "菜单",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // 中间：搜索框（限制两个字）
            // 搜索之后，搜索结果会展示在主界面TopMenuBar下方
            BasicTextField(
                value = searchText,
                onValueChange = { newText ->
                    // 限制输入最多两个字符
                    if (newText.length <= 2) {
                        searchText = newText
                        onSearchTextChange(newText)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    if (searchText.isEmpty()) {
                        Text(
                            text = searchPlaceholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            )
            
            // 右边：空的圆形按钮
            // 用作账号分类按钮，点击后可选择不同的分类，然后根据结果展示在主页面的TopMenuBar下方
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onRightButtonClick() },
                contentAlignment = Alignment.Center
            ) {
                // 暂时为空，不显示任何内容
            }
        }
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
                onSearchTextChange = { /* 搜索文字变化 */ },
                onRightButtonClick = { /* 右侧按钮点击事件 */ }
            )
        }
    }
}