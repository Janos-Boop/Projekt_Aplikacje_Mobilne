package com.example.projektrejestratorjazdy

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun SessionDetailScreen(
    sessionId: Int,
    viewModel: RecorderViewModel,
    onBack: () -> Unit
) {
    var points by remember { mutableStateOf<List<DrivePoint>>(emptyList()) }

    LaunchedEffect(sessionId) {
        points = viewModel.getSessionPoints(sessionId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background // Tło ekranu zgodne z systemem
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Button(onClick = onBack) { Text("Wróć") }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Prędkość (km/h)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (points.isNotEmpty()) {
                // Wykres w ciemnej karcie dla lepszego kontrastu
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)), // Ciemnoszare tło
                    modifier = Modifier.fillMaxWidth().height(250.dp)
                ) {
                    AdvancedLineChart(
                        dataPoints = points.map { it.speed.toFloat() },
                        timestamps = points.map { it.timestamp },
                        lineColor = Color(0xFF4287f5), // Jasnoniebieski
                        unit = "km/h"
                    )
                }
            } else {
                Text("Brak danych (trasa była zbyt krótka?)")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Przeciążenie (G)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (points.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)), // Ciemnoszare tło
                    modifier = Modifier.fillMaxWidth().height(250.dp)
                ) {
                    AdvancedLineChart(
                        dataPoints = points.map { it.gForce },
                        timestamps = points.map { it.timestamp },
                        lineColor = Color(0xFFff4444), // Czerwony
                        unit = "G"
                    )
                }
            }
        }
    }
}

@Composable
fun AdvancedLineChart(
    dataPoints: List<Float>,
    timestamps: List<Long>,
    lineColor: Color,
    unit: String
) {
    if (dataPoints.isEmpty()) return

    val maxVal = (dataPoints.maxOrNull() ?: 1f) * 1.1f // +10% marginesu u góry
    val startTime = timestamps.firstOrNull() ?: 0L

    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val width = size.width
        val height = size.height
        val paddingLeft = 80f // Miejsce na napisy osi Y
        val paddingBottom = 50f // Miejsce na napisy osi X
        val graphWidth = width - paddingLeft
        val graphHeight = height - paddingBottom

        // Farba do tekstu (Biała, bo tło jest ciemne)
        val textPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 32f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.RIGHT
        }

        val axisPaint = Paint().apply {
            color = android.graphics.Color.LTGRAY
            strokeWidth = 2f
        }

        // --- OŚ Y (Pionowa) ---
        drawLine(
            color = Color.Gray,
            start = Offset(paddingLeft, 0f),
            end = Offset(paddingLeft, graphHeight),
            strokeWidth = 3f
        )

        // Podziałka Osi Y (5 linii)
        val stepsY = 4
        for (i in 0..stepsY) {
            val ratio = i.toFloat() / stepsY
            val y = graphHeight - (ratio * graphHeight)
            val value = ratio * maxVal

            // Linia pozioma (siatka)
            drawLine(
                color = Color.DarkGray.copy(alpha = 0.5f),
                start = Offset(paddingLeft, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )

            // Liczba przy osi
            drawContext.canvas.nativeCanvas.drawText(
                "%.1f".format(Locale.US, value),
                paddingLeft - 15f,
                y + 10f, // Wyrównanie w pionie
                textPaint
            )
        }

        // --- OŚ X (Pozioma) ---
        drawLine(
            color = Color.Gray,
            start = Offset(paddingLeft, graphHeight),
            end = Offset(width, graphHeight),
            strokeWidth = 3f
        )

        // Podziałka Osi X (Czas - 3 punkty)
        val timePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 30f
            textAlign = Paint.Align.CENTER
        }

        if (timestamps.isNotEmpty()) {
            val duration = timestamps.last() - startTime
            val stepsX = 3 // Start, środek, koniec
            for (i in 0 until stepsX) {
                val ratio = i.toFloat() / (stepsX - 1)
                val x = paddingLeft + (ratio * graphWidth)
                val timeSec = (ratio * duration) / 1000

                // Kreska na osi
                drawLine(
                    color = Color.Gray,
                    start = Offset(x, graphHeight),
                    end = Offset(x, graphHeight + 15f),
                    strokeWidth = 3f
                )

                // Czas (np. 15s)
                drawContext.canvas.nativeCanvas.drawText(
                    "%.0fs".format(timeSec),
                    x,
                    height, // Na samym dole
                    timePaint
                )
            }
        }

        // --- Rysowanie Linii Wykresu ---
        val path = Path()
        val stepX = graphWidth / (dataPoints.size - 1).coerceAtLeast(1)

        dataPoints.forEachIndexed { index, value ->
            val x = paddingLeft + (index * stepX)
            val y = graphHeight - ((value / maxVal) * graphHeight)

            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}