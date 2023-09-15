package uvg.edu.gt.smartfridge.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumTouchTargetEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitSelector(
    onSelected: () -> Unit = {},
    onCloseButtonClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val categories = mapOf(
        "Weight" to listOf("Kg", "g", "Lb"),
        "Volume" to listOf("L", "mL", "Cups"),
        "Miscellaneous" to listOf("Bags", "Bottles", "")
    )

    val (selectedCategory, setSelectedCategory) = remember { mutableStateOf("Weight") }
    val (selectedCategoryItems, setselectedCategoryItems) = remember { mutableStateOf(categories[selectedCategory]) }

    val (selectedUnit, setSelectedUnit) = remember { mutableStateOf(selectedCategoryItems?.get(0)) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val changeCategoryAction = {newCategory: String ->
        setSelectedCategory(newCategory)
        setselectedCategoryItems(categories[newCategory])
    }

    val changeUnitAction = { newUnit: String ->
        if (newUnit != selectedUnit) {
            setSelectedUnit(newUnit)
            onSelected()
        }
    }

    CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
        Column(modifier = modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = modifier
                    .background(MaterialTheme.colorScheme.inverseSurface)
                    .fillMaxWidth()
            ) {
                Text(
                    "Unit",
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = modifier.padding(16.dp, 0.dp)
                )
                IconButton(onClick = onCloseButtonClick) {
                    Icon(
                        Icons.Rounded.Close,
                        "Close unit selector",
                        tint = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
            Row {
                Column(horizontalAlignment = Alignment.Start) {
                    categories.keys.forEach {
                        if (selectedCategory == it) {
                            TextButton(
                                onClick = { changeCategoryAction(it) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RectangleShape,
                                modifier = modifier.width(screenWidth / 2),
                                contentPadding = PaddingValues(0.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Text(it, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        } else {
                            TextButton(
                                onClick = { changeCategoryAction(it) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background),
                                shape = RectangleShape,
                                modifier = modifier.width(screenWidth / 2),
                                contentPadding = PaddingValues(0.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Text(it, color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    }
                }
                Column (verticalArrangement = Arrangement.Top) {
                    selectedCategoryItems?.forEach {
                        Button(
                            onClick = { changeUnitAction(it) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background),
                            shape = RectangleShape,
                            modifier = modifier.width(screenWidth / 2),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(it, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
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