package com.example.projektrejestratorjazdy

import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraPreview(
    controller: LifecycleCameraController, // Zmiana: kontroler przekazywany jako parametr
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                this.controller = controller
            }
        },
        modifier = modifier
    )
}