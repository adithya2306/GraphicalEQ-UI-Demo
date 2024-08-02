package io.github.adithya2306.graphicaleq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.adithya2306.graphicaleq.ui.EqualizerScreen
import io.github.adithya2306.graphicaleq.ui.EqualizerViewModel
import io.github.adithya2306.graphicaleq.ui.theme.GraphicalEQTheme

class MainActivity : ComponentActivity() {
    private val viewModel: EqualizerViewModel by viewModels { EqualizerViewModel.Factory }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GraphicalEQTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(stringResource(id = R.string.main_title))
                            }
                        )
                    }
                ) { paddingValues ->
                    EqualizerScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}