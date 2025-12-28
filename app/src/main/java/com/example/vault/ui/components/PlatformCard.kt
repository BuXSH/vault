package com.example.vault.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.onSizeChanged
// 引入平台类型枚举，用于根据类型选择不同颜色
import com.example.vault.data.entity.PlatformType
// 颜色类型
import androidx.compose.ui.graphics.Color
// 长文本省略号支持
import androidx.compose.ui.text.style.TextOverflow
// 垂直对齐枚举
import androidx.compose.ui.Alignment
// 不再使用拖拽手柄图标，交互改为“长按即可拖动”

@Composable
fun PlatformCard(
    // [调试] 当前卡片的 sortIndex，用于日志打印
    sortIndex: Int,
    platformName: String,
    // 平台类型（枚举），用于右侧展示并着色；为空则不显示类型
    platformType: PlatformType? = null,
    // 长按类型文本时触发的类型修改回调（可选）
    onChangePlatformType: ((PlatformType) -> Unit)? = null,
    // [排序] 是否处于“排序模式”：为 true 时启用卡片表面拖拽手势
    isReorderMode: Boolean = false,
    // [排序] 拖拽开始回调：进入拖拽时触发（用于记录拖拽项ID与触觉反馈）
    onDragStart: (() -> Unit)? = null,
    // [排序] 拖拽过程回调：传递垂直位移增量 dy（像素）
    onDrag: ((Float) -> Unit)? = null,
    // [排序] 拖拽结束/取消回调：用于持久化排序与退出排序模式
    onDragEnd: (() -> Unit)? = null,
    // [排序] 尺寸上报回调：用于外层计算动态拖拽阈值（像素高度）
    onMeasured: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    // 控制右侧类型下拉菜单的展开状态
    var isTypeMenuExpanded by remember { mutableStateOf(false) }
    // 读取系统触觉反馈对象，用于长按时触发振动反馈
    val hapticFeedback = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            // [排序] 启用“长按不松手直接拖动”的排序交互：无需预先进入排序模式
            // [排序] 在渲染后上报卡片实际高度（像素），用于动态阈值计算与抵消跳变
            .onSizeChanged { onMeasured?.invoke(it.height) }
            // [调试] 仅长按不拖动也打印日志，避免被父级滚动竞争导致拖拽开始未触发
            // .pointerInput(sortIndex) {
            //     detectTapGestures(
            //         onLongPress = {
            //             Log.d("PlatformCard", "仅长按（未拖动），sortIndex = $sortIndex")
            //         }
            //     )
            // }
            .pointerInput(Unit) {
                // [排序] 在卡片表面监听“长按后拖拽”，并将位移与开始/结束事件回传给外层
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        // [排序] 通知外层开始拖拽
                        onDragStart?.invoke()
                        // [排序] 长按后触发振动反馈，提醒用户已进入排序模式
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        // [调试] 打印当前按住的卡片 sortIndex，便于定位排序/交互问题
                        Log.d("PlatformCard", "长按开始，sortIndex = $sortIndex")
                    },
                    onDrag = { _, dragAmount ->
                        // [排序] 仅传递垂直方向的位移增量（像素）
                        onDrag?.invoke(dragAmount.y)
                        // [调试] 打印拖拽中的位移增量，辅助判断是否发生拖拽，以及被谁拦截
                        Log.d("PlatformCard", "拖拽中，sortIndex = $sortIndex, dy = ${dragAmount.y}")
                    },
                    onDragEnd = {
                        // [排序] 正常结束拖拽：通知外层进行持久化与状态复位
                        onDragEnd?.invoke()
                        // [调试] 拖拽结束日志，辅助确认整个拖拽生命周期
                        Log.d("PlatformCard", "拖拽结束，sortIndex = $sortIndex")
                    },
                    onDragCancel = {
                        // [排序] 取消拖拽同样需要复位外层状态
                        onDragEnd?.invoke()
                        // [调试] 拖拽被取消（可能被父级滚动或其他手势竞争中断）
                        Log.d("PlatformCard", "拖拽取消，sortIndex = $sortIndex")
                    }
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 30.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, bottom = 4.dp, end = 16.dp)
        ) {
            // 顶部标题行：左侧展示平台名称，右侧展示平台类型
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                // 垂直居中，避免左右不对齐
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 平台名称（左侧加粗显示）
                Text(
                    text = platformName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    // 当文本过长时，使用权重占满剩余空间，并单行省略
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // 平台类型（右侧辅助信息；存在时显示，并根据类型使用不同颜色突出）
                if (platformType != null) {
                    // 使用 Box 作为锚点容器，从而在长按时弹出下拉菜单
                    Box {
                        // 使用白色背景，文字按类型使用不同颜色；支持长按打开选择菜单
                        Text(
                            text = platformType.displayName,
                            fontSize = 14.sp,
                            // 加粗以提升可读性与视觉权重
                            fontWeight = FontWeight.Bold,
                            // 文本颜色改为不同类型对应的颜色
                            color = platformTypeColor(platformType),
                            modifier = Modifier
                                // 背景改为白色
                                .background(color = Color.White, shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                // 添加长按手势，打开下拉菜单以修改平台类型
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            // [调试] 右侧类型文本长按触发（该子组件会消费事件）
                                            Log.d("PlatformCard", "类型文本长按触发，sortIndex = $sortIndex，将展开类型菜单")
                                            // 长按触发振动反馈
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            // 展开类型选择菜单
                                            isTypeMenuExpanded = true
                                        }
                                    )
                                }
                        )
                        // 下拉菜单：列出所有平台类型，选择后触发回调更新
                        DropdownMenu(
                            expanded = isTypeMenuExpanded,
                            onDismissRequest = {
                                // [调试] 类型菜单关闭（可能是点击空白或返回键）
                                Log.d("PlatformCard", "类型菜单关闭，sortIndex = $sortIndex")
                                isTypeMenuExpanded = false
                            }
                        ) {
                            PlatformType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.displayName) },
                                    onClick = {
                                        // [调试] 选择了某个类型，回调外层保存类型
                                        Log.d("PlatformCard", "类型选择：${type.displayName}，sortIndex = $sortIndex")
                                        isTypeMenuExpanded = false
                                        // 回调给外层以保存修改（若提供）
                                        onChangePlatformType?.invoke(type)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            content()
        }
    }
}

