package com.nxteam.nxeditor.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class OutgoingMessage(
    val role: String,
    val text: String,
    val images: List<String>
)

private class TransientException(message: String) : RuntimeException(message)

class ChatService(
    private val apiKey: String,
    private val baseUrl: String,
    private val model: String
) {
    private val maxAttempts = 4

    suspend fun send(messages: List<OutgoingMessage>): String {
        var attempt = 0
        while (true) {
            try {
                return performRequest(messages)
            } catch (error: TransientException) {
                attempt += 1
                if (attempt >= maxAttempts) {
                    throw RuntimeException(error.message)
                }
                val seconds = Math.pow(2.0, (attempt - 1).toDouble())
                delay((seconds * 1000).toLong())
            }
        }
    }

    private suspend fun performRequest(messages: List<OutgoingMessage>): String = withContext(Dispatchers.IO) {
        val key = apiKey.trim()
        require(key.isNotEmpty()) { "Please add your Groq API key in Settings." }

        val endpoint = URL(baseUrl.trim().trimEnd('/') + "/chat/completions")
        val connection = endpoint.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $key")
        connection.doOutput = true

        val messagesArray = JSONArray()
        for (message in messages) {
            val messageObject = JSONObject().put("role", message.role)
            if (message.images.isEmpty()) {
                messageObject.put("content", message.text)
            } else {
                val parts = JSONArray()
                parts.put(JSONObject().put("type", "text").put("text", message.text))
                for (imageUrl in message.images) {
                    parts.put(
                        JSONObject()
                            .put("type", "image_url")
                            .put("image_url", JSONObject().put("url", imageUrl))
                    )
                }
                messageObject.put("content", parts)
            }
            messagesArray.put(messageObject)
        }

        val body = JSONObject()
            .put("model", model)
            .put("messages", messagesArray)
            .put("temperature", 0.7)

        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(body.toString())
        }

        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val responseText = BufferedReader(InputStreamReader(stream)).use { reader ->
            reader.readText()
        }
        connection.disconnect()

        if (status !in 200..299) {
            if (status == 429 || status in 500..599) {
                throw TransientException(responseText)
            }
            throw RuntimeException(responseText)
        }

        val json = JSONObject(responseText)
        val choices = json.optJSONArray("choices")
        if (choices == null || choices.length() == 0) {
            throw RuntimeException("The server returned an empty response.")
        }
        choices.getJSONObject(0).getJSONObject("message").getString("content")
    }
}
