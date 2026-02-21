package com.robocar.app

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robocar.app.ble.BleState
import com.robocar.app.ui.*
import com.robocar.app.ui.blockly.BlocklyScreen
import com.robocar.app.ui.joystick.JoystickScreen
import com.robocar.app.ui.settings.TuningDialog
import com.robocar.app.ui.theme.*

@Composable
fun AppScreen(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val bleState by viewModel.bleState.collectAsState()
    val showLog by viewModel.showLog.collectAsState()
    val showTuning by viewModel.showTuning.collectAsState()
    val showPassword by viewModel.showPassword.collectAsState()
    val showScan by viewModel.showScanDialog.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // === TOP BAR ===
            TopBar(
                bleState = bleState,
                currentTab = currentTab,
                onTabChange = { viewModel.setTab(it) },
                onBleClick = { viewModel.onConnectClicked() },
                onLogClick = { viewModel.toggleLog() },
                onPasswordClick = { viewModel.togglePassword() },
                onTuningClick = { viewModel.toggleTuning() }
            )

            // === CONTENT ===
            Box(modifier = Modifier.weight(1f)) {
                if (currentTab == 0) {
                    JoystickScreen(viewModel = viewModel)
                } else {
                    BlocklyScreen(viewModel = viewModel)
                }
            }
        }

        // === DIALOGS ===
        if (showScan) ScanDialog(viewModel = viewModel, onDismiss = { viewModel.dismissScan() })
        if (showLog) LogModal(viewModel = viewModel, onDismiss = { viewModel.toggleLog() })
        if (showTuning) TuningDialog(viewModel = viewModel, onDismiss = { viewModel.toggleTuning() })
        if (showPassword) PasswordDialog(viewModel = viewModel, onDismiss = { viewModel.togglePassword() })
    }
}

@Composable
private fun TopBar(
    bleState: BleState,
    currentTab: Int,
    onTabChange: (Int) -> Unit,
    onBleClick: () -> Unit,
    onLogClick: () -> Unit,
    onPasswordClick: () -> Unit,
    onTuningClick: () -> Unit
) {
    val isConnected = bleState is BleState.Connected
    val isScanning = bleState is BleState.Scanning || bleState is BleState.Connecting

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A1525).copy(alpha = 0.95f))
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        // Status dot
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusDot(bleState = bleState)
            val statusText = when (bleState) {
                is BleState.Connected   -> bleState.deviceName
                is BleState.Connecting  -> "Підключення..."
                is BleState.Scanning    -> "Сканування..."
                is BleState.Disconnected -> ""
                is BleState.Error       -> "Помилка"
            }
            if (statusText.isNotEmpty()) {
                Text(statusText, fontSize = 11.sp, color = TextMuted, maxLines = 1)
            }
        }

        // Nav tabs
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1A2540))
                .padding(4.dp)
        ) {
            NavTab(
                selected = currentTab == 0,
                icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Joystick", modifier = Modifier.size(20.dp)) },
                onClick = { onTabChange(0) }
            )
            NavTab(
                selected = currentTab == 1,
                icon = { Icon(Icons.Default.Extension, contentDescription = "Blockly", modifier = Modifier.size(20.dp)) },
                onClick = { onTabChange(1) }
            )
            Divider(
                modifier = Modifier.height(24.dp).width(1.dp).align(Alignment.CenterVertically),
                color = Color.White.copy(alpha = 0.1f)
            )
            NavTab(
                selected = false,
                icon = { Icon(Icons.Default.Lock, contentDescription = "Password", modifier = Modifier.size(18.dp)) },
                onClick = onPasswordClick
            )
            NavTab(
                selected = false,
                icon = { Icon(Icons.Default.Book, contentDescription = "Log", modifier = Modifier.size(18.dp)) },
                onClick = onLogClick
            )
        }

        // BT button
        val btColor = when {
            isConnected -> Color(0xFFDC2626)
            isScanning  -> Color(0xFFF59E0B)
            else        -> AccentBlue
        }
        IconButton(
            onClick = onBleClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(btColor)
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = "Connect",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusDot(bleState: BleState) {
    val color = when (bleState) {
        is BleState.Connected   -> AccentGreen
        is BleState.Connecting,
        is BleState.Scanning    -> AccentAmber
        is BleState.Error       -> AccentRed
        else                    -> Color(0xFF475569)
    }
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun NavTab(
    selected: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val bg = if (selected) Color(0xFF2D4080) else Color.Transparent
    val tint = if (selected) Color.White else Color(0xFF64748B)

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
    ) {
        CompositionLocalProvider(LocalContentColor provides tint) {
            icon()
        }
    }
}
