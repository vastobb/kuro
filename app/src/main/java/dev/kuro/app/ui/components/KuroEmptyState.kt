package dev.kuro.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.kuro.app.ui.theme.KuroSpacing

/**
 * Empty state: illustration slot + headline + description + action.
 * Illustration is a caller-provided glyph so screens stay premium without asset weight.
 */
@Composable
fun KuroEmptyState(
    headline: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    glyph: @Composable () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(KuroSpacing.s32),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(96.dp),
            contentAlignment = Alignment.Center
        ) { glyph() }
        Spacer(Modifier.height(KuroSpacing.s16))
        Text(headline, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(KuroSpacing.s8))
        Text(
            description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(KuroSpacing.s20))
        KuroPrimaryButton(text = actionLabel, onClick = onAction)
    }
}
