package com.robocar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.robocar.app.MainViewModel
import com.robocar.app.ui.theme.*

@Composable
fun PasswordDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    var pass by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0F1B2E))
                .padding(24.dp)
        ) {
            Text(
                "ДОСТУП",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                placeholder = { Text("****", color = TextMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp,
                    letterSpacing = 8.sp,
                    color = Color.White
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (pass.isNotEmpty()) {
                        viewModel.sendPassword(pass)
                        onDismiss()
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = Color(0xFF334155),
                    cursorColor = AccentBlue
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                ) { Text("Скасувати") }

                Button(
                    onClick = {
                        if (pass.isNotEmpty()) { viewModel.sendPassword(pass); onDismiss() }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) { Text("OK", fontWeight = FontWeight.Bold) }
            }
        }
    }
}
