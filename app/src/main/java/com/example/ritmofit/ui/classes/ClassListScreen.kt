package com.example.ritmofit.ui.classes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassListScreen(
    viewModel: ClassViewModel,
    onClassClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilters by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Detectar cuando llega al final de la lista para cargar más
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        }
            .distinctUntilChanged()
            .collect { lastIndex ->
                if (lastIndex >= (listState.layoutInfo.totalItemsCount - 2)) {
                    viewModel.loadNextPage()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clases Disponibles") },
                actions = {
                    IconButton(onClick = { showFilters = true }) {
                        // Aquí iría el ícono de filtro
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (uiState) {
                is ClassesUiState.Loading -> {
                    if (viewModel.currentPage == 1) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is ClassesUiState.Success -> {
                    val classes = (uiState as ClassesUiState.Success).classes
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = classes,
                            key = { it.id }
                        ) { class_ ->
                            ClassCard(
                                class_ = class_,
                                onClick = { onClassClick(class_.id) }
                            )
                        }

                        if (viewModel.hasMorePages) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
                is ClassesUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text((uiState as ClassesUiState.Error).message)
                            Button(onClick = { viewModel.loadClasses(true) }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }

        if (showFilters) {
            FilterSection(
                currentFilters = viewModel.currentFilters,
                onApplyFilters = { filters ->
                    viewModel.updateFilters(filters)
                    showFilters = false
                },
                onDismiss = { showFilters = false }
            )
        }
    }
}

@Composable
fun ClassCard(
    class_: Class,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = class_.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Profesor: ${class_.instructor}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Cupos: ${class_.availableSpots}/${class_.totalSpots}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Sede: ${class_.sede}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
