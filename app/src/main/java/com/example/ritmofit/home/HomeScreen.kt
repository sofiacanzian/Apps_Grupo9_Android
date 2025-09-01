package com.example.ritmofit.ui.theme.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Esta es la importación que soluciona el problema
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ritmofit.data.models.*
import com.example.ritmofit.ui.theme.theme.RitmoFitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToReservations: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToQrScanner: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onClassClick: (String) -> Unit = {}
) {
    val mockClasses = remember { getMockClasses() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "RitmoFit",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tu gimnasio, tu ritmo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToQrScanner,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.QrCode, contentDescription = "Escanear QR")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                WelcomeSection(
                    onNavigateToReservations = onNavigateToReservations,
                    onNavigateToHistory = onNavigateToHistory
                )
            }
            item {
                Padding16 {
                    Text(
                        text = "Clases de hoy",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            items(mockClasses) { gymClass ->
                Padding16 {
                    ClassCard(
                        gymClass = gymClass,
                        onClick = { onClassClick(gymClass.id) }
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun WelcomeSection(
    onNavigateToReservations: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "¡Hola! 👋",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "¿Listo para entrenar hoy?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.EventNote,
                    text = "Mis Reservas",
                    onClick = onNavigateToReservations,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.History,
                    text = "Historial",
                    onClick = onNavigateToHistory,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ClassCard(
    gymClass: GymClass,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = getClassIcon(gymClass.name),
                        contentDescription = gymClass.name,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = gymClass.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Horario",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${gymClass.schedule.startTime} - ${gymClass.schedule.endTime}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${gymClass.duration} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Instructor",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = gymClass.instructor.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Ubicación",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = gymClass.location.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cupos: ${gymClass.availableSpots}/${gymClass.capacity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (gymClass.availableSpots > 0)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = gymClass.difficulty.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

fun getClassIcon(className: String): ImageVector {
    return when (className.lowercase()) {
        "funcional" -> Icons.Default.Star
        "yoga" -> Icons.Default.Favorite
        "spinning" -> Icons.Default.Star
        "crossfit" -> Icons.Default.Star
        "pilates" -> Icons.Default.Favorite
        else -> Icons.Default.Star
    }
}

@Composable
fun Padding16(content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        content()
    }
}

fun getMockClasses(): List<GymClass> {
    val locations = getMockLocations()
    val instructors = getMockInstructors()

    return listOf(
        GymClass(
            id = "1",
            name = "Funcional",
            description = "Entrenamiento funcional completo",
            instructor = instructors[0],
            location = locations[0],
            schedule = ClassSchedule(
                startTime = "18:00",
                endTime = "19:00",
                date = "2025-08-30"
            ),
            capacity = 20,
            availableSpots = 5,
            duration = 60,
            difficulty = DifficultyLevel.INTERMEDIATE
        ),
        GymClass(
            id = "2",
            name = "Yoga",
            description = "Yoga relajante para todos los niveles",
            instructor = instructors[1],
            location = locations[1],
            schedule = ClassSchedule(
                startTime = "19:30",
                endTime = "20:30",
                date = "2025-08-30"
            ),
            capacity = 15,
            availableSpots = 8,
            duration = 60,
            difficulty = DifficultyLevel.BEGINNER
        ),
        GymClass(
            id = "3",
            name = "Spinning",
            description = "Cardio intenso con música",
            instructor = instructors[2],
            location = locations[0],
            schedule = ClassSchedule(
                startTime = "07:00",
                endTime = "07:45",
                date = "2025-08-30"
            ),
            capacity = 25,
            availableSpots = 0,
            duration = 45,
            difficulty = DifficultyLevel.ADVANCED
        ),
        GymClass(
            id = "4",
            name = "CrossFit",
            description = "Entrenamiento de alta intensidad",
            instructor = instructors[0],
            location = locations[2],
            schedule = ClassSchedule(
                startTime = "20:00",
                endTime = "21:00",
                date = "2025-08-30"
            ),
            capacity = 12,
            availableSpots = 3,
            duration = 60,
            difficulty = DifficultyLevel.ADVANCED
        )
    )
}

fun getMockLocations(): List<GymLocation> {
    return listOf(
        GymLocation(
            id = "1",
            name = "Palermo",
            address = "Av. Santa Fe 3000, Palermo"
        ),
        GymLocation(
            id = "2",
            name = "Villa Crespo",
            address = "Av. Corrientes 4500, Villa Crespo"
        ),
        GymLocation(
            id = "3",
            name = "Belgrano",
            address = "Av. Cabildo 2000, Belgrano"
        )
    )
}

fun getMockInstructors(): List<Instructor> {
    return listOf(
        Instructor(
            id = "1",
            name = "Carlos Mendez",
            specialties = listOf("Funcional", "CrossFit"),
            rating = 4.8f
        ),
        Instructor(
            id = "2",
            name = "Ana Rodriguez",
            specialties = listOf("Yoga", "Pilates"),
            rating = 4.9f
        ),
        Instructor(
            id = "3",
            name = "Miguel Torres",
            specialties = listOf("Spinning", "Cardio"),
            rating = 4.7f
        )
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    RitmoFitTheme {
        HomeScreen()
    }
}