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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.indicatorLine
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme

/**
 * Representation of single line text field.
 * @param label Text next to input to describe what type of information is the texField about.
 * @param textValue Contains a reference to the value hold by the user, every time the value changes,
 * this textValue.value is updated.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun textField (label : String, textValue: MutableState<String>) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
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
                onValueChange = {textValue.value = it},
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
        // keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number))
        }
    }
}

@Preview
@Composable
fun textFieldTest() {
    smartFridgeTheme {
        var textSaved = remember { mutableStateOf("") }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Text(text = textSaved.value, fontSize = 12.sp)
            textField(label = "Name", textValue = textSaved)
            textField(label = "Name", textValue = textSaved)
        }
    }
}