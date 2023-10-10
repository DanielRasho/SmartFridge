package uvg.edu.gt.smartfridge.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitSelector(
    title: String = "Title",
    categories : Map<String, List<String>> = mapOf<String, List<String>>(),
    onSelected: (String) -> Unit = {},
    onCloseButtonClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    // Saves the current selected category
    val (selectedCategory, setSelectedCategory) = remember { mutableStateOf(
        categories.entries.iterator().next().key // Get first key of categories, map, to have default.
    ) }
    // Saves the current category's items to display.
    val (selectedCategoryItems, setSelectedCategoryItems) = remember { mutableStateOf(categories[selectedCategory]) }
    // Saves the item selected from the ones saved on selectedCategoryItems.
    val (selectedUnit, setSelectedUnit) = remember { mutableStateOf(selectedCategoryItems?.get(0)) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Function to select a category.
    val changeCategoryAction = {newCategory: String ->
        setSelectedCategory(newCategory)
        setSelectedCategoryItems(categories[newCategory])
    }

    // Function to select an item from within a category.
    val changeUnitAction = { newUnit: String ->
        if (newUnit != selectedUnit) {
            setSelectedUnit(newUnit)
            onSelected(newUnit)
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
                    title,
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
            Row ( modifier = Modifier
                .height(screenHeight / 3)) {
                Column(horizontalAlignment = Alignment.Start,
                    modifier = Modifier.verticalScroll(rememberScrollState()))
                {
                    categories.keys.forEach {
                        if (selectedCategory == it) {
                            TextButton(
                                onClick = { changeCategoryAction(it) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RectangleShape,
                                modifier = modifier.width(screenWidth / 2),
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Text(it, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        } else {
                            TextButton(
                                onClick = { changeCategoryAction(it) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background),
                                shape = RectangleShape,
                                modifier = modifier
                                    .width(screenWidth / 2),
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Text(it, color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                        Divider(
                            modifier = Modifier.width(screenWidth / 2),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline)
                    }
                }
                Divider(
                    modifier = Modifier.fillMaxHeight().width(1.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline)
                Column (verticalArrangement = Arrangement.Top,
                    modifier = Modifier.verticalScroll(rememberScrollState())) {
                    selectedCategoryItems?.forEach {
                        if(it == selectedUnit)
                        Button(
                            onClick = { changeUnitAction(it) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline),
                            shape = RectangleShape,
                            modifier = modifier.width(screenWidth / 2),
                        ) {
                            Text(it, color = MaterialTheme.colorScheme.onBackground)
                        }
                        else
                            Button(
                                onClick = { changeUnitAction(it) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background),
                                shape = RectangleShape,
                                modifier = modifier.width(screenWidth / 2),
                            ) {
                                Text(it, color = MaterialTheme.colorScheme.onBackground)
                            }
                        Divider(
                            modifier = Modifier.width(screenWidth / 2),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun UnitSelectorPreview() {
    var (text, setText) = remember { mutableStateOf("TEXT") }

    val categories = mapOf(
        "Weight" to listOf("Kg", "g", "Lb"),
        "Volume" to listOf("L", "mL", "Cups"),
        "Miscellaneous" to listOf("Bags", "Bottles"),
        "Category 4" to listOf("Bags", "Bottles"),
        "Category 5" to listOf("Bags", "Bottles"),
        "Category 6" to listOf("Bags", "Bottles"),
        "Category 7" to listOf("Bags", "Bottles"),
    )

    // Dummy function to edit "text" variable.
    val onSelectedFun : (String) -> Unit = {selectedText ->
        Log.i("INFO", "Selected text is $selectedText")
        setText(selectedText)
    }
    smartFridgeTheme {
        Column {
            UnitSelector("Hello World", categories, onSelectedFun)
            Text(text = text)
        }
    }
}