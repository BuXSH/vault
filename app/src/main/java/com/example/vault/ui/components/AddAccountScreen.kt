package com.example.vault.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vault.data.entity.Account
import com.example.vault.ui.theme.VaultTheme

/**
 * 添加账号信息的卡片组件
 * 提供弹出式表单界面用于输入新的账号信息
 * 
 * @param onSave 保存账号信息的回调函数
 * @param onCancel 取消操作的回调函数
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountCard(
    modifier: Modifier = Modifier,
    onSave: (Account) -> Unit,
    onCancel: () -> Unit
) {
    // 表单状态管理
    var platformName by remember { mutableStateOf("") }
    var platformType by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    
    // 密码可见性状态
    var passwordVisible by remember { mutableStateOf(false) }
    
    // 表单验证状态
    val isFormValid = platformName.isNotBlank() && password.isNotBlank()
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 600.dp)
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 顶部标题栏（固定）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "添加新账号",
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "关闭"
                    )
                }
            }
            
            // 中间表单内容（可滚动）
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
        
            // 平台名称（必填）
            OutlinedTextField(
            value = platformName,
            onValueChange = { platformName = it },
            label = { Text("平台名称 *") },
            placeholder = { Text("如：微信、支付宝、MySQL服务器") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true
        )
        
            // 平台类型（可选）
            OutlinedTextField(
            value = platformType,
            onValueChange = { platformType = it },
            label = { Text("平台类型") },
            placeholder = { Text("如：社交、支付、数据库") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true
        )
        
            // 备注（可选）
            OutlinedTextField(
            value = remark,
            onValueChange = { remark = it },
            label = { Text("备注") },
            placeholder = { Text("账号相关备注信息") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            maxLines = 3
        )
        
            // 登录账号（可选）
            OutlinedTextField(
            value = account,
            onValueChange = { account = it },
            label = { Text("登录账号") },
            placeholder = { Text("手机号/邮箱/用户名") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true
        )
        
            // 密码（必填）
            OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密码 *") },
            placeholder = { Text("请输入密码") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                    )
                }
            }
        )
        
            // 手机号（可选）
            OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("手机号") },
            placeholder = { Text("请输入手机号") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        
            // 邮箱（可选）
            OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("邮箱") },
            placeholder = { Text("请输入邮箱地址") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        
            // 身份证（可选）
            OutlinedTextField(
                value = idNumber,
                onValueChange = { idNumber = it },
                label = { Text("身份证号") },
                placeholder = { Text("请输入身份证号") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )
            }
            
            // 底部操作按钮（固定）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 取消按钮
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }
                
                // 保存按钮
                Button(
                    onClick = {
                        val newAccount = Account(
                            platformName = platformName,
                            platformType = platformType.takeIf { it.isNotBlank() },
                            remark = remark.takeIf { it.isNotBlank() },
                            account = account.takeIf { it.isNotBlank() },
                            encryptedPassword = password, // 注意：实际应用中需要加密
                            phone = phone.takeIf { it.isNotBlank() },
                            email = email.takeIf { it.isNotBlank() },
                            idNumber = idNumber.takeIf { it.isNotBlank() }
                        )
                        onSave(newAccount)
                    },
                    enabled = isFormValid,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("保存")
                }
            }
        }
    }
}

/**
 * AddAccountCard组件的预览函数
 * 用于在Android Studio设计预览中查看组件外观
 */
@Preview(showBackground = true)
@Composable
fun AddAccountCardPreview() {
    VaultTheme {
        AddAccountCard(
            onSave = { /* 预览模式下的空实现 */ },
            onCancel = { /* 预览模式下的空实现 */ }
        )
    }
}