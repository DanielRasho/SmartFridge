package uvg.edu.gt.smartfridge.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme

/**
 * Representation of single line number field (input that just accepts digits).
 * @param label Text next to input to describe what type of information is the texField about.
 * @param numberValue Contains a reference to the value hold by the user, every time the value changes,
 * this numberValue.value is updated.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun numberField (label : String, numberValue: MutableState<String>) {

    // Layout definition
    Row (verticalAlignment = Alignment.CenterVertically){
        Column {
            Text(text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column (horizontalAlignment = Alignment.CenterHorizontally){
            BasicTextField(value = numberValue.value,
                onValueChange = {
                    if(it.isDigitsOnly())
                    numberValue.value = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth(),
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
fun numberFieldTest() {
    smartFridgeTheme {
        var numberSaved = remember { mutableStateOf("") }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Text(text = numberSaved.value, fontSize = 12.sp)
            numberField(label = "Age", numberValue = numberSaved)
        }
    }
}