package uvg.edu.gt.smartfridge.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun CheckBoxList(
    recipeIngredients: List<String>,
    userIngredients: List<String>
) {
    Column {
        recipeIngredients.forEach { recipeIngredient ->
            val isUserIngredient = userIngredients.contains(recipeIngredient)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)

            ) {
                Checkbox(
                    checked = isUserIngredient,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors( // Customize checkbox colors
                        checkedColor = MaterialTheme.colorScheme.onSurface,
                        checkmarkColor = MaterialTheme.colorScheme.background,
                        uncheckedColor = MaterialTheme.colorScheme.onSurface,

                    ),
                    modifier = Modifier.padding(8.dp)
                )

                Text(
                    text = recipeIngredient,
                    style= MaterialTheme.typography.bodyMedium,
                    fontSize=16.sp
                )
            }
        }
    }
}


@Preview
@Composable
fun CheckBoxListExample() {
    val recipeIngredients = remember { arrayListOf("Ingredient 1", "Ingredient 2", "Ingredient 3") }
    val userIngredients = remember { arrayListOf("Ingredient 2", "Ingredient 3") }
    CheckBoxList(recipeIngredients, userIngredients)

}