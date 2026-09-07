package net.lukeholman.interpretiveinterface.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.lukeholman.interpretiveinterface.data.ChatMessage
import net.lukeholman.interpretiveinterface.data.MessageSender
import net.lukeholman.interpretiveinterface.sms.SmsListener
import net.lukeholman.interpretiveinterface.sms.SmsReceiver
import net.lukeholman.interpretiveinterface.stt.SpeechToTextManager
import net.lukeholman.interpretiveinterface.tts.TextToSpeechManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatViewModel(application: Application) : AndroidViewModel(application), SmsListener {

    val sttManager = SpeechToTextManager(application)
    val ttsManager = TextToSpeechManager(application)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isTtsEnabled = MutableStateFlow(true)
    val isTtsEnabled: StateFlow<Boolean> = _isTtsEnabled.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        SmsReceiver.setSmsListener(this)
        addWelcomeMessage()
    }

    private fun addWelcomeMessage() {
        val welcome = ChatMessage(
            sender = MessageSender.PHONE,
            senderName = "Interpretive Interface",
            content = "Hello! I am your Interpretive Interface. You can talk to me with voice (STT), send text messages, or let me speak incoming SMS texts out loud for you."
        )
        _messages.value = listOf(welcome)
    }

    fun onInputTextChanged(newText: String) {
        _inputText.value = newText
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return

        val userMessage = ChatMessage(
            sender = MessageSender.USER,
            senderName = "You",
            content = text
        )

        val currentList = _messages.value.toMutableList()
        currentList.add(userMessage)

        // Generate response from system
        val responseText = generatePhoneResponse(text)
        val phoneMessage = ChatMessage(
            sender = MessageSender.PHONE,
            senderName = "Interpretive Interface",
            content = responseText
        )
        currentList.add(phoneMessage)

        _messages.value = currentList
        _inputText.value = ""

        if (_isTtsEnabled.value) {
            ttsManager.speak(responseText)
        }
    }

    fun startListening() {
        _statusMessage.value = "Listening..."
        sttManager.startListening(
            onResult = { recognizedText ->
                _inputText.value = recognizedText
                _statusMessage.value = "Recognized: \"$recognizedText\""
                viewModelScope.launch {
                    // Auto-send recognized speech
                    sendMessage()
                }
            },
            onError = { error ->
                _statusMessage.value = "STT Error: $error"
            },
            onPartialResult = { partial ->
                _inputText.value = partial
            }
        )
    }

    fun stopListening() {
        sttManager.stopListening()
        _statusMessage.value = null
    }

    override fun onSmsReceived(sender: String, messageBody: String) {
        val smsMessage = ChatMessage(
            sender = MessageSender.SMS,
            senderName = sender,
            content = messageBody
        )

        val currentList = _messages.value.toMutableList()
        currentList.add(smsMessage)
        _messages.value = currentList

        if (_isTtsEnabled.value) {
            ttsManager.speak("Incoming text message from $sender. $messageBody")
        }
    }

    fun speakMessage(message: ChatMessage) {
        val textToSpeak = when (message.sender) {
            MessageSender.USER -> "You said: ${message.content}"
            MessageSender.PHONE -> message.content
            MessageSender.SMS -> "SMS from ${message.senderName}: ${message.content}"
        }
        ttsManager.speak(textToSpeak)
    }

    fun toggleTts() {
        _isTtsEnabled.value = !_isTtsEnabled.value
        if (!_isTtsEnabled.value) {
            ttsManager.stop()
        }
    }

    private fun generatePhoneResponse(input: String): String {
        val lower = input.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") ->
                "Hello there! I'm listening. How can I help you today?"
            lower.contains("time") ->
                "The current system time is " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(
                    Date()
                ) + "."
            lower.contains("date") ->
                "Today is " + SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(
                    Date()
                ) + "."
            lower.contains("who are you") || lower.contains("what is this") ->
                "I am Interpretive Interface, an Android voice and text chat interface with Speech-to-Text and Text-to-Speech capabilities."
            else ->
                "I received your message: \"$input\". I am ready for your next command or incoming text!"
        }
    }

    override fun onCleared() {
        super.onCleared()
        sttManager.destroy()
        ttsManager.shutdown()
        SmsReceiver.setSmsListener(null)
    }
}
