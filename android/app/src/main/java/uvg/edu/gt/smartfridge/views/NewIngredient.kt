package uvg.edu.gt.smartfridge.views

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import uvg.edu.gt.smartfridge.components.UnitSelector
import uvg.edu.gt.smartfridge.components.dateField
import uvg.edu.gt.smartfridge.components.numberField
import uvg.edu.gt.smartfridge.components.spinnerField
import uvg.edu.gt.smartfridge.components.textField
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme
import java.time.LocalDate

@Composable
fun NewIngredientView(navController: NavController){
    val name = remember { mutableStateOf("") }
    val amount = remember { mutableStateOf("") }
    var measureUnit = remember { mutableStateOf("") }
    var date = remember { mutableStateOf(LocalDate.now())}
    val category = remember { mutableStateOf("") }

    var (exposeUnitSelector, setExposeUnitSelector) = remember { mutableStateOf(false) }

    val unitCategories = mapOf(
        "Weight" to listOf("Kg", "g", "Lb"),
        "Volume" to listOf("L", "mL", "Cups"),
        "Miscellaneous" to listOf("Bags", "Bottles")
    )
    val itemCategories = listOf<String>("Fruits", "Vegetables", "Meat", "SeaFood", "Dairy & Alternatives",
        "Grains and Cereals", "Sweets and Desserts", "Beverages", "Condiments", "Sauces",
        "Herbs", "Oils and Fats", "Packaged Foods", "Baking Supplies")

    Surface (color = MaterialTheme.colors.background){

        Column ( verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()){
            Column ( modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)) {
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

// Executes onClick function when pressed,
// and displays the value given on texValue.
@Composable
fun muteTextField(label: String, textValue: MutableState<String>, onClick : () -> Unit ){
    // Layout definition
    Row (verticalAlignment = Alignment.CenterVertically){
        Column (modifier = Modifier.weight(2f)) {
            Text(text = label,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column (modifier = Modifier.weight(5f),
            horizontalAlignment = Alignment.CenterHorizontally){
            BasicTextField(value = textValue.value,
                onValueChange = { },
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() },
                textStyle = androidx.compose.material3.MaterialTheme.typography.labelLarge.copy(
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(androidx.compose.material3.MaterialTheme.colorScheme.primary),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Preview
@Composable
fun previewNewIngredient(){
    val controller = rememberNavController()
    smartFridgeTheme {
        NewIngredientView(navController = controller)
    }
}