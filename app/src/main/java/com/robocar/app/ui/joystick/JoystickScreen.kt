package com.robocar.app.ui.joystick

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robocar.app.MainViewModel
import com.robocar.app.ble.BleState
import com.robocar.app.ui.theme.*

@Composable
fun JoystickScreen(viewModel: MainViewModel) {
    val motorL by viewModel.motorL.collectAsState()
    val motorR by viewModel.motorR.collectAsState()
    val speedMult by viewModel.speedMultiplier.collectAsState()
    val gyroEnabled by viewModel.gyroEnabled.collectAsState()
    val bleState by viewModel.bleState.collectAsState()

    var speedPercent by remember { mutableStateOf(100f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Joystick
            JoystickControl(
                size = 220.dp,
                stickRadius = 44.dp,
                onMove = { vx, vy ->
                    if (!gyroEnabled) viewModel.updateJoystick(vx, vy)
                },
                onRelease = {
                    if (!gyroEnabled) viewModel.resetJoystick()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Motor display + Gyro button
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBg)
                    .padding(16.dp)
            ) {
                // L motor
                MotorDisplay(label = "L", value = motorL, modifier = Modifier.weight(1f))

                // Gyro button
                val gyroColor by animateColorAsState(
                    if (gyroEnabled) AccentBlue else Color(0xFF2D3A50)
                )
                IconButton(
                    onClick = { viewModel.toggleGyro() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(gyroColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = "Gyro",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // R motor
                MotorDisplay(label = "R", value = motorR, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Speed slider + label
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ПОТУЖНІСТЬ", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text("${speedPercent.toInt()}%", fontSize = 10.sp, color = AccentBlue, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = speedPercent,
                    onValueChange = {
                        speedPercent = it
                        viewModel.setSpeed(it.toInt())
                    },
                    valueRange = 10f..100f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentBlue,
                        activeTrackColor = AccentBlue,
                        inactiveTrackColor = Color(0xFF2D3A50)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // HEX display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                val lHex = (motorL.toByte().toInt() and 0xFF).toString(16).uppercase().padStart(2, '0')
                val rHex = (motorR.toByte().toInt() and 0xFF).toString(16).uppercase().padStart(2, '0')
                Text(
                    text = "HEX: $lHex $rHex 00 00",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFFFD060),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MotorDisplay(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color(0xFF34D399),
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = value.toString(),
            fontSize = 22.sp,
            color = AccentBlue,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
