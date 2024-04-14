package com.jfalck.musictimer.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Handler
import android.os.Looper
import android.util.Log

class MuteManager(private val context: Context) : OnAudioFocusChangeListener {

    private val handler: Handler = Handler(Looper.getMainLooper())
    private var playbackDelayed = false
    private var playbackNowAuthorized = false

    private val focusLock = Any()

    fun requestMediaFocus() {
        Log.d(TAG, "MuteManager initializing")
        // initializing variables for audio focus and playback management
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
            setAudioAttributes(AudioAttributes.Builder().run {
                setUsage(AudioAttributes.USAGE_GAME)
                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                build()
            })
            setAcceptsDelayedFocusGain(true)
            setOnAudioFocusChangeListener(this@MuteManager, handler)
            build()
        }

        // requesting audio focus and processing the response
        val res = audioManager.requestAudioFocus(focusRequest)
        synchronized(focusLock) {
            playbackNowAuthorized = when (res) {
                AudioManager.AUDIOFOCUS_REQUEST_FAILED -> false
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                    true
                }

                AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                    playbackDelayed = true
                    false
                }

                else -> false
            }
        }
    }

    // implementing OnAudioFocusChangeListener to react to focus changes
    override fun onAudioFocusChange(focusChange: Int) {
        /*
        * Do nothing here, the goal is only to stop the sound playing
        * */
    }

    companion object {
        private const val TAG = "MuteManager"
    }
}