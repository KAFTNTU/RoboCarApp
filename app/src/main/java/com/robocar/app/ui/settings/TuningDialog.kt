package com.robocar.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.robocar.app.MainViewModel
import com.robocar.app.model.TuningSettings
import com.robocar.app.ui.theme.*

@Composable
fun TuningDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val tuning by viewModel.tuning.collectAsState()
    var invertL   by remember { mutableStateOf(tuning.invertL) }
    var invertR   by remember { mutableStateOf(tuning.invertR) }
    var trim      by remember { mutableStateOf(tuning.trim.toFloat()) }
    var turnSens  by remember { mutableStateOf(tuning.turnSens.toFloat()) }

    Dialog(onDismissRequest = {
        viewModel.updateTuning(TuningSettings(invertL, invertR, trim.toInt(), turnSens.toInt()))
        onDismiss()
    }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0F1B2E))
                .padding(20.dp)
        ) {
            // Header
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("⚙️ Налаштування шасі", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                IconButton(onClick = {
                    viewModel.updateTuning(TuningSettings(invertL, invertR, trim.toInt(), turnSens.toInt()))
                    onDismiss()
                }) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Invert motors
            SectionLabel("Інверсія моторів")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CheckToggle(label = "L (Лівий)", checked = invertL, onCheckedChange = { invertL = it })
                CheckToggle(label = "R (Правий)", checked = invertR, onCheckedChange = { invertR = it })
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trim
            SettingSlider(
                title = "ВІДХИЛЕННЯ (БАЛАНС)",
                left = "Ліво",
                right = "Право",
                value = trim,
                valueRange = -50f..50f,
                displayText = "${trim.toInt()}",
                onValueChange = { trim = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Turn sensitivity
            SettingSlider(
                title = "ЧУТЛИВІСТЬ ПОВОРОТУ",
                left = "Плавний",
                right = "Різкий",
                value = turnSens,
                valueRange = 10f..100f,
                displayText = "${turnSens.toInt()}%",
                onValueChange = { turnSens = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    viewModel.updateTuning(TuningSettings(invertL, invertR, trim.toInt(), turnSens.toInt()))
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Зберегти та закрити", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        color = TextMuted,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun CheckToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CardBg)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = AccentBlue)
        )
        Text(label, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(start = 4.dp))
    }
}

@Composable
private fun SettingSlider(
    title: String,
    left: String,
    right: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayText: String,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .padding(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(left, fontSize = 10.sp, color = TextMuted)
            Text(title, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.ExtraBold)
            Text(right, fontSize = 10.sp, color = TextMuted)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(thumbColor = AccentBlue, activeTrackColor = AccentBlue)
        )
        Text(
            displayText,
            fontSize = 12.sp,
            color = AccentBlue,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
