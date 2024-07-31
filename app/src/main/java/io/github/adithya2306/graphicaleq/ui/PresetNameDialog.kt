package io.github.adithya2306.graphicaleq.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun PresetNameDialog(
    presetName: String = "",
    onPresetNameSet: (String) -> Boolean,
    onDismissDialog: () -> Unit
) {
    var showDialog by remember { mutableStateOf(true) }
    if (!showDialog) {
        onDismissDialog()
        return
    }
    var text by remember { mutableStateOf(presetName) }
    var isError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { showDialog = false }
    ) {
        Card(
            shape = RoundedCornerShape(size = 16.dp),
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Preset name") },
                isError = isError,
                singleLine = true,
                modifier = Modifier.padding(16.dp)
            )
            if (isError) {
                Text(
                    "Preset name already exists!",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                )
            }
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .align(Alignment.End)
                    .padding(end = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancel")
                }
                TextButton(
                    onClick = {
                        if (onPresetNameSet(text)) {
                            showDialog = false
                            isError = false
                        } else { // validation failed
                            isError = true
                        }
                    }
                ) {
                    Text("OK")
                }
            }
        }

    }
}