package uvg.edu.gt.smartfridge.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme

@Composable
fun ActionBar(title: String, onBackButtonClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth()
    ) {
        IconButton(onClick = onBackButtonClick) {
            Icon(
                Icons.Rounded.ArrowBack,
                "Go Back Arrow",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(title, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Preview
@Composable
private fun ActionBarPreview() {
    smartFridgeTheme {
        ActionBar(title = "Recipe Editing")
    }
}