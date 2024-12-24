package com.example.sqltest2.utils

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sqltest2.R
import java.util.*

object SpeechRecognizerUtil {

    private var speechRecognizer: SpeechRecognizer? = null

    // 初始化语音识别器
    fun init(context: Context) {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        }
    }

    // 开始语音识别
    fun startListening(context: Context, intent: Intent, onResult: (String) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
            == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            speechRecognizer?.startListening(intent)
            speechRecognizer?.setRecognitionListener(object : android.speech.RecognitionListener {
                override fun onReadyForSpeech(p0: android.os.Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(p0: Float) {}
                override fun onBufferReceived(p0: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    Toast.makeText(context, "语音识别失败，错误代码: $error", Toast.LENGTH_SHORT).show()
                }

                override fun onResults(results: android.os.Bundle?) {
                    results?.let {
                        val matches = it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            onResult(matches[0])  // 回调返回结果
                        }
                    }
                }

                override fun onPartialResults(p0: android.os.Bundle?) {}
                override fun onEvent(p0: Int, p1: android.os.Bundle?) {}
            })
        } else {
            ActivityCompat.requestPermissions(
                context as android.app.Activity,
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                1
            )
        }
    }

    // 创建语音识别的 Intent
    fun createRecognizerIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话")
        }
    }

    // 释放资源
    fun release() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
