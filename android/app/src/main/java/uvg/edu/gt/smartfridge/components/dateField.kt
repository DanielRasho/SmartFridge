package uvg.edu.gt.smartfridge.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import uvg.edu.gt.smartfridge.ui.theme.smartFridgeTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun dateField (pickedDate : MutableState<LocalDate>) {

    // State definitions
    val formattedDate by remember {
        derivedStateOf {
            DateTimeFormatter
                .ofPattern("MMM dd yyyy")
                .format(pickedDate.value)
        }
    }
    val dateDialogState = rememberMaterialDialogState()

    // Layout definition
    Row (verticalAlignment = Alignment.CenterVertically){
       Text(text = "Date",
           style = MaterialTheme.typography.labelLarge,
           color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column (horizontalAlignment = Alignment.CenterHorizontally){
            TextButton(onClick = { dateDialogState.show() },
                shape = RectangleShape,
                modifier = Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(0.dp)
            )
                {
                Text(text = formattedDate,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Divider(color = MaterialTheme.colorScheme.onSurface)
        }
        // Date picker definition
        MaterialDialog(
            dialogState = dateDialogState,
            buttons = {
                positiveButton(text = "Ok")
                negativeButton(text = "Cancel")
            }
        ) {
            datepicker(
                initialDate = LocalDate.now(),
                title = "Acquisition date",
                colors = DatePickerDefaults.colors(
                    headerBackgroundColor = MaterialTheme.colorScheme.primary,
                    headerTextColor = MaterialTheme.colorScheme.onPrimary,
                    dateActiveBackgroundColor = MaterialTheme.colorScheme.primary,
                    dateActiveTextColor = MaterialTheme.colorScheme.onPrimary),
            ) {
                pickedDate.value = it
            }
        }

    }
}

@Preview
@Composable
fun dateFieldText(){
    smartFridgeTheme {
        var owo = remember { mutableStateOf(LocalDate.now())}
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ){
            dateField(owo)
        }
    }
}