package com.example.taxibookingproject.ui.passenger

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taxibookingproject.controller.AuthController
import com.example.taxibookingproject.controller.BookingController
import com.example.taxibookingproject.model.SavedPlace
import com.example.taxibookingproject.model.User
import com.example.taxibookingproject.ui.theme.DeepYellow
import com.example.taxibookingproject.ui.theme.SoftYellow
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritePlacesScreen(
    authController: AuthController,
    bookingController: BookingController,
    onBack: () -> Unit,
    onQuickBook: (String) -> Unit
) {
    val context = LocalContext.current
    val uid = authController.getCurrentUserUid() ?: ""
    var userData by remember { mutableStateOf<User?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var editingPlace by remember { mutableStateOf<SavedPlace?>(null) }

    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            authController.listenToUserData(uid) { data ->
                userData = data
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Địa điểm yêu thích", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        editingPlace = null
                        showDialog = true
                    }) {
                        Icon(Icons.Default.Add, "Thêm địa điểm", tint = Color.Black)
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
            val favorites = userData?.favoritePlaces?.values?.toList() ?: emptyList()

            if (favorites.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Chưa có địa điểm yêu thích nào", color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            editingPlace = null
                            showDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepYellow, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("THÊM NGAY")
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(favorites) { place ->
                        FavoritePlaceItem(
                            place = place,
                            onQuickBook = { onQuickBook(place.id) },
                            onEdit = {
                                editingPlace = place
                                showDialog = true
                            },
                            onDelete = {
                                bookingController.deleteFavorite(uid, place.id)
                                Toast.makeText(context, "Đã xóa địa điểm", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            if (showDialog) {
                AddEditFavoriteDialog(
                    placesClient = remember { Places.createClient(context) },
                    placeToEdit = editingPlace,
                    onDismiss = { showDialog = false },
                    onSave = { name, address, lat, lng ->
                        val id = editingPlace?.id ?: System.currentTimeMillis().toString()
                        val newPlace = SavedPlace(id, name, address, lat, lng)
                        bookingController.saveFavorite(uid, newPlace)
                        showDialog = false
                        Toast.makeText(context, if (editingPlace == null) "Đã thêm địa điểm" else "Đã cập nhật địa điểm", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun FavoritePlaceItem(
    place: SavedPlace,
    onQuickBook: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFFF3E0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Favorite, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = place.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = place.address,
                            fontSize = 13.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Chỉnh sửa", tint = Color.Gray)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Xóa", tint = Color.Red)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onQuickBook,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ĐẶT NHANH", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFavoriteDialog(
    placesClient: PlacesClient,
    placeToEdit: SavedPlace?,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Double) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val sessionToken = remember { AutocompleteSessionToken.newInstance() }

    var name by remember { mutableStateOf(placeToEdit?.name ?: "") }
    var address by remember { mutableStateOf(placeToEdit?.address ?: "") }
    var lat by remember { mutableStateOf(placeToEdit?.lat ?: 0.0) }
    var lng by remember { mutableStateOf(placeToEdit?.lng ?: 0.0) }

    var predictions by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    val searchPlaces = { query: String ->
        if (query.length >= 2) {
            isSearching = true
            val builder = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(sessionToken)
                .setQuery(query)
                .setCountries("VN")
            
            // Bias to Vietnam Center (Da Nang / general)
            val bounds = RectangularBounds.newInstance(
                LatLng(8.0, 102.0),
                LatLng(24.0, 110.0)
            )
            builder.setLocationBias(bounds)

            placesClient.findAutocompletePredictions(builder.build()).addOnSuccessListener { response ->
                predictions = response.autocompletePredictions.map { it.getFullText(null).toString() to it.placeId }
                isSearching = false
            }.addOnFailureListener {
                predictions = emptyList()
                isSearching = false
            }
        } else {
            predictions = emptyList()
            isSearching = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (placeToEdit == null) "Thêm địa điểm yêu thích" else "Sửa địa điểm yêu thích", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên gợi nhớ (ví dụ: Nhà, Công ty)") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepYellow),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = {
                        address = it
                        searchPlaces(it)
                    },
                    label = { Text("Địa chỉ") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepYellow),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isSearching) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = DeepYellow)
                }

                if (predictions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(predictions) { (fullAddress, placeId) ->
                                ListItem(
                                    headlineContent = { Text(fullAddress, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    leadingContent = { Icon(Icons.Default.LocationOn, null, tint = Color.LightGray) },
                                    modifier = Modifier
                                        .clickable {
                                            address = fullAddress
                                            predictions = emptyList()
                                            focusManager.clearFocus()
                                            
                                            // Fetch LatLng
                                            val request = FetchPlaceRequest.builder(placeId, listOf(com.google.android.libraries.places.api.model.Place.Field.LAT_LNG)).build()
                                            placesClient.fetchPlace(request).addOnSuccessListener { response ->
                                                response.place.latLng?.let {
                                                    lat = it.latitude
                                                    lng = it.longitude
                                                }
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isEmpty() || address.trim().isEmpty()) {
                        Toast.makeText(context, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                    } else {
                        onSave(name, address, lat, lng)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepYellow, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("LƯU", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("HỦY", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
    )
}
