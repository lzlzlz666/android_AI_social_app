package com.example.sqltest2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        // 延迟3秒跳转到 MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            // 跳转到 MainActivity
            val intent = Intent(this@StartActivity, LoginActivity::class.java)
            startActivity(intent)
            // 结束 StartActivity
            finish()
        }, 3000) // 延迟3秒 (3000毫秒)
    }
}