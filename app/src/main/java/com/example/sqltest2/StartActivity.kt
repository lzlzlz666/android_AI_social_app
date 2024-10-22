package com.example.sqltest2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.jwt.JWT
import com.example.sqltest2.utils.JWTStorageHelper
import java.util.Date

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        // 检查是否存在 JWT 令牌
        val jwtToken = JWTStorageHelper.getJwtToken(this)
        if (jwtToken != null && !isTokenExpired(jwtToken)) {
            // 否则，延迟3秒跳转到 LoginActivity
            Handler(Looper.getMainLooper()).postDelayed({
                // 如果令牌存在，跳转到 LayoutActivity
                val intent = Intent(this, LayoutActivity::class.java)
                startActivity(intent)
                finish()
            }, 3000) // 延迟3秒 (3000毫秒)
        } else {
            // 否则，延迟3秒跳转到 LoginActivity
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this@StartActivity, LoginActivity::class.java)
                startActivity(intent)
                finish() // 结束 StartActivity
            }, 3000) // 延迟3秒 (3000毫秒)
        }
    }


    // 检查 JWT 令牌是否过期
    private fun isTokenExpired(token: String): Boolean {
        val jwt = JWT(token)
        val expiresAt = jwt.expiresAt
        return expiresAt?.before(Date()) ?: true // 如果过期时间为空，则认为已过期
    }
}
