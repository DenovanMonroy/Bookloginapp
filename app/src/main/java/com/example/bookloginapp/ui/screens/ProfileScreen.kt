package com.example.bookloginapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.bookloginapp.model.UserProfile
import com.example.bookloginapp.util.PermissionsManager
import com.example.bookloginapp.util.RequestStoragePermission
import com.example.bookloginapp.viewmodel.UpdateProfileState
import com.example.bookloginapp.viewmodel.UserProfileState
import com.example.bookloginapp.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userViewModel: UserViewModel = viewModel()
) {
    val userProfileState by userViewModel.userProfileState.collectAsState()
    val updateProfileState by userViewModel.updateProfileState.collectAsState()

    val context = LocalContext.current
    val permissionsManager = remember { PermissionsManager(context) }
    var showPermissionRequest by remember { mutableStateOf(false) }

    var isEditing by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var secondLastName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf<Date?>(null) }
    var profileImageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // DatePicker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = birthDate?.time
    )

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            userViewModel.setSelectedImage(it)
        }
    }

    // Update local state when profile is loaded
    LaunchedEffect(userProfileState) {
        if (userProfileState is UserProfileState.Success) {
            val profile = (userProfileState as UserProfileState.Success).profile
            firstName = profile.firstName
            lastName = profile.lastName
            secondLastName = profile.secondLastName
            birthDate = profile.birthDate
            profileImageUrl = profile.profilePictureUrl
        }
    }

    // Show success or error messages
    LaunchedEffect(updateProfileState) {
        when (updateProfileState) {
            is UpdateProfileState.Success -> {
                isEditing = false
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (userProfileState) {
            is UserProfileState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            }
            is UserProfileState.Error -> {
                Text(
                    text = (userProfileState as UserProfileState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                // Profile picture
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable(enabled = isEditing) {
                            if (isEditing) {
                                if (permissionsManager.hasReadExternalStoragePermission()) {
                                    imagePickerLauncher.launch("image/*")
                                } else {
                                    showPermissionRequest = true
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    if (isEditing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Cambiar foto",
                                tint = Color.White
                            )
                        }
                    }
                }

                if (showPermissionRequest) {
                    RequestStoragePermission(
                        permissionsManager = permissionsManager,
                        onPermissionGranted = {
                            showPermissionRequest = false
                            imagePickerLauncher.launch("image/*")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Profile info fields
                if (isEditing) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("Nombre") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Apellido paterno") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = secondLastName,
                        onValueChange = { secondLastName = it },
                        label = { Text("Apellido materno") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    // Birth date field with date picker
                    OutlinedTextField(
                        value = birthDate?.let { dateFormat.format(it) } ?: "",
                        onValueChange = { },
                        label = { Text("Fecha de nacimiento") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { showDatePicker = true },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Seleccionar fecha"
                                )
                            }
                        }
                    )

                    // Date picker dialog
                    if (showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    // Actualizar la fecha si se seleccionó una válida
                                    datePickerState.selectedDateMillis?.let {
                                        val calendar = Calendar.getInstance()
                                        calendar.timeInMillis = it
                                        birthDate = calendar.time
                                    }
                                    showDatePicker = false
                                }) {
                                    Text("OK")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                    Text("Cancelar")
                                }
                            }
                        ) {
                            DatePicker(
                                state = datePickerState
                            )
                        }
                    }

                    if (updateProfileState is UpdateProfileState.Error) {
                        Text(
                            text = (updateProfileState as UpdateProfileState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = { isEditing = false }
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = {
                                userViewModel.updateProfile(
                                    firstName = firstName,
                                    lastName = lastName,
                                    secondLastName = secondLastName,
                                    birthDate = birthDate
                                )
                            },
                            enabled = updateProfileState !is UpdateProfileState.Loading
                        ) {
                            if (updateProfileState is UpdateProfileState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Guardar")
                            }
                        }
                    }
                } else {
                    // Display mode
                    ProfileInfoItem(label = "Nombre", value = firstName)
                    ProfileInfoItem(label = "Apellido paterno", value = lastName)
                    ProfileInfoItem(label = "Apellido materno", value = secondLastName)
                    ProfileInfoItem(
                        label = "Fecha de nacimiento",
                        value = birthDate?.let { dateFormat.format(it) } ?: "No especificado"
                    )

                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Text("Editar perfil")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.ifEmpty { "No especificado" },
            style = MaterialTheme.typography.bodyLarge
        )
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}