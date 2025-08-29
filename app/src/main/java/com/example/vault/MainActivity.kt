package com.example.vault

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vault.ui.components.AddAccountCard
import com.example.vault.ui.components.TopMenuBar
import com.example.vault.data.entity.Account
import com.example.vault.data.viewmodel.AccountViewModel
import com.example.vault.ui.components.PlatformCard
import com.example.vault.ui.components.InfoCard

import com.example.vault.ui.theme.VaultTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VaultTheme {
                MainContent()
            }
        }
    }
}

/**
 * 主要内容组件，负责显示应用的主界面
 * 包含顶部菜单栏和右下角悬浮按钮，点击悬浮按钮可打开添加账号卡片
 */
@Composable
fun MainContent() {
    val hapticFeedback = LocalHapticFeedback.current
    var showAddAccountCard by remember { mutableStateOf(false) }
    
    // 获取ViewModel实例
    val accountViewModel: AccountViewModel = viewModel()
    
    // 观察所有账号数据
    val allAccounts by accountViewModel.allAccounts.observeAsState(emptyList())
    val isLoading by accountViewModel.isLoading.observeAsState(false)
    
    // 用于显示账号信息的状态
    var displayText by remember { mutableStateOf("") }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 主页面内容
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        // 触发振动反馈
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        // 切换添加账号卡片的显示状态
                        showAddAccountCard = !showAddAccountCard
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
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(top = 8.dp)
            ) {
                TopMenuBar()
                
                // TODO: 添加新的账号展示组件
                
                // 添加PlatformCard测试
                PlatformCard(
                    platformName = "GitHub",
                    accountCount = 2,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                // 在PlatformCard下方添加对应的InfoCard组件
                InfoCard(
                    platformName = "GitHub",
                    password = "password123",
                    account = "user@example.com",
                    remark = "个人开发账号",
                    onDelete = { /* 删除GitHub个人账号逻辑 */ },
                    onSwipeLeft = { /* 向左滑动逻辑 */ },
                    onSwipeRight = { /* 向右滑动逻辑 */ },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                
                InfoCard(
                    platformName = "GitHub",
                    password = "workpass456",
                    account = "work@company.com",
                    remark = "工作账号",
                    onDelete = { /* 删除GitHub工作账号逻辑 */ },
                    onSwipeLeft = { /* 向左滑动逻辑 */ },
                    onSwipeRight = { /* 向右滑动逻辑 */ },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                
                // 添加独立的InfoCard测试
                InfoCard(
                    platformName = "微信",
                    password = "wechat123",
                    account = "13812345678",
                    remark = "个人微信账号",
                    onDelete = { /* 删除微信账号逻辑 */ },
                    onSwipeLeft = { /* 向左滑动逻辑 */ },
                    onSwipeRight = { /* 向右滑动逻辑 */ },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )


            }
        }
        
        // 添加账号卡片叠加层（带动画）
        AnimatedVisibility(
            visible = showAddAccountCard,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        // 点击背景区域关闭卡片
                        showAddAccountCard = false
                    }
            ) {
                AddAccountCard(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(top = 80.dp) // 为顶部菜单栏留出空间
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            // 阻止点击事件传播到背景
                        },
                    onSave = { account ->
                         // 保存账号到数据库
                         accountViewModel.saveAccount(account)
                         showAddAccountCard = false
                     },
                    onCancel = {
                        showAddAccountCard = false
                    }
                )
            }
        }
    }
}