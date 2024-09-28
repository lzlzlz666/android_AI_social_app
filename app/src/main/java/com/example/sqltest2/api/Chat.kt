package com.example.sqltest2.api

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.sqltest2.utils.JWTStorageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class ChatService(private val context: Context) {

    private val client = OkHttpClient()

    fun saveConversation(conversationName: String, callback: (Boolean) -> Unit) {
        val token = JWTStorageHelper.getJwtToken(context) // 获取 JWT token

        if (token == null) {
            Log.e("ChatService", "JWT 令牌不存在")
            callback(false)
            return
        }

        // 将 conversationName 作为查询参数附加到 URL 中
        val url = "${com.example.sqltest2.utils.Constants.BASE_URL}/chat?conversationName=$conversationName"

        // 构建请求
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", token) // 在请求头中添加 Authorization
            .post("".toRequestBody(null)) // 因为是参数传递，这里可以使用空的 POST body
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ChatService", "保存失败: ${e.message}")
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("ChatService", "保存成功")
                    callback(true)
                } else {
                    Log.e("ChatService", "保存失败: ${response.message}")
                    callback(false)
                }
            }
        })
    }
}

