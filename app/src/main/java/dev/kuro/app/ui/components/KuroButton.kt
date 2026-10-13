package dev.kuro.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.kuro.app.ui.theme.KuroButtonShape

/** Primary 18dp button. Scale 0.97 + haptic handled by caller via press indication. */
@Composable
fun KuroPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        shape = KuroButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun KuroSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = KuroButtonShape
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun KuroGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = KuroButtonShape
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun KuroFullWidthPrimary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    KuroPrimaryButton(text = text, onClick = onClick, modifier = modifier.fillMaxWidth())
}
