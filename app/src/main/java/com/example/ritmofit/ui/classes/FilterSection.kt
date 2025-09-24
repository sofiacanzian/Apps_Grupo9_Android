package com.example.ritmofit.ui.classes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ritmofit.model.ClassFilters
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    currentFilters: ClassFilters,
    onApplyFilters: (ClassFilters) -> Unit,
    onDismiss: () -> Unit
) {
    var sede by remember { mutableStateOf(currentFilters.sede ?: "") }
    var discipline by remember { mutableStateOf(currentFilters.discipline ?: "") }
    var date by remember { mutableStateOf(currentFilters.date ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrar Clases") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = sede,
                    onValueChange = { sede = it },
                    label = { Text("Sede") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = discipline,
                    onValueChange = { discipline = it },
                    label = { Text("Disciplina") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Fecha (DD/MM/YYYY)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApplyFilters(
                        ClassFilters(
                            sede = sede.takeIf { it.isNotBlank() },
                            discipline = discipline.takeIf { it.isNotBlank() },
                            date = date.takeIf { it.isNotBlank() }
                        )
                    )
                }
            ) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
