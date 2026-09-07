# Interpretive Interface

**Interpretive Interface** is an Android application that enables hands-free voice and text communication with your phone. It integrates Speech-to-Text (STT) for voice input, Text-to-Speech (TTS) for spoken responses, an interactive text chat interface, and automatic text-to-speech reading of incoming SMS text messages.

---

## Key Features

- 🎙️ **Voice Listening & Speech-to-Text (STT)**: Speak directly to the phone; voice input is transcribed in real-time into the chat interface.
- 💬 **Interactive Text Chat**: Send and receive messages in a clean Jetpack Compose chat UI.
- 🔊 **Text-to-Speech (TTS)**: Spoken audio feedback for chat responses and incoming notifications.
- 📲 **SMS Text Receiver**: Listens for incoming SMS messages and automatically converts them to speech (TTS) so you can hear incoming texts without looking at the screen.
- 🔐 **Runtime Permissions**: Manages `RECORD_AUDIO` and `RECEIVE_SMS` runtime permissions securely.

---

## Requirements

- **Android SDK**: Min SDK 26 (Android 8.0), Target SDK 35
- **JDK**: Java 17 / Kotlin 2.0+
- **Gradle**: 9.3+

---

## Project Structure

```
interpretive-interface/
├── app/ (Root Kotlin source)
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   └── java/net/lukeholman/interpretiveinterface/
│   │       ├── MainActivity.kt
│   │       ├── data/
│   │       │   └── ChatMessage.kt
│   │       ├── stt/
│   │       │   └── SpeechToTextManager.kt
│   │       ├── tts/
│   │       │   └── TextToSpeechManager.kt
│   │       ├── sms/
│   │       │   └── SmsReceiver.kt
│   │       └── ui/
│   │           ├── ChatViewModel.kt
│   │           └── ChatScreen.kt
│   └── build.gradle.kts
├── LICENSE (MIT Open Source License)
└── README.md
```

---

## Build & Run

### Command Line
```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug
```

---

## License

This project is licensed under the [MIT License](LICENSE).
