package uvg.edu.gt.smartfridge.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme

@Composable
fun UnitSelector(
    onSelected: () -> Unit = {},
    onCloseButtonClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .background(MaterialTheme.colorScheme.inverseSurface)
                .fillMaxWidth()
        ) {
            Text("Unit", color = MaterialTheme.colorScheme.inverseOnSurface, modifier = modifier.padding(16.dp, 0.dp))
            IconButton(onClick = onCloseButtonClick) {
                Icon(
                    Icons.Rounded.Close,
                    "Close unit selector",
                    tint = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        }
    }
}

@Preview
@Composable
private fun UnitSelectorPreview() {
    smartFridgeTheme {
        UnitSelector()
    }
}