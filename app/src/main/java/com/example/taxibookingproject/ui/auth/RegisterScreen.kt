package com.example.taxibookingproject.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.ui.components.TaxiButton
import com.example.taxibookingproject.ui.components.TaxiTextField

@Composable
fun RegisterScreen(
    authController: AuthController,
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableIntStateOf(3) } // 2: Tài xế, 3: Khách
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(text = "Đăng ký", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(text = "Tham gia cùng cộng đồng Taxi của chúng tôi", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        TaxiTextField(value = fullName, onValueChange = { fullName = it }, label = "Họ và tên")
        TaxiTextField(value = phone, onValueChange = { phone = it }, label = "Số điện thoại")
        TaxiTextField(value = email, onValueChange = { email = it }, label = "Email")
        TaxiTextField(value = password, onValueChange = { password = it }, label = "Mật khẩu (ít nhất 6 ký tự)")

        Spacer(modifier = Modifier.height(16.dp))

        Text("Bạn muốn đăng ký với vai trò:", fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = (selectedRole == 3), onClick = { selectedRole = 3 })
            Text("Khách hàng")
            Spacer(modifier = Modifier.width(20.dp))
            RadioButton(selected = (selectedRole == 2), onClick = { selectedRole = 2 })
            Text("Tài xế")
        }

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red, modifier = Modifier.padding(vertical = 8.dp))
        }

        TaxiButton(text = "ĐĂNG KÝ") {
            authController.registerUser(email, password, fullName, phone, selectedRole,
                onSuccess = { onRegisterSuccess() },
                onFailure = { errorMessage = it }
            )
        }

        TextButton(onClick = onBackToLogin) {
            Text("Đã có tài khoản? Đăng nhập tại đây")
        }
    }
}