package com.nxteam.nxeditor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.nxteam.nxeditor.data.SettingsStore

@Composable
fun SettingsScreen(settings: SettingsStore, modifier: Modifier = Modifier) {
    var apiKey by remember { mutableStateOf(settings.apiKey) }
    var model by remember { mutableStateOf(settings.model) }
    var visionModel by remember { mutableStateOf(settings.visionModel) }
    var baseUrl by remember { mutableStateOf(settings.baseUrl) }
    var systemPrompt by remember { mutableStateOf(settings.systemPrompt) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = apiKey,
            onValueChange = {
                apiKey = it
                settings.apiKey = it
            },
            label = { Text("Groq API Key") },
            placeholder = { Text("gsk_...") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Get a free key from console.groq.com and paste it here. It is stored only on this device.",
            style = MaterialTheme.typography.bodySmall
        )

        OutlinedTextField(
            value = model,
            onValueChange = {
                model = it
                settings.model = it
            },
            label = { Text("Model") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = visionModel,
            onValueChange = {
                visionModel = it
                settings.visionModel = it
            },
            label = { Text("Vision Model") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Used automatically when you attach a photo or video frame.",
            style = MaterialTheme.typography.bodySmall
        )

        OutlinedTextField(
            value = baseUrl,
            onValueChange = {
                baseUrl = it
                settings.baseUrl = it
            },
            label = { Text("Base URL") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "OpenAI compatible endpoint. Change this to use another provider.",
            style = MaterialTheme.typography.bodySmall
        )

        OutlinedTextField(
            value = systemPrompt,
            onValueChange = {
                systemPrompt = it
                settings.systemPrompt = it
            },
            label = { Text("System Prompt") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
        )
    }
}
