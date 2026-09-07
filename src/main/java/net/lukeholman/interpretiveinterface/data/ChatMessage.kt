package net.lukeholman.interpretiveinterface.data

import java.util.UUID

enum class MessageSender {
    USER,
    PHONE,
    SMS
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val senderName: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSpoken: Boolean = false
)
