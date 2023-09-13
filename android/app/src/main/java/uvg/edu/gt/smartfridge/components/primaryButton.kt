package uvg.edu.gt.smartfridge.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme

/**
 * A normal text Primary Button
 *
 * If you need and image inside the button please use an [IconPrimaryButton].
 *
 * @param text The text to display.
 * @param onClick The action to execute when the button is clicked.
 * @param modifier The modifier of the button.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        shape = RoundedCornerShape(10),
    ) {
        Text(text, color = MaterialTheme.colorScheme.onError)
    }
}

/**
 * A primary button with an icon on it's side.
 *
 * If you don't need an icon please use [PrimaryButton].
 *
 * @param text The text to display.
 * @param icon The icon to display adjacent to the text.
 * @param onClick The action to execute when the button is clicked.
 * @param modifier The modifier of the button.
 */
@Composable
fun IconPrimaryButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        shape = RoundedCornerShape(10),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            Icon(icon, contentDescription = "Button Icon", tint = MaterialTheme.colorScheme.onError)
            Text(text, color = MaterialTheme.colorScheme.onError)
        }
    }
}

@Preview
@Composable
private fun PrimaryButtonPreview_Logout(text: String = "Logout", onClick: () -> Unit = { -> }) {
    smartFridgeTheme {
        PrimaryButton(text = text, onClick = { onClick() })
    }
}

@Preview
@Composable
private fun IconPrimaryButtonPreview_Exit(
    text: String = "Exit",
    icon: ImageVector = Icons.Rounded.ArrowBack,
    onClick: () -> Unit = { -> }
) {
    smartFridgeTheme {
        IconPrimaryButton(text = text, icon = icon, onClick = { onClick() })
    }
}