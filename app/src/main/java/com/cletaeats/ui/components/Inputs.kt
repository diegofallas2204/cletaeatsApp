package com.cletaeats.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.cletaeats.ui.theme.BrownDark
import com.cletaeats.ui.theme.BrownLight
import com.cletaeats.ui.theme.OrangeSoft

@Composable
fun CletaInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions? = null,
    isError: Boolean = false,
    supportingText: String? = null
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Default to Done imeAction if unspecified
    val finalKeyboardOptions = if (keyboardOptions.imeAction == KeyboardOptions.Default.imeAction) {
        keyboardOptions.copy(imeAction = ImeAction.Done)
    } else {
        keyboardOptions
    }

    val finalKeyboardActions = keyboardActions ?: KeyboardActions(
        onDone = {
            keyboardController?.hide()
            focusManager.clearFocus()
        },
        onNext = {
            focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down)
        }
    )

    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            // Filter out newline characters to prevent enter key insertion
            val filtered = input.replace("\n", "").replace("\r", "")
            onValueChange(filtered)
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        singleLine = singleLine,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = finalKeyboardOptions,
        keyboardActions = finalKeyboardActions,
        isError = isError,
        supportingText = supportingText?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OrangeSoft,
            unfocusedBorderColor = BrownLight,
            focusedLabelColor = BrownDark
        )
    )
}
