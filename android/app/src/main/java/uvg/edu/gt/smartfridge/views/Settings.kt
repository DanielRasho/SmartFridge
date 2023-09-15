package uvg.edu.gt.smartfridge.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uvg.edu.gt.smartfridge.components.IconPrimaryButton
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme

@Composable
fun SettingsView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp, 0.dp)
    ) {
        Text(
            "Settings",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = modifier.height(20.dp))
        Text(
            "Style",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = true, onClick = { /*TODO*/ })
            Text(
                "Light Theme",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = false, onClick = { /*TODO*/ })
            Text(
                "Dark Theme",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxWidth()
        ) {
            IconPrimaryButton(text = "Logout", icon = Icons.Rounded.ExitToApp) {}
        }
    }
}

@Preview
@Composable
fun SettingsViewPreview() {
    smartFridgeTheme {
        SettingsView()
    }
}