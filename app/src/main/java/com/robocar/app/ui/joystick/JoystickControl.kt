package com.robocar.app.ui.joystick

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun JoystickControl(
    size: Dp = 200.dp,
    stickRadius: Dp = 40.dp,
    onMove: (x: Int, y: Int) -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val maxDist = size.value / 2f - stickRadius.value
    var stickOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
    ) {
        // Base circle
        Canvas(
            modifier = Modifier
                .size(size)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            stickOffset = Offset.Zero
                            onRelease()
                        },
                        onDragCancel = {
                            isDragging = false
                            stickOffset = Offset.Zero
                            onRelease()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = stickOffset + dragAmount
                            val dist = sqrt(newOffset.x.pow(2) + newOffset.y.pow(2))
                            val angle = atan2(newOffset.y, newOffset.x)
                            val clamped = min(dist, maxDist)
                            stickOffset = Offset(
                                cos(angle) * clamped,
                                sin(angle) * clamped
                            )
                            val vx = ((stickOffset.x / maxDist) * 100).toInt().coerceIn(-100, 100)
                            val vy = ((-stickOffset.y / maxDist) * 100).toInt().coerceIn(-100, 100)
                            onMove(vx, vy)
                        }
                    )
                }
        ) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val baseRadius = size.toPx() / 2f - 4.dp.toPx()
            val sR = stickRadius.toPx()

            // Draw base ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1A2540),
                        Color(0xFF0D1525)
                    ),
                    center = center,
                    radius = baseRadius
                ),
                radius = baseRadius,
                center = center
            )
            drawCircle(
                color = Color(0xFF3A6FFF).copy(alpha = 0.25f),
                radius = baseRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Cross guide lines
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(center.x, 8f),
                end = Offset(center.x, size.toPx() - 8f),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(8f, center.y),
                end = Offset(size.toPx() - 8f, center.y),
                strokeWidth = 1.dp.toPx()
            )

            // Stick
            val stickCenter = center + stickOffset
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF5A9FFF),
                        Color(0xFF2260CC)
                    ),
                    center = stickCenter,
                    radius = sR
                ),
                radius = sR,
                center = stickCenter
            )
            // Glow when dragging
            if (isDragging) {
                drawCircle(
                    color = Color(0xFF3A80FF).copy(alpha = 0.3f),
                    radius = sR + 8.dp.toPx(),
                    center = stickCenter
                )
            }
            // Stick shine
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = stickCenter - Offset(sR * 0.3f, sR * 0.3f),
                    radius = sR * 0.5f
                ),
                radius = sR * 0.5f,
                center = stickCenter - Offset(sR * 0.3f, sR * 0.3f)
            )
        }
    }
}
