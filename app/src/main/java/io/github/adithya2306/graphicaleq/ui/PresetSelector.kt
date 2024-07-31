package io.github.adithya2306.graphicaleq.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import io.github.adithya2306.graphicaleq.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetSelector(viewModel: EqualizerViewModel) {
    val presets by viewModel.presets.collectAsState()
    val currentPreset by viewModel.preset.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    var showNewPresetDialog by remember { mutableStateOf(false) }
    var showRenamePresetDialog by remember { mutableStateOf(false) }
    var showDeletePresetDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                bottom = 24.dp
            ),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier
                .padding(end = 8.dp)
                .weight(1f)
        ) {
            TextField(
                value = currentPreset.name,
                onValueChange = { },
                readOnly = true,
                label = { Text("Preset") },
                singleLine = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                presets.forEach { preset ->
                    DropdownMenuItem(
                        text = { Text(text = preset.name) },
                        onClick = {
                            viewModel.setPreset(preset)
                            expanded = false
                        }
                    )
                }
            }
        }

        TooltipIconButton(
            icon = Icons.Default.Add,
            text = "New preset",
            onClick = { showNewPresetDialog = true }
        )

        if (currentPreset.isUserDefined) {
            TooltipIconButton(
                icon = Icons.Default.Edit,
                text = "Rename preset",
                onClick = { showRenamePresetDialog = true }
            )
            TooltipIconButton(
                icon = Icons.Default.Delete,
                text = "Delete preset",
                onClick = { showDeletePresetDialog = true }
            )
        }

        TooltipIconButton(
            icon = ImageVector.vectorResource(
                id = R.drawable.reset_settings_24px
            ),
            text = "Reset gains",
            onClick = { viewModel.reset() }
        )
    }

    // Dialogs

    if (showNewPresetDialog) {
        PresetNameDialog(
            onPresetNameSet = {
                return@PresetNameDialog viewModel.createNewPreset(name = it)
            },
            onDismissDialog = { showNewPresetDialog = false }
        )
    }

    if (showRenamePresetDialog) {
        PresetNameDialog(
            onPresetNameSet = {
                return@PresetNameDialog viewModel.renamePreset(
                    preset = currentPreset,
                    name = it
                )
            },
            onDismissDialog = { showRenamePresetDialog = false }
        )
    }

    if (showDeletePresetDialog) {
        DeletePresetDialog(
            onConfirm = { viewModel.deletePreset(currentPreset) },
            onDismissDialog = { showDeletePresetDialog = false }
        )
    }
}