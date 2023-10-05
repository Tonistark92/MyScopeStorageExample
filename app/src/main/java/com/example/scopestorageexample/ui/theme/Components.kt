package com.example.scopestorageexample.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun dropDownMenu(
    list: List<String>?,
    onSelected: (album: String) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }
    val suggestions = list
    var selectedText by remember { mutableStateOf("") }

    var textfieldSize by remember { mutableStateOf(Size.Zero) }

    val icon = if (expanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown


    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = { enterdTest ->
                selectedText = enterdTest
            },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    //This value is used to assign to the DropDown the same width
                    textfieldSize = coordinates.size.toSize()
                },
            label = { Text(text = "Choose From Your Albums") },
            trailingIcon = {
                Icon(
                    icon, "contentDescription",
                    Modifier.clickable { expanded = !expanded }, tint = Color(0xff15420F)
                )
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.Black, // Set text color
                focusedLabelColor = Color.LightGray, // Set focused hint color
                unfocusedLabelColor = Color.LightGray, // Set unfocused hint color
                disabledLabelColor = Color.LightGray, // Set disabled hint color
                cursorColor = Color.Transparent, // Hide the cursor
                focusedBorderColor = Color.Transparent, // Remove border when focused
                unfocusedBorderColor = Color.Transparent, // Remove border when unfocused
                disabledBorderColor = Color.Transparent,// Remove border when disabled

            ),
            enabled = false

        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { textfieldSize.width.toDp() })
                .background(Color.White)
        ) {
            suggestions?.forEach { label ->
                DropdownMenuItem(text = {
                    Text(text = label, color = Color.Black)
                }, onClick = {
                    selectedText = label
                    onSelected(label)
                    expanded = false
                })
            }
        }
    }

}