package com.robocar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.window.Dialog
import com.robocar.app.MainViewModel
import com.robocar.app.ui.theme.*

@Composable
fun LogModal(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val logs by viewModel.logs.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) listState.animateScrollToItem(logs.size - 1)
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0A1020))
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📋 Лог", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Row {
                    IconButton(onClick = { viewModel.clearLog() }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = TextMuted)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(logs) { (msg, type) ->
                    val color = when (type) {
                        "tx"   -> Color(0xFF60A5FA)
                        "rx"   -> Color(0xFF34D399)
                        "err"  -> Color(0xFFF87171)
                        "warn" -> Color(0xFFFBBF24)
                        else   -> Color(0xFFCBD5E1)
                    }
                    val prefix = when (type) {
                        "tx"   -> "→ "
                        "rx"   -> "← "
                        "err"  -> "✗ "
                        "warn" -> "⚠ "
                        else   -> "  "
                    }
                    Text(
                        text = "$prefix$msg",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = color,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
