package uvg.edu.gt.smartfridge.views

import android.icu.util.LocaleData
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import uvg.edu.gt.smartfridge.components.UnitSelector
import uvg.edu.gt.smartfridge.components.dateField
import uvg.edu.gt.smartfridge.components.numberField
import uvg.edu.gt.smartfridge.components.textField
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme
import java.time.LocalDate

@Composable
fun NewIngredientView(navController: NavController){
    val name = remember { mutableStateOf("") }
    val amount = remember { mutableStateOf("") }
    var date = remember { mutableStateOf(LocalDate.now())}
    val categories = mapOf(
        "Weight" to listOf("Kg", "g", "Lb"),
        "Volume" to listOf("L", "mL", "Cups"),
        "Miscellaneous" to listOf("Bags", "Bottles")
    )

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
                numberField(label = "Amount", numberValue = amount)
            }
            UnitSelector(title = "Unit", categories = categories)
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