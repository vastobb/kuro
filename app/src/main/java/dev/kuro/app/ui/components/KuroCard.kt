package dev.kuro.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.kuro.app.ui.theme.KuroCardShape
import dev.kuro.app.ui.theme.KuroHeroShape
import dev.kuro.app.ui.theme.KuroSpacing

/** Standard 20dp card. */
@Composable
fun KuroCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            modifier = modifier,
            shape = KuroCardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            onClick = onClick,
            content = { Column(Modifier.padding(KuroSpacing.s16), content = content) }
        )
    } else {
        Card(
            modifier = modifier,
            shape = KuroCardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            content = { Column(Modifier.padding(KuroSpacing.s16), content = content) }
        )
    }
}

/** Large 28dp hero card for dashboard / import heroes. */
@Composable
fun KuroHeroCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = KuroHeroShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        content = { Column(Modifier.padding(KuroSpacing.s20), content = content) }
    )
}
