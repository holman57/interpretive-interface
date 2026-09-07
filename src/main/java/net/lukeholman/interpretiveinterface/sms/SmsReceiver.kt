package net.lukeholman.interpretiveinterface.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

interface SmsListener {
    fun onSmsReceived(sender: String, messageBody: String)
}

class SmsReceiver : BroadcastReceiver() {

    companion object {
        private var listener: SmsListener? = null

        fun setSmsListener(smsListener: SmsListener?) {
            listener = smsListener
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (message in messages) {
                val sender = message.displayOriginatingAddress ?: "Unknown Sender"
                val body = message.messageBody ?: ""
                if (body.isNotBlank()) {
                    listener?.onSmsReceived(sender, body)
                }
            }
        }
    }
}