// 根据平台类型返回不同的颜色，用于类型文本着色以区分
@Composable
private fun platformTypeColor(type: PlatformType): Color {
    // 为常见类型指定具有辨识度的颜色；可按需调整为符合品牌的色板
    return when (type) {
        PlatformType.社交 -> Color(0xFF42A5F5) // 蓝色，用于社交
        PlatformType.学习 -> Color(0xFF66BB6A) // 绿色，用于学习
        PlatformType.工作 -> Color(0xFFAB47BC) // 紫色，用于工作
        PlatformType.娱乐 -> Color(0xFFFF7043) // 橙色，用于娱乐
        PlatformType.金融 -> Color(0xFF8D6E63) // 棕色，用于金融
        PlatformType.支付 -> Color(0xFFFFC107) // 琥珀色，用于支付
        PlatformType.交通 -> Color(0xFF26C6DA) // 青色，用于交通
        PlatformType.购物 -> Color(0xFFFF5252) // 红色，用于购物
        PlatformType.其他 -> MaterialTheme.colorScheme.onSurfaceVariant // 默认辅助色
    }
}

// @Preview(showBackground = true)
// @Composable
// fun PlatformCardPreview() {
//     MaterialTheme {
//         PlatformCard(
//             sortIndex = 0,
//             platformName = "微信微信发士大夫士大夫1发发发发发发发发费时费力康师傅",
//             // 传入平台类型显示名称到卡片右侧
//             platformType = PlatformType.社交,
//             modifier = Modifier.padding(vertical = 2.dp)
//         )
//     }
// }
