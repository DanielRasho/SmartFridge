package uvg.edu.gt.smartfridge.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme

@Composable
fun Title(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.displayLarge,
        modifier = modifier,
        textAlign = textAlign
    )
}

@Preview
@Composable
private fun TitlePreview(
    text: String = "Exit"
) {
    smartFridgeTheme {
        Title(text = text)
    }
}