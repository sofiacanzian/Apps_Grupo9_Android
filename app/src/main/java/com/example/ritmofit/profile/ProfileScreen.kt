//archivo: profilescreen.kt
package com.example.ritmofit.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.ritmofit.R
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val userId = SessionManager.userId ?: return

    LaunchedEffect(key1 = userId) {
        profileViewModel.fetchUserProfile(userId)
    }

    val uiState by profileViewModel.userProfileState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    when (uiState) {
        is ProfileViewModel.ProfileUiState.Loading -> {
            Text("Cargando perfil...")
        }
        is ProfileViewModel.ProfileUiState.Success -> {
            val user = (uiState as ProfileViewModel.ProfileUiState.Success).user
            var name by remember { mutableStateOf(user.name ?: "") }
            var lastName by remember { mutableStateOf(user.lastName ?: "") }
            var memberId by remember { mutableStateOf(user.memberId ?: "") }
            var email by remember { mutableStateOf(user.email) }
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            var birthDate by remember {
                mutableStateOf(user.birthDate?.let { dateFormat.format(it) } ?: "")
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (user.profilePhotoUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(user.profilePhotoUrl),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Silueta de persona",
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
                    onValueChange = { /* El email no se edita */ },
                    label = { Text("Email") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = memberId,
                    onValueChange = { /* El número de socio no se edita */ },
                    label = { Text("Número de Socio") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { /* La fecha de nacimiento no se edita aquí */ },
                    label = { Text("Fecha de Nacimiento") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isEditing) {
                    Button(
                        onClick = {
                            val updatedUser = user.copy(
                                name = name,
                                lastName = lastName
                            )
                            profileViewModel.updateUserProfile(updatedUser.id, updatedUser)
                            isEditing = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar cambios")
                    }
                } else {
                    Button(
                        onClick = { isEditing = true },
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
                    Text("Cerrar Sesión")
                }
            }
        }
        is ProfileViewModel.ProfileUiState.Error -> {
            Text("Error al cargar el perfil: ${(uiState as ProfileViewModel.ProfileUiState.Error).message}")
        }
    }
}