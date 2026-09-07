package net.lukeholman.interpretiveinterface

import net.lukeholman.interpretiveinterface.data.ChatMessage
import net.lukeholman.interpretiveinterface.data.MessageSender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ChatMessageTest {

    @Test
    fun testChatMessageCreation() {
        val message = ChatMessage(
            sender = MessageSender.USER,
            senderName = "Tester",
            content = "Hello world"
        )

        assertNotNull(message.id)
        assertEquals(MessageSender.USER, message.sender)
        assertEquals("Tester", message.senderName)
        assertEquals("Hello world", message.content)
    }

    @Test
    fun testSmsMessageCreation() {
        val sms = ChatMessage(
            sender = MessageSender.SMS,
            senderName = "+1234567890",
            content = "Test incoming text message"
        )

        assertEquals(MessageSender.SMS, sms.sender)
        assertEquals("+1234567890", sms.senderName)
        assertEquals("Test incoming text message", sms.content)
    }
}
