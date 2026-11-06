package com.nxteam.nxeditor.model

import java.util.UUID

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val role: String,
    val text: String,
    val images: List<String> = emptyList()
)
