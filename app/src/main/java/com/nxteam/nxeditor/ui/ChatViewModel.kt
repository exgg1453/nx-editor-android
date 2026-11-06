package com.nxteam.nxeditor.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nxteam.nxeditor.data.ChatService
import com.nxteam.nxeditor.data.OutgoingMessage
import com.nxteam.nxeditor.data.SettingsStore
import com.nxteam.nxeditor.model.Message
import kotlinx.coroutines.launch

class ChatViewModel(private val settings: SettingsStore) : ViewModel() {
    val messages = mutableStateListOf<Message>()
    val pendingImages = mutableStateListOf<String>()
    var input by mutableStateOf("")
    var isSending by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val imageLimit = 3

    fun addImage(dataUrl: String) {
        if (pendingImages.size >= imageLimit) {
            return
        }
        pendingImages.add(dataUrl)
    }

    fun removePendingImage(index: Int) {
        if (index in pendingImages.indices) {
            pendingImages.removeAt(index)
        }
    }

    fun send() {
        val trimmed = input.trim()
        if ((trimmed.isEmpty() && pendingImages.isEmpty()) || isSending) {
            return
        }

        val images = pendingImages.toList()
        val promptText = if (trimmed.isEmpty() && images.isNotEmpty()) "What is in this image?" else trimmed

        input = ""
        pendingImages.clear()
        errorMessage = null
        messages.add(Message(role = "user", text = promptText, images = images))
        isSending = true

        val hasImages = images.isNotEmpty()
        val model = if (hasImages) settings.visionModel else settings.model
        val service = ChatService(settings.apiKey, settings.baseUrl, model)

        val outgoing = mutableListOf(OutgoingMessage("system", settings.systemPrompt, emptyList()))
        messages.forEachIndexed { index, message ->
            val isLast = index == messages.size - 1
            outgoing.add(OutgoingMessage(message.role, message.text, if (isLast) message.images else emptyList()))
        }

        viewModelScope.launch {
            try {
                val reply = service.send(outgoing)
                messages.add(Message(role = "assistant", text = reply))
            } catch (error: Exception) {
                errorMessage = error.message ?: "The request failed."
            }
            isSending = false
        }
    }

    fun clear() {
        messages.clear()
        pendingImages.clear()
        errorMessage = null
    }
}

class ChatViewModelFactory(private val settings: SettingsStore) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ChatViewModel(settings) as T
    }
}
