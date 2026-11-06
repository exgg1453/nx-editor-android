package com.nxteam.nxeditor.data

import com.nxteam.nxeditor.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ChatService(
    private val apiKey: String,
    private val baseUrl: String,
    private val model: String
) {
    suspend fun send(systemPrompt: String, history: List<Message>): String = withContext(Dispatchers.IO) {
        val key = apiKey.trim()
        require(key.isNotEmpty()) { "Please add your Groq API key in Settings." }

        val endpoint = URL(baseUrl.trim().trimEnd('/') + "/chat/completions")
        val connection = endpoint.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $key")
        connection.doOutput = true

        val messages = JSONArray()
        messages.put(JSONObject().put("role", "system").put("content", systemPrompt))
        for (message in history) {
            messages.put(JSONObject().put("role", message.role).put("content", message.text))
        }
        val body = JSONObject()
            .put("model", model)
            .put("messages", messages)
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
