package com.example.taxibookingproject.ui.passenger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taxibookingproject.ui.components.TaxiButton
import com.example.taxibookingproject.ui.theme.DeepYellow
import com.example.taxibookingproject.ui.theme.MilkyYellow
import com.example.taxibookingproject.ui.theme.SoftYellow

@Composable
fun RatingScreen(onFinish: () -> Unit) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Chuyến đi kết thúc! ✨", fontSize = 26.sp, fontWeight = FontWeight.Black)
        Text(
            text = "Hy vọng bạn có một trải nghiệm tuyệt vời", 
            color = Color.Gray, 
            modifier = Modifier.padding(top = 8.dp),
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Ảnh tài xế
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(MilkyYellow), 
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, modifier = Modifier.size(64.dp), tint = Color.Black)
        }
        Text(
            text = "Tài xế Trần Văn A", 
            fontWeight = FontWeight.ExtraBold, 
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Chọn sao
        Row {
            repeat(5) { index ->
                val isSelected = index < rating
                IconButton(
                    onClick = { rating = index + 1 },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        tint = if (isSelected) DeepYellow else Color.LightGray,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Ô nhập nhận xét
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            placeholder = { Text("Bạn thấy tài xế thế nào? (Không bắt buộc)", fontSize = 14.sp) },
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DeepYellow,
                unfocusedBorderColor = Color(0xFFEEEEEE),
                focusedContainerColor = SoftYellow.copy(alpha = 0.3f),
                unfocusedContainerColor = Color(0xFFF9F9F9)
            )
        )

        Spacer(modifier = Modifier.height(48.dp))

        TaxiButton(
            text = "GỬI ĐÁNH GIÁ",
            containerColor = DeepYellow
        ) {
            onFinish()
        }
    }
}