package com.example.vault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.input.KeyboardType
// 密码掩码显示（隐藏真实字符）
import androidx.compose.ui.text.input.PasswordVisualTransformation
// 密码可见性切换所需类型
import androidx.compose.ui.text.input.VisualTransformation
// 密码可见性图标
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import android.widget.Toast
import com.example.vault.data.viewmodel.AccountViewModel
import com.example.vault.data.entity.PlatformType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountCard(
    accountId: Int? = null, // 可选的账号ID参数：传入时可用于预填与更新逻辑
    onBack: () -> Unit
) {
    // ViewModel & UI 状态
    val accountViewModel: AccountViewModel = viewModel()
    val isLoading by accountViewModel.isLoading.observeAsState(false)
    val errorMessage by accountViewModel.errorMessage.observeAsState(null)
    val statusMessage by accountViewModel.statusMessage.observeAsState(null)
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current
    // 观察所有账号与平台，用于根据 accountId 预填数据
    val allAccounts by accountViewModel.allAccounts.observeAsState(emptyList())
    val allPlatforms by accountViewModel.allPlatforms.observeAsState(emptyList())

    // 保存成功提示与返回
    LaunchedEffect(statusMessage) {
        if (statusMessage == "保存成功") {
            Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }
    // 错误提示改为 Toast 展示
    LaunchedEffect(errorMessage) {
        errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    // 输入状态
    var platformName by remember { mutableStateOf("") }
    // 必填项错误标记：用于保存失败时高亮红色边框
    var platformNameError by remember { mutableStateOf(false) }
    var isTypeExpanded by remember { mutableStateOf(false) }
    var selectedPlatformType by remember { mutableStateOf<PlatformType?>(PlatformType.其他) }
    var remark by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }
    // 支付密码（可选）
    var payPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    // 密码与支付密码的可见性状态（默认隐藏）
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isPayPasswordVisible by remember { mutableStateOf(false) }

    // 根据传入的 accountId 查找目标账号并预填输入框
    val target = remember(accountId, allAccounts) { allAccounts.firstOrNull { it.id == accountId } }
    LaunchedEffect(target, allPlatforms) {
        if (target != null) {
            // 平台名称与类型仅用于显示，不在编辑保存中修改平台
            val platform = allPlatforms.firstOrNull { it.id == target.platformId }
            platformName = platform?.platformName ?: ""
            selectedPlatformType = platform?.platformType
            // 预填账号信息
            remark = target.remark ?: ""
            account = target.account ?: ""
            password = target.password
            payPassword = target.payPassword ?: ""
            phone = target.phone ?: ""
            email = target.email ?: ""
            idNumber = target.idNumber ?: ""
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            // 顶部固定标题栏
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            // 轻按振动反馈：返回时提供轻微反馈
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onBack()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                        Text(
                            // 顶部标题按模式显示不同文案：编辑模式为“编辑账号”，新增模式为“添加账号”
                            text = if (accountId != null && target != null) "编辑账号" else "添加账号",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            },
            // 底部固定保存按钮
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = {
                            // 轻按振动反馈：保存时提供轻微反馈
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (platformName.isBlank() || password.isBlank()) {
                                // 根据具体错误设置红色边框并弹出 Toast 提示
                                platformNameError = platformName.isBlank()
                                passwordError = password.isBlank()
                                val msg = when {
                                    platformNameError && passwordError -> "请填写平台名称和密码"
                                    platformNameError -> "请填写平台名称"
                                    else -> "请填写密码"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            localError = null
                            if (accountId != null && target != null) {
                                // 编辑模式：更新现有账号（保持原有 id 与 platformId，不修改平台）
                                val updated = target.copy(
                                    remark = remark.ifBlank { null },
                                    account = account.ifBlank { null },
                                    password = password,
                                    payPassword = payPassword.ifBlank { null },
                                    phone = phone.ifBlank { null },
                                    email = email.ifBlank { null },
                                    idNumber = idNumber.ifBlank { null }
                                )
                                accountViewModel.saveAccount(updated)
                            } else {
                                // 新增模式：与原添加页一致，支持创建平台并保存账号
                                accountViewModel.saveAccountWithPlatform(
                                    platformName = platformName.trim(),
                                    platformType = selectedPlatformType?.displayName ?: PlatformType.其他.displayName,
                                    remark = remark.ifBlank { null },
                                    accountName = account.ifBlank { null },
                                    password = password,
                                    payPassword = payPassword.ifBlank { null },
                                    phone = phone.ifBlank { null },
                                    email = email.ifBlank { null },
                                    idNumber = idNumber.ifBlank { null }
                                )
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            // 保存按钮的文案固定为“保存”，不随模式或加载状态变化
                            "保存"
                        )
                    }
                }
            }
        ) { innerPadding ->
            // 中间输入区域：支持滚动
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 平台名称（必填，单行）
                OutlinedTextField(
                    value = platformName,
                    onValueChange = {
                        platformName = it
                        // 非空时清除错误状态
                        if (platformName.isNotBlank()) platformNameError = false
                    },
                    label = { Text("平台名称 platformName (必填)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    maxLines = 1,
                    // 校验失败时为红色边框
                    isError = platformNameError
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 备注（单行）
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("备注 remark") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 账号（过滤非 ASCII 可见字符）
                OutlinedTextField(
                    value = account,
                    onValueChange = { input ->
                        account = input.filter { ch -> ch.code in 0x21..0x7E }
                    },
                    label = { Text("账号 account") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    singleLine = true,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 密码（必填，过滤非 ASCII）
                OutlinedTextField(
                    value = password,
                    onValueChange = { input ->
                        password = input.filter { ch -> ch.code <= 0x7F }
                        // 非空时清除错误状态
                        if (password.isNotBlank()) passwordError = false
                    },
                    label = { Text("密码 Password (必填)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    // 添加密码可见性切换：可见时不掩码，不可见时使用掩码
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    // 右侧图标用于切换密码的可见性
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (isPasswordVisible) "隐藏密码" else "显示密码"
                            )
                        }
                    },
                    singleLine = true,
                    maxLines = 1,
                    // 校验失败时为红色边框
                    isError = passwordError
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 支付密码（可选，过滤非 ASCII）
                OutlinedTextField(
                    value = payPassword,
                    onValueChange = { input ->
                        payPassword = input.filter { ch -> ch.code <= 0x7F }
                    },
                    label = { Text("支付密码 payPassword") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    // 添加密码可见性切换：可见时不掩码，不可见时使用掩码
                    visualTransformation = if (isPayPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    // 右侧图标用于切换支付密码的可见性
                    trailingIcon = {
                        IconButton(onClick = { isPayPasswordVisible = !isPayPasswordVisible }) {
                            Icon(
                                imageVector = if (isPayPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (isPayPasswordVisible) "隐藏支付密码" else "显示支付密码"
                            )
                        }
                    },
                    singleLine = true,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 手机号（仅数字）
                OutlinedTextField(
                    value = phone,
                    onValueChange = { input ->
                        phone = input.filter { ch -> ch.isDigit() }
                    },
                    label = { Text("手机号 phone") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 邮箱（ASCII 且去除空白）
                OutlinedTextField(
                    value = email,
                    onValueChange = { input ->
                        email = input.filter { ch -> ch.code <= 0x7F && !ch.isWhitespace() }
                    },
                    label = { Text("邮箱 email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 身份证（仅数字与 X，转大写）
                OutlinedTextField(
                    value = idNumber,
                    onValueChange = { input ->
                        idNumber = input.filter { ch -> ch.isDigit() || ch == 'x' || ch == 'X' }.uppercase()
                    },
                    label = { Text("身份证 idNumber") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    singleLine = true,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 提示改为 Toast，移除文本提示区域
            }
        }
    }
}
