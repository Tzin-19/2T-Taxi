package com.example.taxibookingproject.ui.passenger

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.model.User
import com.example.taxibookingproject.ui.theme.DeepYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerProfileScreen(
    authController: AuthController,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uid = authController.getCurrentUserUid() ?: ""
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploading by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            authController.getUserData(uid, {
                user = it
                isLoading = false
            }, {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                isLoading = false
            })
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            MediaManager.get().upload(it)
                .option("folder", "passenger_profiles")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val imageUrl = resultData?.get("secure_url") as? String ?: ""
                        authController.updateProfileImage(uid, imageUrl, {
                            user = user?.copy(profileImageUrl = imageUrl)
                            isUploading = false
                            Toast.makeText(context, "Cập nhật ảnh thành công!", Toast.LENGTH_SHORT).show()
                        }, {
                            isUploading = false
                            Toast.makeText(context, "Lỗi Firebase: $it", Toast.LENGTH_SHORT).show()
                        })
                    }
                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        isUploading = false
                        Toast.makeText(context, "Lỗi Cloudinary: ${error?.description}", Toast.LENGTH_LONG).show()
                    }
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                }).dispatch()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông tin cá nhân", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepYellow)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DeepYellow)
            }
        } else {
            user?.let { customer ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        if (customer.profileImageUrl.isNullOrEmpty()) {
                            Box(
                                modifier = Modifier.size(130.dp).clip(CircleShape).background(Color(0xFFEEEEEE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(90.dp), tint = Color.Gray)
                            }
                        } else {
                            AsyncImage(
                                model = customer.profileImageUrl,
                                contentDescription = "Profile Image",
                                modifier = Modifier.size(130.dp).clip(CircleShape).border(3.dp, DeepYellow, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        if (isUploading) {
                            Box(
                                modifier = Modifier.size(130.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(36.dp))
                            }
                        }

                        IconButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(DeepYellow).border(2.dp, Color.White, CircleShape)
                        ) {
                            Icon(Icons.Default.CameraAlt, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(customer.fullName, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Khách hàng thân thiết", color = Color.Gray, fontWeight = FontWeight.Medium)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    ProfileInfoRow("Số điện thoại", customer.phone)
                    ProfileInfoRow("Email", customer.email)
                    ProfileInfoRow("Hạng thành viên", "Thành viên Bạc")
                    ProfileInfoRow("Điểm tích lũy", "500 điểm")
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        HorizontalDivider(modifier = Modifier.padding(top = 10.dp), thickness = 0.8.dp, color = Color(0xFFF0F0F0))
    }
}
