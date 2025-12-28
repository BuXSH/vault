package com.example.vault

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
// 移除旧的叠加卡片组件导入
import com.example.vault.ui.components.TopMenuBar
import com.example.vault.ui.components.PlatformCard
import com.example.vault.ui.components.InfoCard
import com.example.vault.ui.screens.EditAccountCard
import com.example.vault.ui.theme.VaultTheme
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.vault.data.viewmodel.AccountViewModel
import com.example.vault.data.entity.Account
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.animateFloatAsState
import com.example.vault.data.entity.PlatformType
// 生物识别导入：用于应用启动时进行身份验证
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
// 为兼容 BiometricPrompt(this, executor, callback) 构造，需要使用 FragmentActivity
import androidx.fragment.app.FragmentActivity
import android.widget.Toast

// 将 MainActivity 改为 FragmentActivity，以匹配 BiometricPrompt 构造函数要求
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 应用启动时进行生物识别验证，通过后才加载主内容
            var isAuthenticated by remember { mutableStateOf(false) }
            // 在首次进入组合时发起生物识别验证
            LaunchedEffect(Unit) {
                // 通过 Activity 扩展函数触发生物识别验证
                this@MainActivity.showBiometricPrompt { success ->
                    isAuthenticated = success
                }
            }
            // UI：未通过验证时显示锁屏与重试按钮；验证成功后加载主内容
            VaultTheme {
                if (isAuthenticated) {
                    AppNav()
                } else {
                    // 简单锁屏界面：提示需要验证，并提供重试按钮
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.06f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "需要生物识别验证才能进入", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = {
                                // 重新触发生物识别验证
                                this@MainActivity.showBiometricPrompt { success ->
                                    isAuthenticated = success
                                }
                            }) {
                                Text("重新验证")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 生物识别弹窗扩展函数：在任意 FragmentActivity 上调用，用于进行二次确认等强操作
 * 支持指纹/面部/设备凭据，允许设备凭据作为回退（锁屏密码/图案）
 * @param onResult 验证结果回调，true 表示成功，false 表示失败或取消
 */
fun FragmentActivity.showBiometricPrompt(onResult: (Boolean) -> Unit) {
    // 使用主线程执行器创建 BiometricPrompt
    val executor = ContextCompat.getMainExecutor(this)
    val prompt = BiometricPrompt(
        this,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            // 验证成功
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onResult(true)
            }
            // 生物识别失败（识别到不匹配的指纹/人脸等）
            override fun onAuthenticationFailed() {
                onResult(false)
            }
            // 错误或取消（包括用户主动取消、系统错误等）
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onResult(false)
            }
        }
    )
    // 配置弹窗信息（标题、副标题），并允许设备凭据作为回退
    val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
        .setTitle("安全确认")
        .setSubtitle("请进行生物识别或设备凭据确认操作")
        // 允许设备凭据（稳定版 API）
        .setDeviceCredentialAllowed(true)
    val promptInfo = promptInfoBuilder.build()
    // 显示验证弹窗
    prompt.authenticate(promptInfo)
}

/**
 * 主要内容组件，负责显示应用的主界面
 * 包含顶部菜单栏和右下角悬浮按钮，点击悬浮按钮可打开添加账号卡片
 */
