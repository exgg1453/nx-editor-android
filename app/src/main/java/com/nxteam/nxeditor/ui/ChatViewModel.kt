package com.nxteam.nxeditor.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nxteam.nxeditor.data.ChatService
import com.nxteam.nxeditor.data.SettingsStore
import com.nxteam.nxeditor.model.Message
import kotlinx.coroutines.launch

class ChatViewModel(private val settings: SettingsStore) : ViewModel() {
    val messages = mutableStateListOf<Message>()
    var input by mutableStateOf("")
    var isSending by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun send() {
        val trimmed = input.trim()
        if (trimmed.isEmpty() || isSending) {
            return
        }
        input = ""
        errorMessage = null
        messages.add(Message(role = "user", text = trimmed))
        isSending = true

        val service = ChatService(settings.apiKey, settings.baseUrl, settings.model)
        val history = messages.toList()
        val systemPrompt = settings.systemPrompt

        viewModelScope.launch {
            try {
                val reply = service.send(systemPrompt, history)
                messages.add(Message(role = "assistant", text = reply))
            } catch (error: Exception) {
                errorMessage = error.message ?: "The request failed."
            }
            isSending = false
        }
    }

    fun clear() {
        messages.clear()
        errorMessage = null
    }
}

class ChatViewModelFactory(private val settings: SettingsStore) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ChatViewModel(settings) as T
    }
}
