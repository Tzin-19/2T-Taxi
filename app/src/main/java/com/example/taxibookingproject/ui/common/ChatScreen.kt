package com.example.taxibookingproject.ui.common

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Mood
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.controller.BookingController
import com.example.taxibookingproject.model.ChatMessage
import com.example.taxibookingproject.model.Trip
import com.example.taxibookingproject.model.User
import com.example.taxibookingproject.ui.theme.DeepYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    tripId: String,
    bookingController: BookingController,
    authController: AuthController,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUid = authController.getCurrentUserUid() ?: ""
    
    var tripData by remember { mutableStateOf<Trip?>(null) }
    var currentUserData by remember { mutableStateOf<User?>(null) }
    var otherUserData by remember { mutableStateOf<User?>(null) }
    var messageText by remember { mutableStateOf("") }
    var messagesList by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()

    val commonEmojis = remember {
        listOf(
            "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇", "🙂", 
            "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚", "😋", 
            "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩", "🥳", 
            "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣", "😖", 
            "😫", "😩", "🥺", "😢", "😭", "😤", "😠", "😡", "🤬", "🤯",
            "👋", "👌", "👍", "👎", "✊", "👊", "🤛", "🤜", "🤝", "🙏",
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
            "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑", "🚒", "🚐"
        )
    }

    // 1. Image selection launcher
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            MediaManager.get().upload(it)
                .option("folder", "chat_attachments")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val url = resultData?.get("secure_url") as? String ?: ""
                        isUploading = false
                        val receiverId = if (currentUserData?.role == 2) tripData?.passengerId ?: "" else (tripData?.driverId ?: "")
                        val newMsg = ChatMessage(
                            senderId = currentUid,
                            receiverId = receiverId,
                            text = "[Hình ảnh]",
                            timestamp = System.currentTimeMillis(),
                            imageUrl = url,
                            messageType = "image"
                        )
                        bookingController.sendMessage(tripId, newMsg)
                    }
                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        isUploading = false
                        Toast.makeText(context, "Lỗi tải ảnh lên: ${error?.description}", Toast.LENGTH_SHORT).show()
                    }
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                }).dispatch()
        }
    }

    // 2. File selection launcher
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            // Get original filename
            val cursor = context.contentResolver.query(it, null, null, null, null)
            val nameIndex = cursor?.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor?.moveToFirst()
            val filename = nameIndex?.let { idx -> cursor.getString(idx) } ?: "file"
            cursor?.close()

            MediaManager.get().upload(it)
                .option("folder", "chat_attachments")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val url = resultData?.get("secure_url") as? String ?: ""
                        isUploading = false
                        val receiverId = if (currentUserData?.role == 2) tripData?.passengerId ?: "" else (tripData?.driverId ?: "")
                        val newMsg = ChatMessage(
                            senderId = currentUid,
                            receiverId = receiverId,
                            text = "[Tệp tin]: $filename",
                            timestamp = System.currentTimeMillis(),
                            fileUrl = url,
                            fileName = filename,
                            messageType = "file"
                        )
                        bookingController.sendMessage(tripId, newMsg)
                    }
                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        isUploading = false
                        Toast.makeText(context, "Lỗi tải tệp tin lên: ${error?.description}", Toast.LENGTH_SHORT).show()
                    }
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                }).dispatch()
        }
    }

    // Fetch current user data
    LaunchedEffect(currentUid) {
        if (currentUid.isNotEmpty()) {
            authController.getUserData(currentUid, { currentUserData = it }, {})
        }
    }

    // Listen to trip status and identify the other participant
    LaunchedEffect(tripId) {
        bookingController.listenToTripStatus(tripId) { trip ->
            tripData = trip
        }
    }

    // Fetch other user data once trip and current user role are known
    LaunchedEffect(tripData, currentUserData) {
        val trip = tripData
        val user = currentUserData
        if (trip != null && user != null) {
            val otherUid = if (user.role == 2) trip.passengerId else (trip.driverId ?: "")
            if (otherUid.isNotEmpty()) {
                authController.getUserData(otherUid, { otherUserData = it }, {})
            }
        }
    }

    // Listen to chat messages in real time
    LaunchedEffect(tripId) {
        bookingController.listenForMessages(tripId) { list ->
            messagesList = list.sortedBy { it.timestamp }
        }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messagesList.size) {
        if (messagesList.isNotEmpty()) {
            listState.animateScrollToItem(messagesList.size - 1)
        }
    }

    val sendMessage = {
        if (messageText.trim().isNotEmpty() && tripData != null && currentUserData != null) {
            val trip = tripData!!
            val user = currentUserData!!
            val receiverId = if (user.role == 2) trip.passengerId else (trip.driverId ?: "")
            
            val newMsg = ChatMessage(
                senderId = currentUid,
                receiverId = receiverId,
                text = messageText.trim(),
                timestamp = System.currentTimeMillis()
            )
            bookingController.sendMessage(tripId, newMsg) {
                messageText = ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        ) {
                            if (otherUserData?.profileImageUrl.isNullOrEmpty()) {
                                Icon(
                                    Icons.Default.Person,
                                    null,
                                    modifier = Modifier.align(Alignment.Center).size(24.dp),
                                    tint = Color.DarkGray
                                )
                            } else {
                                AsyncImage(
                                    model = otherUserData?.profileImageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = otherUserData?.fullName ?: "Đang tải...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = if (currentUserData?.role == 2) "Khách hàng" else "Tài xế",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF7F7F7))
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messagesList) { message ->
                    val isMine = message.senderId == currentUid
                    ChatBubble(message = message, isMine = isMine)
                }
            }

            if (isUploading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = DeepYellow, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Đang tải tệp lên...", fontSize = 13.sp, color = Color.Gray)
                }
            }

            if (showEmojiPicker) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 45.dp),
                        modifier = Modifier.padding(8.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(commonEmojis) { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable {
                                        messageText += emoji
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 24.sp)
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showEmojiPicker = !showEmojiPicker }) {
                        Icon(Icons.Default.Mood, null, tint = if (showEmojiPicker) DeepYellow else Color.Gray)
                    }
                    IconButton(onClick = { imageLauncher.launch("image/*") }) {
                        Icon(Icons.Default.Image, null, tint = Color.Gray)
                    }
                    IconButton(onClick = { fileLauncher.launch("*/*") }) {
                        Icon(Icons.Default.AttachFile, null, tint = Color.Gray)
                    }
                    
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Nhập tin nhắn...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepYellow,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { sendMessage() },
                        enabled = messageText.trim().isNotEmpty(),
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (messageText.trim().isNotEmpty()) DeepYellow else Color.LightGray,
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            null,
                            tint = if (messageText.trim().isNotEmpty()) Color.Black else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isMine: Boolean) {
    val bubbleColor = if (isMine) DeepYellow else Color.White
    val textColor = Color.Black
    val alignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isMine) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }
    
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = timeFormat.format(Date(message.timestamp))

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
        ) {
            Surface(
                color = bubbleColor,
                shape = shape,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    when (message.messageType) {
                        "image" -> {
                            AsyncImage(
                                model = message.imageUrl,
                                contentDescription = "Attached Image",
                                modifier = Modifier
                                    .sizeIn(maxWidth = 200.dp, maxHeight = 200.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                        "file" -> {
                            val openContext = LocalContext.current
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    message.fileUrl?.let { url ->
                                        val browserIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                        openContext.startActivity(browserIntent)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.InsertDriveFile, null, tint = if (isMine) Color.Black else Color.Gray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = message.fileName ?: "Tệp đính kèm",
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    fontSize = 14.sp,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 150.dp)
                                )
                            }
                        }
                        else -> {
                            Text(text = message.text, color = textColor, fontSize = 15.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}
