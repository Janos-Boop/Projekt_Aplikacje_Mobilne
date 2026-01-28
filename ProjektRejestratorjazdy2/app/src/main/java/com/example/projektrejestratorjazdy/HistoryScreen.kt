package com.example.projektrejestratorjazdy

import androidx.compose.foundation.clickable // Import
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    history: List<DriveSession>,
    onDelete: (DriveSession) -> Unit,
    onSessionClick: (Int) -> Unit, // <--- NOWY PARAMETR
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Historia przejazdów") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Button(onClick = onBack, modifier = Modifier.padding(8.dp)) {
                Text("Wróć do kamery")
            }

            LazyColumn {
                items(history) { session ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { onSessionClick(session.id) }, // <--- KLIKNIĘCIE OTWIERA SZCZEGÓŁY
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(session.timestamp))
                                Text("Data: $date")
                                Text("Max prędkość: %.1f km/h".format(session.maxSpeed))
                                Text("Max G: %.2f".format(session.maxGForce)) // Pokazujemy też G
                            }
                            IconButton(onClick = { onDelete(session) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Usuń")
                            }
                        }
                    }
                }
            }
        }
    }
}