@Composable
fun AppNav() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { MainContent(navController) }
        composable("add_account") {
            EditAccountCard(onBack = { navController.navigateUp() })
        }
        composable(
            route = "edit_account/{accountId}",
            arguments = listOf(navArgument("accountId") { type = NavType.IntType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getInt("accountId") ?: 0
            EditAccountCard(accountId = accountId, onBack = { navController.navigateUp() })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainContent(navController: NavController) {
    val hapticFeedback = LocalHapticFeedback.current
    // [排序] 将 16dp 的卡片间距转换为像素值，用于交换阈值计算
    val density = LocalDensity.current
    val cardSpacingPx = with(density) { 16.dp.toPx() }
    // 获取上下文用于触发生物识别与弹出 Toast
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val accountViewModel: AccountViewModel = viewModel()
    val allAccounts by accountViewModel.allAccounts.observeAsState(emptyList())
    val allPlatforms by accountViewModel.allPlatforms.observeAsState(emptyList())
    // 观察搜索结果：由 ViewModel 暴露的全文搜索结果列表
    val searchResults by accountViewModel.searchResults.observeAsState(emptyList())
    val isLoading by accountViewModel.isLoading.observeAsState(false)
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    // 顶部菜单栏右侧类型筛选（null 表示“全部”）
    var selectedTypeFilter by remember { mutableStateOf<PlatformType?>(null) }
    // 当前搜索关键字（空则显示全部数据；非空则仅显示搜索结果）
    var searchKeyword by remember { mutableStateOf("") }
    // [排序] 排序模式标记：长按卡片开始时进入，后续用于控制交互与渲染
    var isReorderMode by remember { mutableStateOf(false) }
    // [排序] 当前拖拽的平台ID（null 表示未拖拽）
    var draggingPlatformId by remember { mutableStateOf<Int?>(null) }
    // [排序] 当前拖拽项进入排序模式时的初始索引
    var draggingStartIndex by remember { mutableStateOf<Int?>(null) }
    // [排序] 记录各平台卡片的实际高度（像素），用于计算拖拽交换阈值
    val platformHeightsPx = remember { mutableStateMapOf<Int, Int>() }
    // [排序] 当前渲染的顺序（平台ID序列），排序模式下根据拖拽实时更新
    val orderedIds = remember { mutableStateListOf<Int>() }
    // [排序] 累计垂直位移（像素），按平台ID记录，用于计算跨卡片交换步数
    val dragAccumulatedDy = remember { mutableStateMapOf<Int, Float>() }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 主页面内容
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                // 当详情弹窗打开时，对主体内容施加模糊以弱化背景
                .then(if (selectedAccount != null) Modifier.blur(8.dp) else Modifier),
            // 顶部固定菜单栏，将 TopMenuBar 放入 Scaffold 的 topBar
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    // 传入 statusBarsPadding 以适配沉浸状态栏
                    TopMenuBar(
                        modifier = Modifier.statusBarsPadding(),
                        // 当前筛选类型（null 为“全部”）
                        currentType = selectedTypeFilter,
                        // 选择类型后更新筛选状态
                        onTypeSelected = { selectedTypeFilter = it },
                        // 搜索文本变化：非空触发全文搜索；为空清空搜索结果
                        onSearchTextChange = { newText ->
                            val keyword = newText.trim()
                            searchKeyword = keyword
                            if (keyword.isNotEmpty()) {
                                // 触发 ViewModel 的全文搜索（平台名、账号、备注、电话、邮箱）
                                accountViewModel.searchAccounts(keyword)
                            } else {
                                // 清空搜索结果，恢复展示全部数据
                                accountViewModel.clearSearchResults()
                            }
                        },
                        // 关闭搜索：取消进行中的搜索任务并恢复展示全部数据
                        onSearchCancel = {
                            accountViewModel.cancelSearch()
                            searchKeyword = ""
                        }
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        // 触发振动反馈
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        // 离开主页前取消任何进行中的搜索任务与 loading，避免返回后进度条仍在转动
                        accountViewModel.cancelSearch()
                        searchKeyword = ""
                        // 导航到添加账号页面
                        navController.navigate("add_account")
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加新账号"
                    )
                }
            }
        )
        { paddingValues ->
            // 根据搜索关键字选择数据源：非空使用搜索结果，空则使用全部账号
            val accountsToUse = if (searchKeyword.isNotEmpty()) searchResults else allAccounts
            // 先按类型筛选平台（null 显示全部）
            var filteredPlatforms = if (selectedTypeFilter == null) {
                allPlatforms
            } else {
                allPlatforms.filter { it.platformType == selectedTypeFilter }
            }
            // 若处于搜索模式，仅展示含有搜索结果账号的相关平台
            if (searchKeyword.isNotEmpty()) {
                val platformIdsWithAccounts = accountsToUse.map { it.platformId }.toSet()
                filteredPlatforms = filteredPlatforms.filter { it.id in platformIdsWithAccounts }
            }
            // 渲染平台序列：
            // - 非排序模式：直接使用 filteredPlatforms
            // - 排序模式：按 orderedIds 排序渲染
            val listToRender = if (isReorderMode && orderedIds.isNotEmpty()) {
                orderedIds.mapNotNull { id -> filteredPlatforms.firstOrNull { it.id == id } }
            } else {
                filteredPlatforms
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(top = 0.dp),
                // 修复 PaddingValues 参数错误：horizontal/top/bottom 组合不匹配任何重载
                // 使用 start/end/top/bottom 四参数版本以实现左右 16dp、上 8dp、下 24dp
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 8.dp,
                    end = 16.dp,
                    bottom = 94.dp
                )
            ) {
                if (isLoading) {
                    item {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
                if (allPlatforms.isEmpty()) {
                    item {
                        Text(
                            text = "暂无平台数据",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(listToRender, key = { it.id }) { platform ->
                        // 平台对应的账号列表：搜索模式下使用搜索结果，否则使用全部账号
                        val platformAccounts = accountsToUse.filter { it.platformId == platform.id }
                        // [排序] 是否为当前拖拽项（用于视觉过渡效果）
                        val isDragging = isReorderMode && draggingPlatformId == platform.id
                        // [排序] 目标位移（像素）：拖拽项跟随累计位移，其他项为0
                        val targetOffsetPx = if (isDragging) (dragAccumulatedDy[platform.id] ?: 0f) else 0f
                        // [排序] 动画过渡：位移与缩放使用弹簧动画以获得顺滑效果
                        val animatedOffsetPx by animateFloatAsState(
                            targetValue = targetOffsetPx,
                            animationSpec = spring(stiffness = Spring.StiffnessLow)
                        )
                        val targetScale = if (isDragging) 1.03f else 1f
                        val animatedScale by animateFloatAsState(
                            targetValue = targetScale,
                            animationSpec = spring(stiffness = Spring.StiffnessLow)
                        )
                        PlatformCard(
                            sortIndex = platform.sortIndex,
                            platformName = platform.platformName,
                            // 传入平台类型（用于右侧按类型着色）
                            platformType = platform.platformType,
                            // 排序模式标记：传给卡片以控制交互与视觉反馈
                            isReorderMode = isReorderMode,
                            // 长按右侧类型文本时，弹出菜单并在选择后更新平台类型
                            onChangePlatformType = { newType ->
                                // 调用 ViewModel 保存平台类型更新
                                accountViewModel.updatePlatformType(platform, newType)
                            },
                            // [排序] 长按开始：在“全部且未搜索”场景进入排序模式
                            onDragStart = {
                                val canReorder = selectedTypeFilter == null && searchKeyword.isEmpty()
                                if (canReorder) {
                                    isReorderMode = true
                                    // 初始化有序ID列表（进入排序模式时以当前渲染顺序为准）
                                    if (orderedIds.isEmpty()) {
                                        orderedIds.clear()
                                        orderedIds.addAll(listToRender.map { it.id })
                                        Log.d("Reorder", "orderedIds初始化=${orderedIds.joinToString(",")}")
                                    }
                                    // 记录当前拖拽项信息（ID、初始索引）
                                    draggingPlatformId = platform.id
                                    draggingStartIndex = orderedIds.indexOf(platform.id).takeIf { it >= 0 }
                                    // 重置累计位移
                                    dragAccumulatedDy[platform.id] = 0f
                                    // [排序] 打印进入排序模式后的状态
                                    // Log.d("Reorder", """
                                    //     排序模式=$isReorderMode,
                                    //     平台名称=${platform.platformName},
                                    //     平台ID=${platform.id},
                                    //     拖拽项ID=$draggingPlatformId,
                                    //     初始索引=$draggingStartIndex,
                                    //     sortIndex=${platform.sortIndex}
                                    // """.trimIndent())
                                }
                            },
                            // [排序] 拖拽过程：根据累计位移与卡片高度阈值触发邻接交换
                            onDrag = { dy ->
                                // [排序] 若未处于排序模式，忽略拖拽事件
                                if (!isReorderMode) return@PlatformCard
                                // [排序] 获取当前正在拖拽的平台ID，若为空则忽略
                                val draggingId = draggingPlatformId ?: return@PlatformCard
                                // 累计位移（像素）
                                val acc = (dragAccumulatedDy[draggingId] ?: 0f) + dy
                                // [排序] 更新该平台的累计位移，用于后续阈值判断
                                dragAccumulatedDy[draggingId] = acc
                                // [排序] 计算该拖拽项在当前有序ID列表中的索引
                                val currentIndex = orderedIds.indexOf(draggingId)
                                // [排序] 索引不存在（安全校验），直接忽略本次拖拽事件
                                if (currentIndex < 0) return@PlatformCard
                                // [排序] 相邻卡片高度（像素），用于方向性阈值计算
                                val aboveId = orderedIds.getOrNull(currentIndex - 1)
                                val belowId = orderedIds.getOrNull(currentIndex + 1)
                                val aboveH = aboveId?.let { platformHeightsPx[it] }
                                val belowH = belowId?.let { platformHeightsPx[it] }
                                // [排序] 自身高度与平均高度作为兜底（当相邻高度不可用时）
                                val selfH = platformHeightsPx[draggingId]
                                val avgH = if (platformHeightsPx.isNotEmpty()) {
                                    platformHeightsPx.values.average().toFloat()
                                } else null
                                val fallbackH = (selfH ?: avgH?.toInt() ?: 120).toFloat()
                                // [排序] 方向性交换阈值：
                                // - 向下移动使用“下方卡片高度的一半 + 卡片间距(16dp)”
                                // - 向上移动使用“上方卡片高度的一半 + 卡片间距(16dp)”
                                val thresholdDown = ((belowH?.toFloat() ?: fallbackH) / 2f) + cardSpacingPx
                                val thresholdUp = ((aboveH?.toFloat() ?: fallbackH) / 2f) + cardSpacingPx
                                Log.d("Reorder", "当前累计位移=$acc, 阈值(上)=$thresholdUp, 阈值(下)=$thresholdDown")
                                // 计算需要跨越的“步数”（向上为负，向下为正）
                                var steps = 0
                                if (acc >= thresholdDown) {
                                    steps = (acc / thresholdDown).toInt()
                                } else if (acc <= -thresholdUp) {
                                    steps = (acc / thresholdUp).toInt() // 负数
                                }
                                Log.d("Reorder", "计算得到步数=$steps")
                                if (steps != 0) {
                                    // 将拖拽项按步数逐步移动，防止一次跨越过多导致错乱
                                    var targetIndex = currentIndex + steps
                                    // 边界保护
                                    targetIndex = targetIndex.coerceIn(0, orderedIds.lastIndex)
                                    // 执行移动：先移除，再插入目标位置
                                    if (targetIndex != currentIndex) {
                                        // [排序] 从当前位置移除拖拽项
                                        orderedIds.removeAt(currentIndex)
                                        // [排序] 将拖拽项插入到目标位置
                                        orderedIds.add(targetIndex, draggingId)
                                        // 抵消已消耗的位移，避免重复触发
                                        val stepThreshold = if (steps > 0) thresholdDown else thresholdUp
                                        val consumed = steps * stepThreshold
                                        // [排序] 更新累计位移为剩余未消耗的部分
                                        dragAccumulatedDy[draggingId] = acc - consumed
                                    }
                                }
                            },
                            // [排序] 长按/拖拽结束：退出排序模式并打印日志
                            onDragEnd = {
                                if (isReorderMode) {
                                    isReorderMode = false
                                    // 持久化最终顺序（ID 序列）为 sortIndex=0..N-1
                                    if (orderedIds.isNotEmpty()) {
                                        accountViewModel.reorderPlatforms(orderedIds.toList())
                                    }
                                    // 清理拖拽相关状态
                                    draggingPlatformId = null
                                    draggingStartIndex = null
                                    dragAccumulatedDy.clear()
                                    // Log.d("Reorder", """
                                    //     排序模式=$isReorderMode,
                                    //     平台名称=${platform.platformName},
                                    //     平台ID=${platform.id},
                                    //     拖拽项ID=$draggingPlatformId,
                                    //     sortIndex=${platform.sortIndex}
                                    // """.trimIndent())
                                }
                            },
                            // [排序] 上报卡片实际高度（像素），用于动态阈值计算
                            onMeasured = { hPx ->
                                platformHeightsPx[platform.id] = hPx
                                // [排序] 单行打印卡片高度与平台信息，便于调试
                                Log.d("Reorder", "平台名称=${platform.platformName}, 平台ID=${platform.id}, 高度（像素）=$hPx")
                            },
                            modifier = Modifier
                                .padding(vertical = 0.dp)
                                // [排序] 拖拽项的视觉过渡：位移、缩放、阴影、置顶
                                .graphicsLayer {
                                    translationY = animatedOffsetPx
                                    scaleX = animatedScale
                                    scaleY = animatedScale
                                    // 提升阴影以强调拖拽项（像素）
                                    shadowElevation = if (isDragging) with(density) { 8.dp.toPx() } else 0f
                                }
                                .zIndex(if (isDragging) 1f else 0f)
                        ) {
                            platformAccounts.forEach { acc ->
                                InfoCard(
                                    password = acc.password,
                                    account = acc.account,
                                    remark = acc.remark,
                                    onMoreClick = { selectedAccount = acc },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        // 叠加一层半透明遮罩以进一步弱化背景（在Dialog之下、内容之上）
        if (selectedAccount != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )
        }
        // Dialog 账号详细信息弹窗
        if (selectedAccount != null) {
            val acc = selectedAccount!!
            val platformName = allPlatforms.firstOrNull { it.id == acc.platformId }?.platformName ?: "未知平台"
            Dialog(onDismissRequest = { selectedAccount = null }) {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(top = 16.dp, start = 16.dp, bottom = 8.dp, end = 16.dp)
                            .widthIn(min = 280.dp)
                    ) {
                        Text(
                            text = platformName,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (!acc.remark.isNullOrBlank()) {
                            Text(text = "备注: ${acc.remark}", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (!acc.account.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "账号: ${acc.account}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "密码: ${acc.password}", style = MaterialTheme.typography.bodyMedium)
                        if (!acc.payPassword.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "支付密码: ${acc.payPassword}", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (!acc.phone.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "手机号: ${acc.phone}", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (!acc.email.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "邮箱: ${acc.email}", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (!acc.idNumber.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "身份证: ${acc.idNumber}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = {
                                    // 点击删除时先触发振动反馈，增强交互手感
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    // 点击删除时进行二次确认：触发生物识别验证
                                    val activity = context as? FragmentActivity
                                    if (activity == null) {
                                        // 兜底处理：无法获取 FragmentActivity，提示未执行删除
                                        Toast.makeText(context, "当前环境不支持生物识别确认", Toast.LENGTH_SHORT).show()
                                        return@TextButton
                                    }
                                    activity.showBiometricPrompt { success ->
                                        if (success) {
                                            // 验证成功：轻触振动反馈
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            // 生物识别通过后执行删除，并关闭详情弹窗
                                            selectedAccount?.let { acc ->
                                                accountViewModel.deleteAccount(acc)
                                                selectedAccount = null
                                                Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            // 验证失败或取消：重触（长按）振动反馈
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            // 验证失败或取消，不进行删除
                                            Toast.makeText(context, "验证失败或已取消，未删除", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            ) {
                                Text("删除", color = MaterialTheme.colorScheme.error)
                            }
                            Row {
                                TextButton(
                                    onClick = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        // 进入编辑页前取消搜索任务与 loading，避免返回后进度条仍在转动
                                        accountViewModel.cancelSearch()
                                        searchKeyword = ""
                                        val accId = acc.id
                                        selectedAccount = null
                                        navController.navigate("edit_account/$accId")
                                    }
                                ) {
                                    Text("修改")
                                }
                                TextButton(onClick = { selectedAccount = null }) {
                                    Text("关闭")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
