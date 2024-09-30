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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ChatService(private val context: Context) {

    private val client = OkHttpClient()

    // 保存对话
    fun saveConversation(conversationName: String, callback: (Boolean, Int?) -> Unit) {
        val token = JWTStorageHelper.getJwtToken(context) // 获取 JWT token

        if (token == null) {
            Log.e("ChatService", "JWT 令牌不存在")
            callback(false, null)
            return
        }

        val url = "${com.example.sqltest2.utils.Constants.BASE_URL}/chat?conversationName=$conversationName"

        // 构建请求
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", token)
            .post("".toRequestBody(null))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ChatService", "保存失败: ${e.message}")
                callback(false, null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    // 假设返回 JSON 数据包含 conversationId
                    val conversationId = extractConversationId(json) // 从 JSON 提取 conversationId
                    callback(true, conversationId)
                } else {
                    Log.e("ChatService", "保存失败: ${response.message}")
                    callback(false, null)
                }
            }
        })
    }

    fun saveMessage(conversationId: Int, message: String, senderType: String): Boolean {
        val token = JWTStorageHelper.getJwtToken(context) ?: return false

        // 使用 JSONObject 自动处理 JSON 转义
        val jsonBody = JSONObject().apply {
            put("conversationId", conversationId)
            put("senderType", senderType)
            put("message", message)
        }.toString()

        Log.d("ChatService", "请求体大小: ${jsonBody.toByteArray().size} 字节")

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toRequestBody(mediaType)

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/chat/saveMessage")
            .addHeader("Authorization", token)
            .post(requestBody)
            .build()

        return try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            Log.d("ChatService", "响应内容: $responseBody")

            if (!response.isSuccessful) {
                Log.e("ChatService", "数据库插入失败，错误代码: ${response.code}, 响应消息: $responseBody")
            }
            response.isSuccessful
        } catch (e: IOException) {
            Log.e("ChatService", "保存消息失败: ${e.message}")
            e.printStackTrace()
            false
        }
    }


    // 解析返回的 JSON，提取 conversationId
    private fun extractConversationId(json: String?): Int? {
        // 简单 JSON 解析，假设数据结构符合预期
        // 使用 JSON 解析库（如 Gson 或 Kotlin 的 Json）更为安全
        val jsonObject = JSONObject(json)
        val dataObject = jsonObject.optJSONObject("data")
        return dataObject?.optInt("conversationId")
    }

    // 获取对话组的方法，返回 JSON 数据
    fun fetchConversationGroups(callback: (Boolean, JSONArray?) -> Unit) {
        val token = JWTStorageHelper.getJwtToken(context) // 获取 JWT 令牌
        if (token.isNullOrEmpty()) {
            Log.e("ChatService", "JWT 令牌为空，无法发送请求")
            callback(false, null) // 回调错误
            return
        }

        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/chat/conversationGroups")
            .addHeader("Authorization", token) // 携带 JWT 令牌
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ChatService", "请求失败: ${e.message}")
                callback(false, null) // 请求失败时回调
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        try {
                            val json = JSONObject(responseData)
                            val dataArray = json.getJSONArray("data")
                            callback(true, dataArray) // 成功时回调数据
                        } catch (e: Exception) {
                            Log.e("ChatService", "解析失败: ${e.message}")
                            callback(false, null)
                        }
                    } else {
                        callback(false, null)
                    }
                } else {
                    Log.e("ChatService", "请求失败，响应码: ${response.code}")
                    callback(false, null)
                }
            }
        })
    }

    fun fetchMessagesByConversationId(conversationId: Int, callback: (Boolean, JSONArray?) -> Unit) {
        val token = JWTStorageHelper.getJwtToken(context) ?: return callback(false, null)

        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/chat/Messages?conversationId=$conversationId")
            .addHeader("Authorization", token)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val dataArray = JSONObject(responseData).getJSONArray("data")
                    callback(true, dataArray)
                } else {
                    callback(false, null)
                }
            }
        })
    }

}


