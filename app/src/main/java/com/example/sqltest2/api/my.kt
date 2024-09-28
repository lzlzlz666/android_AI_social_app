package com.example.sqltest2.api

import android.content.Context
import android.util.Log
import com.example.sqltest2.model.Message
import com.example.sqltest2.utils.JWTStorageHelper
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object ApiMyListService {

    private val client = OkHttpClient()
    private val gson = Gson()

    fun getMessages(context: Context): Pair<List<Message>?, String?> {
        val token = JWTStorageHelper.getJwtToken(context)

        if (token == null) {
            Log.e("ApiUserService", "JWT 令牌不存在")
            return Pair(null, "用户未登录或 JWT 令牌不存在")
        }

        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/my/messages")
            .addHeader("Authorization", token)
            .get()
            .build()

        return try {
            val response: Response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e("ApiUserService", "Failed to fetch messages: ${response.code}")
                return Pair(null, "请求失败，请稍后重试。")
            }

            val responseData = response.body?.string() ?: return Pair(null, "服务器响应为空")
            val jsonResponse = JSONObject(responseData)

            // 检查返回的 code 字段
            val code = jsonResponse.getInt("code")
            if (code == 0) {
                // 成功获取消息数据
                val dataArray = jsonResponse.getJSONArray("data")
                val messages = mutableListOf<Message>()

                for (i in 0 until dataArray.length()) {
                    val messageJson = dataArray.getJSONObject(i)
                    val message = gson.fromJson(messageJson.toString(), Message::class.java)
                    messages.add(message)
                }

                return Pair(messages, null)
            } else {
                val message = jsonResponse.getString("message")
                Log.e("ApiUserService", "Operation failed: $message")
                return Pair(null, message)
            }

        } catch (e: IOException) {
            Log.e("ApiUserService", "Exception during fetching messages", e)
            return Pair(null, "网络错误，请检查网络连接。")
        }
    }

}