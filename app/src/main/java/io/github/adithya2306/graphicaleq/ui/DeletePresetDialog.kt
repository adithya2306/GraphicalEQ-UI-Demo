package io.github.adithya2306.graphicaleq.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun DeletePresetDialog(
    onConfirm: () -> Unit,
    onDismissDialog: () -> Unit
) {
    var showDialog by remember { mutableStateOf(true) }
    if (!showDialog) {
        onDismissDialog()
        return
    }

    AlertDialog(
        onDismissRequest = { showDialog = false },
        confirmButton = {
            TextButton(
                onClick = {
                    showDialog = false
                    onConfirm()
                }
            ) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { showDialog = false }
            ) {
                Text("No")
            }
        },
        text = {
            Text("Do you want to delete this preset?")
        }
    )
}