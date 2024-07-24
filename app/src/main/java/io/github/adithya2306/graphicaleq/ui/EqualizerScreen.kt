package io.github.adithya2306.graphicaleq.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EqualizerScreen(
    viewModel: EqualizerViewModel,
    modifier: Modifier
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 8.dp
            )
            .then(modifier),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            PresetSelector(viewModel = viewModel)
            EqualizerBands(viewModel = viewModel)
        }
    }
}