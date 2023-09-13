package uvg.edu.gt.smartfridge.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { onClick() },
        modifier = modifier.background(
            MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(10)
        )
    ) {
        Text(text, color = MaterialTheme.colorScheme.onPrimary)
    }
}

@Composable
fun IconPrimaryButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { onClick() },
        modifier = modifier.background(
            MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(10)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Icon(icon, contentDescription = "Button Icon")
            Text(text)
        }
    }
}

@Preview
@Composable
private fun PrimaryButtonPreview_Logout(text: String = "Logout", onClick: () -> Unit = { -> }) {
    PrimaryButton(text = text, onClick = { onClick() })
}

@Preview
@Composable
private fun IconPrimaryButtonPreview_Exit(
    text: String = "Exit",
    icon: ImageVector = Icons.Rounded.ArrowBack,
    onClick: () -> Unit = { -> }
) {
    IconPrimaryButton(text = text, icon = icon, onClick = { onClick() })
}