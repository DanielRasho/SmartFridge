package uvg.edu.gt.smartfridge.components

import android.graphics.drawable.Icon
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun spinnerField(label : String){
    val options = listOf("Food", "Bill Payment", "Recharges", "Outing", "Other")
    val (selectedOption, setOption) = remember{ mutableStateOf("") }
    val (isDropdownExposed, setExpose) = remember{ mutableStateOf(false) }

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
            ExposedDropdownMenuBox(expanded = isDropdownExposed,
                onExpandedChange = { setExpose(it) }
            ) {
                Row (){
                    BasicTextField( value = selectedOption,
                        readOnly = true,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        textStyle = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine = true
                    )
                    Icon( imageVector = Icons.Default.Person,
                        contentDescription = "Person Icon",
                        tint = MaterialTheme.colorScheme.outline
                    )
                    ExposedDropdownMenu(expanded = isDropdownExposed,
                        onDismissRequest = { setExpose(false) }
                    ) {
                        options.forEach(){
                            DropdownMenuItem(
                                text = { Text(text = it) },
                                onClick = {
                                    setOption(it)
                                    setExpose(false)
                                })
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            // keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number))
        }
    }
}

@Preview
@Composable
fun spinnerFieldTest(){
    smartFridgeTheme {
        var owo = remember { mutableStateOf(LocalDate.now())}
        var value = remember { mutableStateOf("") }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ){
            spinnerField(label = "Texting")
        }
    }
}