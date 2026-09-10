package net.lukeholman.interpretiveinterface.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import net.lukeholman.interpretiveinterface.sms.SmsListener
import net.lukeholman.interpretiveinterface.sms.SmsReceiver
import net.lukeholman.interpretiveinterface.stt.SpeechToTextManager
import net.lukeholman.interpretiveinterface.tts.TextToSpeechManager

class VoiceBridgeService : Service(), SmsListener {

    private val binder = LocalBinder()
    private var ttsManager: TextToSpeechManager? = null
    private var isListening = false
    private var isPushToTalkActive = false

    companion object {
        const val CHANNEL_ID = "voice_bridge_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_VOICE = "net.lukeholman.ACTION_START_VOICE"
        const val ACTION_STOP_VOICE = "net.lukeholman.ACTION_STOP_VOICE"
        const val ACTION_PTT_DOWN = "net.lukeholman.ACTION_PTT_DOWN"
        const val ACTION_PTT_UP = "net.lukeholman.ACTION_PTT_UP"
    }

    inner class LocalBinder : Binder() {
        fun getService(): VoiceBridgeService = this@VoiceBridgeService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        SmsReceiver.setSmsListener(this)
        ttsManager = TextToSpeechManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification("Voice Bridge Active", "Listening for voice input & incoming dispatches")
        startForeground(NOTIFICATION_ID, notification)

        when (intent?.action) {
            ACTION_START_VOICE -> startListening()
            ACTION_STOP_VOICE -> stopListening()
            ACTION_PTT_DOWN -> setPushToTalk(true)
            ACTION_PTT_UP -> setPushToTalk(false)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        SmsReceiver.setSmsListener(null)
        ttsManager?.shutdown()
        stopListening()
    }

    override fun onSmsReceived(sender: String, messageBody: String) {
        val spokenAlert = "New message from $sender: $messageBody"
        ttsManager?.speak(spokenAlert)
    }

    fun startListening() {
        isListening = true
    }

    fun stopListening() {
        isListening = false
    }

    fun setPushToTalk(active: Boolean) {
        isPushToTalkActive = active
        if (active) {
            startListening()
        } else {
            stopListening()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Voice Bridge Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Maintains communicative voice bridge and hands-free notifications"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, content: String): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }
}
