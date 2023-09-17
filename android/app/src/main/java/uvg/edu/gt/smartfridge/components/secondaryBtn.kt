package uvg.edu.gt.smartfridge.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uvg.edu.gt.smartfridge.R
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
fun SecondaryButton(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(10),
    ) {
        Text(text, color=MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
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
fun IconSecondaryButton(
    text: String,
    icon: Int,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(10),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            Icon(
                painter = painterResource(id = icon), // Load the icon from drawable
                contentDescription = "Button Icon",
                tint = Color.Unspecified,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text, color=MaterialTheme.colorScheme.onSurface,style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview
@Composable
private fun SecondaryButtonPreview_Logout(text: String = "Logout", onClick: () -> Unit = { -> }) {
    smartFridgeTheme {
        SecondaryButton(text = text, onClick = { onClick() })
    }
}

@Preview
@Composable
private fun IconSecondaryButtonPreview_Exit(
    text: String = "Exit",
    icon: Int = R.drawable.google_icon,
    onClick: () -> Unit = { -> }
) {
    smartFridgeTheme {
        IconSecondaryButton(text = text, icon = icon, onClick = { onClick() })
    }
}