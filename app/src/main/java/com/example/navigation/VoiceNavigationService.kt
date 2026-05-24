package com.example.navigation

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Foreground service to provide voice turn-by-turn guidance.
 */
class VoiceNavigationService : Service(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            speak("Voice navigation activated. Head north.")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val text = intent?.getStringExtra("TEXT_TO_SPEAK")
        if (text != null && tts != null) {
            speak(text)
        }
        return START_STICKY
    }

    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }
}
