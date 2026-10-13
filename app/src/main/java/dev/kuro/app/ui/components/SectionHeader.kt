package dev.kuro.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import dev.kuro.app.ui.theme.KuroSpacing

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = KuroSpacing.s20),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        if (actionLabel != null) {
            TextButton(onClick = { onAction?.invoke() }) {
                Text(
                    actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/** Flagship screen titles: letterspaced eyebrow + display title + muted subtitle. */
@Composable
fun KuroScreenTitle(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    eyebrow: String = ""
) {
    Column(modifier = modifier.padding(horizontal = KuroSpacing.s20)) {
        if (eyebrow.isNotEmpty()) {
            Text(
                eyebrow.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 3.sp),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(title, style = MaterialTheme.typography.displayLarge)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = KuroSpacing.s8)
        )
    }
}

/** Home brand header: wordmark eyebrow + greeting + status line. */
@Composable
fun BrandHeader(
    greeting: String,
    statusLine: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = KuroSpacing.s20)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Kuro",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                " ●",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                "  workspace".uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 3.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = KuroSpacing.s8)
            )
        }
        Text(
            greeting,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(top = KuroSpacing.s12)
        )
        Text(
            statusLine,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = KuroSpacing.s8)
        )
    }
}
