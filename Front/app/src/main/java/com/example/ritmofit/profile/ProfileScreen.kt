package com.example.ritmofit.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size // <- AÑADIDA
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.ritmofit.data.models.SessionManager
import com.example.ritmofit.data.models.User
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    var currentUserId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        currentUserId = SessionManager.getUserId()
    }

    LaunchedEffect(currentUserId) {
        currentUserId?.let { profileViewModel.fetchUserProfile(it) }
    }

    val uiState by profileViewModel.userProfileState.collectAsState()
    val scrollState = rememberScrollState()
    var isEditing by remember { mutableStateOf(false) }

    if (currentUserId == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        ) {
            Text("Acceso denegado. Usuario no autenticado.")
        }
        return
    }

    when (uiState) {
        is ProfileViewModel.ProfileUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                CircularProgressIndicator()
            }
        }

        is ProfileViewModel.ProfileUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                Text((uiState as ProfileViewModel.ProfileUiState.Error).message)
            }
        }

        is ProfileViewModel.ProfileUiState.Success -> {
            val user = (uiState as ProfileViewModel.ProfileUiState.Success).user
            ProfileContent(
                user = user,
                isEditing = isEditing,
                onValueChange = { updated -> profileViewModel.updateUserProfile(currentUserId!!, updated) },
                onToggleEditing = { isEditing = !isEditing },
                onLogout = onLogout,
                scrollState = scrollState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    user: User,
    isEditing: Boolean,
    onValueChange: (User) -> Unit,
    onToggleEditing: () -> Unit,
    onLogout: () -> Unit,
    scrollState: androidx.compose.foundation.ScrollState
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var name by remember(user.name) { mutableStateOf(user.name.orEmpty()) }
    var lastName by remember(user.lastName) { mutableStateOf(user.lastName.orEmpty()) }
    val email = remember(user.email) { user.email.orEmpty() }
    val memberId = remember(user.memberId) { user.memberId.orEmpty() }
    val birthDate = remember(user.birthDate) { user.birthDate?.let(dateFormat::format).orEmpty() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (user.profilePhotoUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(user.profilePhotoUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            readOnly = !isEditing,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Apellido") },
            readOnly = !isEditing,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {},
            label = { Text("Email") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = memberId,
            onValueChange = {},
            label = { Text("Número de Socio") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = birthDate,
            onValueChange = {},
            label = { Text("Fecha de Nacimiento") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isEditing) {
            Button(
                onClick = {
                    onValueChange(
                        user.copy(
                            name = name,
                            lastName = lastName
                        )
                    )
                    onToggleEditing()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")
            }
        } else {
            Button(
                onClick = onToggleEditing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Editar perfil")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}
