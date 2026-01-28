package com.example.projektrejestratorjazdy

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    state: RecorderUiState,
    onToggleRecord: () -> Unit,
    onNavigateHistory: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Inicjalizacja kontrolera kamery tutaj, aby mieć do niego dostęp
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            bindToLifecycle(lifecycleOwner)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Tło: Kamera (Przekazujemy kontroler)
        CameraPreview(
            controller = cameraController,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Nakładka z danymi
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- GÓRA: Historia + Przycisk Zdjęcia ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onNavigateHistory) { Text("Historia") }

                // PRZYCISK: Zrób zdjęcie
                Button(
                    onClick = { takePhoto(context, cameraController) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                ) {
                    Text("FOTO")
                }
            }

            // --- ŚRODEK: Wykres Przeciążeń (Dashboard z wykresami) ---
            // Rysujemy prosty pasek G-Force
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                GForceChart(gForce = state.gForce)
            }

            // --- DÓŁ: Prędkość i Nagrywanie ---
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Karta z prędkością dla lepszej czytelności
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "%.0f km/h".format(state.currentSpeed),
                        fontSize = 64.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onToggleRecord,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isRecording) Color.Red else Color.Green
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text(
                        text = if (state.isRecording) "STOP NAGRYWANIA" else "START JAZDY",
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

// --- Komponent Wykresu (Spełnia wymaganie "Dashboard z wykresami") ---
@Composable
fun GForceChart(gForce: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Przeciążenie (G)", color = Color.White, fontSize = 12.sp)
        Canvas(modifier = Modifier
            .width(200.dp)
            .height(20.dp)
            .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
        ) {
            val center = size.width / 2
            val barWidth = (gForce * 50).dp.toPx() // Skalowanie paska

            // Linia środkowa
            drawLine(
                color = Color.White,
                start = Offset(center, 0f),
                end = Offset(center, size.height),
                strokeWidth = 2.dp.toPx()
            )

            // Pasek wartości
            val color = if (gForce > 1.5f) Color.Red else Color.Green
            drawRect(
                color = color,
                topLeft = Offset(center, 0f),
                size = Size(barWidth, size.height)
            )
        }
        Text(text = "%.2f G".format(gForce), color = Color.White, fontSize = 14.sp)
    }
}

// --- Logika robienia zdjęcia ---
private fun takePhoto(context: Context, controller: LifecycleCameraController) {
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US)
        .format(System.currentTimeMillis())

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "Rej_$name")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        // Dla Android 10+ (Q) zapisujemy w Pictures/Rejestrator
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/RejestratorJazdy")
        }
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    controller.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                Toast.makeText(context, "Zapisano zdjęcie!", Toast.LENGTH_SHORT).show()
            }

            override fun onError(exc: ImageCaptureException) {
                Toast.makeText(context, "Błąd zdjęcia: ${exc.message}", Toast.LENGTH_LONG).show()
            }
        }
    )
}