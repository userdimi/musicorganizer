package de.colognecode.musicorganizer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.colognecode.musicorganizer.theme.Purple_700

/**
 * (c) Dimitri Simon on 02.10.21
 */
object MusicOrganizerLoadingSpinner {
    @Composable
    fun LoadingSpinnerComposable() {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator(
                color = Purple_700
            )
        }
    }
}
