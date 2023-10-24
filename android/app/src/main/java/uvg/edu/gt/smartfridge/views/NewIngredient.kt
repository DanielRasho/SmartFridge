package uvg.edu.gt.smartfridge.views

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uvg.edu.gt.smartfridge.components.UnitSelector
import uvg.edu.gt.smartfridge.components.dateField
import uvg.edu.gt.smartfridge.components.numberField
import uvg.edu.gt.smartfridge.components.spinnerField
import uvg.edu.gt.smartfridge.components.textField
import uvg.edu.gt.smartfridge.data.ResponseException
import uvg.edu.gt.smartfridge.models.Ingredient
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme
import uvg.edu.gt.smartfridge.viewModels.LoginViewModel
import uvg.edu.gt.smartfridge.viewModels.NewIngredientViewModel
import uvg.edu.gt.smartfridge.viewModels.SharedViewModel
import java.time.LocalDate

@Composable
fun NewIngredientView(sharedViewModel: SharedViewModel, navController: NavController){
    val jwtToken = sharedViewModel.jwtToken
    val newingredientViewModel : NewIngredientViewModel = NewIngredientViewModel()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val name = remember { mutableStateOf("") }
    val amount = remember { mutableStateOf("") }
    var measureUnit = remember { mutableStateOf("") }
    var date = remember { mutableStateOf(LocalDate.now())}
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
                    IconButton(onClick = { navController.navigate("Fridge") },
                        content = {Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)})
                    Text(
                        "Add Ingredient",
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
                    Button(onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            val result = newingredientViewModel.addIngredient(jwtToken,
                                Ingredient("",name.value, category.value, amount.value.toFloat(),measureUnit.value,date.value.toString())
                            )

                            when(result.isSuccess){
                                true -> {
                                    val mensaje = result.getOrNull()!!
                                    Toast.makeText(context,mensaje,Toast.LENGTH_SHORT).show()
                                    withContext(Dispatchers.Main){
                                        println(navController?.toString())
                                        navController.navigate("Fridge")
                                    }

                                }
                                false -> {
                                    val exception = result.exceptionOrNull() as ResponseException
                                    println("ERROR! " + "Error ${exception.statusCode} : ${exception.message}")

                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context,
                                            "Error ${exception.statusCode} : ${exception.message}",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    },
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
                        Text(text = "Continue",
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

// Executes onClick function when pressed,
// and displays the value given on texValue.
@Composable
fun muteTextField(label: String, textValue: MutableState<String>, onClick : () -> Unit ){
    // Layout definition
    Row (verticalAlignment = Alignment.CenterVertically){
        Column (modifier = Modifier.weight(2f)) {
            Text(text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outlineVariant)
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
                textStyle = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Preview
@Composable
fun previewNewIngredient(){
    val controller = rememberNavController()
    val sharedViewModel = SharedViewModel()
    smartFridgeTheme {
        NewIngredientView(sharedViewModel,navController = controller)
    }
}