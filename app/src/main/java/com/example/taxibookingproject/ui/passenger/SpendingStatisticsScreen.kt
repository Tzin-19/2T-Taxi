package com.example.taxibookingproject.ui.passenger

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.controller.BookingController
import com.example.taxibookingproject.model.Trip
import com.example.taxibookingproject.ui.theme.DeepYellow
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendingStatisticsScreen(
    authController: AuthController,
    bookingController: BookingController,
    onBack: () -> Unit
) {
    val uid = authController.getCurrentUserUid() ?: ""
    var history by remember { mutableStateOf<List<Trip>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Hàng ngày (Last 7 Days), 1: Hàng tháng

    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            bookingController.listenForPassengerHistory(uid) { trips ->
                history = trips
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thống kê chi tiêu", fontWeight = FontWeight.Bold) },
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
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = DeepYellow)
            } else {
                val totalSpending = history.sumOf { it.price }
                val totalTrips = history.size
                val avgSpending = if (totalTrips > 0) totalSpending / totalTrips else 0.0

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Summary Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Tổng chi tiêu", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${String.format("%,.0f", totalSpending)}đ",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Trung bình/chuyến", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${String.format("%,.0f", avgSpending)}đ",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DeepYellow
                                )
                            }
                        }
                    }

                    // Total Trips Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Tổng số chuyến đi", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$totalTrips chuyến", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(DeepYellow.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TrendingUp, null, tint = DeepYellow)
                            }
                        }
                    }

                    // Chart Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            // Tabs switcher
                            TabRow(
                                selectedTabIndex = selectedTab,
                                indicator = { tabPositions ->
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                        color = DeepYellow
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Tab(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    text = { Text("Hàng ngày", fontWeight = FontWeight.Bold, color = if (selectedTab == 0) Color.Black else Color.Gray) }
                                )
                                Tab(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    text = { Text("Hàng tháng", fontWeight = FontWeight.Bold, color = if (selectedTab == 1) Color.Black else Color.Gray) }
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Draw Canvas Chart
                            if (history.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.BarChart, null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Chưa có dữ liệu để lập biểu đồ", color = Color.Gray, fontSize = 14.sp)
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    if (selectedTab == 0) {
                                        DailySpendingChart(history = history)
                                    } else {
                                        MonthlySpendingChart(history = history)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailySpendingChart(history: List<Trip>) {
    val dailyData = remember(history) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val data = mutableListOf<Pair<String, Double>>()

        // Generate last 7 days
        for (i in 6 downTo 0) {
            val dateCalendar = Calendar.getInstance()
            dateCalendar.add(Calendar.DAY_OF_YEAR, -i)
            val dayStr = dateFormat.format(dateCalendar.time)
            
            // Sum spending for this day
            val startOfDay = dateCalendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = dateCalendar.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val spending = history.filter {
                val time = it.completedAt ?: it.timestamp
                time in startOfDay..endOfDay
            }.sumOf { it.price }

            data.add(dayStr to spending)
        }
        data
    }

    SpendingBarChart(chartData = dailyData)
}

@Composable
fun MonthlySpendingChart(history: List<Trip>) {
    val monthlyData = remember(history) {
        val data = mutableListOf<Pair<String, Double>>()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        // Generate 12 months
        for (month in Calendar.JANUARY..Calendar.DECEMBER) {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, currentYear)
            calendar.set(Calendar.MONTH, month)
            val monthLabel = "Th ${month + 1}"

            val startOfMonth = calendar.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfMonth = calendar.apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val spending = history.filter {
                val time = it.completedAt ?: it.timestamp
                time in startOfMonth..endOfMonth
            }.sumOf { it.price }

            data.add(monthLabel to spending)
        }
        data
    }

    SpendingBarChart(chartData = monthlyData)
}

@Composable
fun SpendingBarChart(chartData: List<Pair<String, Double>>) {
    val maxVal = remember(chartData) {
        val max = chartData.maxOfOrNull { it.second } ?: 0.0
        if (max == 0.0) 10000.0 else max * 1.15 // pad 15% space at the top
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val bottomPadding = 30.dp.toPx()
        val leftPadding = 50.dp.toPx()
        val rightPadding = 10.dp.toPx()
        val topPadding = 20.dp.toPx()

        val chartWidth = canvasWidth - leftPadding - rightPadding
        val chartHeight = canvasHeight - bottomPadding - topPadding

        // 1. Draw horizontal lines and labels (Y Axis)
        val paint = android.graphics.Paint().apply {
            textSize = 10.sp.toPx()
            color = android.graphics.Color.GRAY
            isAntiAlias = true
        }

        val stepCount = 4
        for (i in 0..stepCount) {
            val yVal = maxVal * i / stepCount
            val yPos = topPadding + chartHeight * (1f - i.toFloat() / stepCount)
            
            // Grid line
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(leftPadding, yPos),
                end = Offset(canvasWidth - rightPadding, yPos),
                strokeWidth = 1.dp.toPx()
            )

            // Label text
            val label = if (yVal >= 1000000) {
                "${String.format(Locale.getDefault(), "%.1f", yVal / 1000000.0)}M"
            } else if (yVal >= 1000) {
                "${String.format(Locale.getDefault(), "%.0f", yVal / 1000.0)}k"
            } else {
                yVal.toInt().toString()
            }
            
            drawContext.canvas.nativeCanvas.drawText(
                label,
                10.dp.toPx(),
                yPos + 4.dp.toPx(),
                paint
            )
        }

        // 2. Draw bars and X Axis labels
        val barCount = chartData.size
        val gapRatio = 0.35f
        val totalGapsWidth = chartWidth * gapRatio
        val totalBarsWidth = chartWidth * (1f - gapRatio)
        val barWidth = totalBarsWidth / barCount
        val gapWidth = totalGapsWidth / (barCount + 1)

        chartData.forEachIndexed { index, (label, value) ->
            val xPos = leftPadding + gapWidth + index * (barWidth + gapWidth)
            val barHeight = chartHeight * (value / maxVal).toFloat()
            val yPos = topPadding + chartHeight - barHeight

            // Draw Bar with rounded corners
            if (barHeight > 0) {
                drawRoundRect(
                    color = DeepYellow,
                    topLeft = Offset(xPos, yPos),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            } else {
                // Draw zero baseline dot
                drawCircle(
                    color = Color.LightGray,
                    center = Offset(xPos + barWidth / 2f, topPadding + chartHeight),
                    radius = 2.dp.toPx()
                )
            }

            // Draw label under the bar
            val textWidth = paint.measureText(label)
            val textX = xPos + (barWidth - textWidth) / 2f
            drawContext.canvas.nativeCanvas.drawText(
                label,
                textX,
                canvasHeight - 6.dp.toPx(),
                paint
            )
        }
    }
}
