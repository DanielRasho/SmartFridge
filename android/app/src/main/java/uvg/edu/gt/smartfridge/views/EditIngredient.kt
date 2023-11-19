package uvg.edu.gt.smartfridge.views

import android.widget.Toast
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import uvg.edu.gt.smartfridge.models.Recipe
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme
import uvg.edu.gt.smartfridge.viewModels.EditIngredientViewModel
import uvg.edu.gt.smartfridge.viewModels.NewIngredientViewModel
import uvg.edu.gt.smartfridge.viewModels.SharedViewModel
import uvg.edu.gt.smartfridge.viewModels.TokenManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun EditIngredientView(navController: NavController, ingredient : Ingredient, sharedViewModel: SharedViewModel){
    val name = remember { mutableStateOf(ingredient.Name) }
    val amount = remember { mutableStateOf(ingredient.Quantity.toString()) }
    var measureUnit = remember { mutableStateOf(ingredient.Unit) }
    var date = remember { mutableStateOf(LocalDate.parse(ingredient.ExpireDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))) }
    val category = remember { mutableStateOf(ingredient.Category) }

    val jwtToken = sharedViewModel.jwtToken
    val editIngredientViewModel: EditIngredientViewModel = EditIngredientViewModel()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

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
                    Button(onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            val result = editIngredientViewModel.editIngredient(
                                jwtToken,
                                Ingredient(
                                    ingredient.IngredientId,
                                    "",
                                    name.value,
                                    category.value,
                                    amount.value.toFloat(),
                                    measureUnit.value,
                                    formatStringToDate(date.value.toString())
                                )
                            )

                            when (result.isSuccess) {
                                true -> {
                                    val message = result.getOrNull()!!
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        println(navController.toString())
                                        navController.navigate("Fridge")
                                    }

                                }

                                false -> {
                                    val exception =
                                        result.exceptionOrNull() as ResponseException
                                    println("ERROR! " + "Error ${exception.statusCode} : ${exception.message}")
                                    val code = exception.statusCode

                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            "Error ${exception.statusCode} : ${exception.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    if (code == 401) {
                                        withContext(Dispatchers.Main) {
                                            val tokenManager = TokenManager(context)
                                            tokenManager.clearJwtToken()
                                            // Navigate to the login view
                                            navController.navigate("Login"){
                                                // Clear the back stack to prevent going back to Login
                                                popUpTo("Home") {
                                                    inclusive = true
                                                }
                                            }
                                        }
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
    val sharedViewModel = SharedViewModel()
    val ingredient = Ingredient("234", "43", "Shrimp", "Meat", 34f, "Lb", "2/3/2023")
    smartFridgeTheme {
        EditIngredientView(navController = controller, ingredient, sharedViewModel)
    }
}