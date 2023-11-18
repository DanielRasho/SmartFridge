package uvg.edu.gt.smartfridge.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import uvg.edu.gt.smartfridge.components.UnitSelector
import uvg.edu.gt.smartfridge.components.dateField
import uvg.edu.gt.smartfridge.components.numberField
import uvg.edu.gt.smartfridge.components.spinnerField
import uvg.edu.gt.smartfridge.components.textField
import uvg.edu.gt.smartfridge.models.Ingredient
import uvg.edu.gt.smartfridge.models.Recipe
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme
import java.time.LocalDate

@Composable
fun EditIngredientView(navController: NavController, ingredient : Ingredient){
    val name = remember { mutableStateOf("") }
    val amount = remember { mutableStateOf("") }
    var measureUnit = remember { mutableStateOf("") }
    var date = remember { mutableStateOf(LocalDate.now()) }
    val category = remember { mutableStateOf("") }

    var (exposeUnitSelector, setExposeUnitSelector) = remember { mutableStateOf(false) }

    var navigateBack : () -> Unit = { /*TODO: implement when navGraph is created*/ }

    val unitCategories = mapOf(
        "Weight" to listOf("Kg", "g", "Lb"),
        "Volume" to listOf("L", "mL", "Cups"),
        "Miscellaneous" to listOf("Bags", "Bottles")
    )
    val itemCategories = listOf<String>("Fruits", "Vegetables", "Meat", "SeaFood", "Dairy & Alternatives",
        "Grains and Cereals", "Sweets and Desserts", "Beverages", "Condiments", "Sauces",
        "Herbs", "Oils and Fats", "Packaged Foods", "Baking Supplies")

    Surface (color = MaterialTheme.colorScheme.background){

        Column ( verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()){

            Column ( modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)) {

                Row (horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically){
                    IconButton(onClick = { /*TODO*/ },
                        content = { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null) })
                    Text(
                        "Edit Ingredient",
                        style = MaterialTheme.typography.displayMedium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))


                textField(label = "Name", textValue = name)
                Spacer(modifier = Modifier.height(16.dp))
                dateField("Date", date)
                Spacer(modifier = Modifier.height(16.dp))
                muteTextField(label = "Unit", textValue = measureUnit) {
                    setExposeUnitSelector(true) }
                Spacer(modifier = Modifier.height(16.dp))
                numberField(label = "Amount", numberValue = amount)
                Spacer(modifier = Modifier.height(16.dp))
                spinnerField(label = "Category", options = itemCategories, selectedTextHolder = category)

                Spacer(modifier = Modifier.height(24.dp))

                Row {
                    Button(onClick = { /*TODO*/ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(5.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Save",
                            color = MaterialTheme.colorScheme.onError)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = { /*TODO*/ },
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Delete",
                            color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
            if (exposeUnitSelector)
                UnitSelector(title = "Unit", categories = unitCategories,
                    onSelected = {
                        measureUnit.value = it
                        setExposeUnitSelector(false)},
                    onCloseButtonClick = { setExposeUnitSelector(false) })

        }
    }
}

@Preview
@Composable
fun previewEditIngredient(){
    val controller = rememberNavController()
    val ingredient = Ingredient("234", "43", "Shrimp", "Meat", 34f, "Lb", "2/3/2023")
    smartFridgeTheme {
        EditIngredientView(navController = controller, ingredient)
    }
}