package com.example.taxibookingproject.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.ui.components.ErrorText
import com.example.taxibookingproject.ui.components.TaxiButton
import com.example.taxibookingproject.ui.components.TaxiTextField
import com.example.taxibookingproject.ui.theme.DeepYellow
import com.example.taxibookingproject.ui.theme.MilkyYellow
import com.example.taxibookingproject.ui.theme.SoftYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authController: AuthController,
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableIntStateOf(3) } // 2: Tài xế, 3: Khách
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var agreeTerms by remember { mutableStateOf(false) }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(SoftYellow, Color.White, Color.White)
    )

    Box(modifier = Modifier.fillMaxSize().background(gradientBrush)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            IconButton(
                onClick = onBackToLogin,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.Black)
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Tạo tài khoản ✨",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )
            Text(
                text = "Tham gia cùng đội ngũ Taxi chuyên nghiệp ngay",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            TaxiTextField(
                value = fullName,
                onValueChange = { fullName = it; errorMessage = "" },
                label = "Họ và tên",
                leadingIcon = Icons.Default.Person
            )

            TaxiTextField(
                value = phone,
                onValueChange = { phone = it; errorMessage = "" },
                label = "Số điện thoại",
                leadingIcon = Icons.Default.Phone,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
            )

            TaxiTextField(
                value = email,
                onValueChange = { email = it; errorMessage = "" },
                label = "Email đăng ký",
                leadingIcon = Icons.Default.Email,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
            )

            TaxiTextField(
                value = password,
                onValueChange = { password = it; errorMessage = "" },
                label = "Mật khẩu (6+ ký tự)",
                leadingIcon = Icons.Default.Lock,
                isPassword = true
            )

            TaxiTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = "" },
                label = "Xác nhận mật khẩu",
                leadingIcon = Icons.Default.LockReset,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("Bạn muốn đăng ký là:", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            Row(modifier = Modifier.padding(vertical = 12.dp)) {
                FilterChip(
                    selected = selectedRole == 3,
                    onClick = { selectedRole = 3 },
                    label = { Text("Khách hàng", fontWeight = FontWeight.Bold) },
                    leadingIcon = if (selectedRole == 3) { { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) } } else null,
                    modifier = Modifier.padding(end = 12.dp).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DeepYellow,
                        selectedLabelColor = Color.Black,
                        containerColor = Color.White,
                        labelColor = Color.Gray
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (selectedRole == 3) DeepYellow else Color(0xFFEEEEEE),
                        borderWidth = 1.dp,
                        enabled = true,
                        selected = selectedRole == 3
                    )
                )
                FilterChip(
                    selected = selectedRole == 2,
                    onClick = { selectedRole = 2 },
                    label = { Text("Tài xế", fontWeight = FontWeight.Bold) },
                    leadingIcon = if (selectedRole == 2) { { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) } } else null,
                    modifier = Modifier.height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DeepYellow,
                        selectedLabelColor = Color.Black,
                        containerColor = Color.White,
                        labelColor = Color.Gray
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (selectedRole == 2) DeepYellow else Color(0xFFEEEEEE),
                        borderWidth = 1.dp,
                        enabled = true,
                        selected = selectedRole == 2
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = agreeTerms, 
                    onCheckedChange = { agreeTerms = it },
                    colors = CheckboxDefaults.colors(checkedColor = DeepYellow)
                )
                Text(
                    text = "Tôi đồng ý với Điều khoản & Chính sách",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium
                )
            }

            ErrorText(errorMessage)

            Spacer(modifier = Modifier.height(24.dp))

            TaxiButton(
                text = "ĐĂNG KÝ TÀI KHOẢN",
                isLoading = isLoading,
                containerColor = DeepYellow
            ) {
                val cleanEmail = email.trim()
                val cleanPhone = phone.trim()
                
                when {
                    fullName.trim().isEmpty() || cleanPhone.isEmpty() || cleanEmail.isEmpty() || password.isEmpty() -> {
                        errorMessage = "📍 Bạn điền thiếu thông tin mất rồi!"
                    }
                    !cleanEmail.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()) -> {
                        errorMessage = "📍 Định dạng email đăng ký chưa chính xác."
                    }
                    !cleanPhone.matches("0[35789]\\d{8}".toRegex()) -> {
                        errorMessage = "📍 Số điện thoại phải gồm 10 chữ số (bắt đầu bằng 03, 05, 07, 08, 09)."
                    }
                    password.length < 6 -> {
                        errorMessage = "📍 Mật khẩu cần có ít nhất 6 ký tự nè."
                    }
                    password != confirmPassword -> {
                        errorMessage = "📍 Mật khẩu không khớp, kiểm tra lại nhé."
                    }
                    !agreeTerms -> {
                        errorMessage = "📍 Bạn hãy đồng ý với điều khoản để tiếp tục nha."
                    }
                    else -> {
                        isLoading = true
                        authController.registerUser(cleanEmail, password, fullName.trim(), cleanPhone, selectedRole,
                            onSuccess = { 
                                isLoading = false
                                Toast.makeText(context, "Đăng ký thành công! Đang quay lại đăng nhập...", Toast.LENGTH_LONG).show()
                                onRegisterSuccess() 
                            },
                            onFailure = { 
                                isLoading = false
                                errorMessage = "📍 Lỗi: $it"
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 48.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Đã có tài khoản?", color = Color.Gray)
                TextButton(onClick = onBackToLogin) {
                    Text("Đăng nhập ngay", fontWeight = FontWeight.Black, color = Color.Black)
                }
            }
        }
    }
}