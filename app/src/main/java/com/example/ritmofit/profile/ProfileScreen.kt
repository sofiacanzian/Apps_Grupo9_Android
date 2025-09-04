package com.example.ritmofit.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ritmofit.ui.theme.theme.RitmoFitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleEditMode() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Foto de perfil",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (viewModel.isEditing) {
                // Modo de edición
                OutlinedTextField(
                    value = viewModel.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.email,
                    onValueChange = viewModel::onEmailChange,
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false // El email no se edita en este ejemplo
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.birthDate,
                    onValueChange = viewModel::onBirthDateChange,
                    label = { Text("Fecha de Nacimiento (DD/MM/AAAA)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.age,
                    onValueChange = viewModel::onAgeChange,
                    label = { Text("Edad") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.gender,
                    onValueChange = viewModel::onGenderChange,
                    label = { Text("Sexo") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.height,
                    onValueChange = viewModel::onHeightChange,
                    label = { Text("Altura (m)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.weight,
                    onValueChange = viewModel::onWeightChange,
                    label = { Text("Peso (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.partnerNumber,
                    onValueChange = viewModel::onPartnerNumberChange,
                    label = { Text("Número de Socio") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.saveProfile() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Guardar cambios")
                }
            } else {
                // Modo de vista
                Text(
                    text = viewModel.user.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = viewModel.user.email,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Número de Socio: ${viewModel.user.partnerNumber ?: "No especificado"}", style = MaterialTheme.typography.bodyMedium)
                Text("Fecha de Nacimiento: ${viewModel.user.birthDate ?: "No especificado"}", style = MaterialTheme.typography.bodyMedium)
                Text("Edad: ${viewModel.user.age ?: "No especificado"}", style = MaterialTheme.typography.bodyMedium)
                Text("Sexo: ${viewModel.user.gender ?: "No especificado"}", style = MaterialTheme.typography.bodyMedium)
                Text("Altura: ${viewModel.user.height ?: "No especificado"} m", style = MaterialTheme.typography.bodyMedium)
                Text("Peso: ${viewModel.user.weight ?: "No especificado"} kg", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    RitmoFitTheme {
        ProfileScreen(onNavigateBack = {}, onLogout = {})
    }
}