package com.example.taxibookingproject.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taxibookingproject.ui.theme.DeepYellow

data class DriverNotification(
    val id: String,
    val title: String,
    val description: String,
    val time: String,
    val type: NotificationType
)

enum class NotificationType {
    SYSTEM,
    PAYMENT,
    ACCOUNT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverNotificationsScreen(
    onBack: () -> Unit
) {
    val notifications = remember {
        listOf(
            DriverNotification(
                id = "1",
                title = "Chào mừng đối tác mới!",
                description = "Chào mừng bạn đã gia nhập đội ngũ đối tác tài xế của chúng tôi. Hãy bật trạng thái trực tuyến để nhận những chuyến xe đầu tiên ngay nhé!",
                time = "Hôm nay, 08:30",
                type = NotificationType.SYSTEM
            ),
            DriverNotification(
                id = "2",
                title = "Thanh toán thành công",
                description = "Hệ thống đã cập nhật tổng thu nhập cá nhân của bạn sau khi hoàn thành chuyến đi gần nhất. Vui lòng kiểm tra mục 'Thu Nhập Cá Nhân' để xem chi tiết.",
                time = "Hôm qua, 18:45",
                type = NotificationType.PAYMENT
            ),
            DriverNotification(
                id = "3",
                title = "Nguyên tắc cộng đồng và An toàn",
                description = "Đối tác lưu ý luôn tuân thủ luật lệ giao thông, đối xử lịch sự với khách hàng để duy trì điểm đánh giá 5 sao cao nhất.",
                time = "16/06/2026",
                type = NotificationType.SYSTEM
            ),
            DriverNotification(
                id = "4",
                title = "Tài khoản trực tuyến",
                description = "Trạng thái GPS của bạn hoạt động tốt. Bạn sẽ nhận được các thông báo chuyến đi trong bán kính 5km.",
                time = "15/06/2026",
                type = NotificationType.ACCOUNT
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông báo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF7F7F7))
        ) {
            if (notifications.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Không có thông báo nào", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notifications) { notification ->
                        NotificationItemCard(notification = notification)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItemCard(notification: DriverNotification) {
    val (icon, color) = when (notification.type) {
        NotificationType.SYSTEM -> Icons.Default.Campaign to Color(0xFF1976D2)
        NotificationType.PAYMENT -> Icons.Default.Payments to Color(0xFF4CAF50)
        NotificationType.ACCOUNT -> Icons.Default.CheckCircle to DeepYellow
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.description,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notification.time,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
