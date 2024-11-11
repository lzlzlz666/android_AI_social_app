package com.example.sqltest2.api

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.sqltest2.model.Comment
import com.example.sqltest2.model.CommentResponse
import com.example.sqltest2.utils.JWTStorageHelper
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import kotlin.concurrent.thread

class CommentService(private val context: Context) {
    private val client = OkHttpClient()

    // 获取评论数据
    fun fetchComments(articleId: Int, callback: (List<Comment>?) -> Unit) {
        val token = JWTStorageHelper.getJwtToken(context) // 获取 JWT token

        if (token == null) {
            Log.e("CommentService", "JWT 令牌不存在")
            return
        }

        // 构造请求 URL
        val url = "http://112.124.27.151:8090/comment?articleId=$articleId"  // 根据实际情况替换

        // 创建请求
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", token)
            .build()

        // 在后台线程中进行网络请求
        thread {
            try {
                // 执行网络请求
                val response: Response = client.newCall(request).execute()

                // 处理响应
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val comments = parseComments(responseBody)

                    // 在主线程更新 UI
                    Handler(Looper.getMainLooper()).post {
                        callback(comments)
                    }
                } else {
                    Log.e("CommentService", "获取评论失败: ${response.code}")
                    // 在主线程调用回调
                    Handler(Looper.getMainLooper()).post {
                        callback(null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CommentService", "网络请求异常: ${e.message}")
                // 在主线程调用回调
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            }
        }
    }

    // 解析返回的 JSON 数据
    private fun parseComments(responseBody: String?): List<Comment>? {
        return try {
            val gson = Gson()
            val commentResponse = gson.fromJson(responseBody, CommentResponse::class.java)
            commentResponse.data // 返回解析后的评论数据
        } catch (e: Exception) {
            e.printStackTrace()
            null // 如果解析失败，返回 null
        }
    }


    // 发送评论数据
    fun postComment(content: String, articleId: Int, parentCommentId: Int?, callback: (Boolean) -> Unit) {
        val token = JWTStorageHelper.getJwtToken(context)

        if (token == null) {
            Log.e("CommentService", "JWT 令牌不存在")
            callback(false)
            return
        }

        val url = "http://112.124.27.151:8090/comment"

        // 构建 JSON 数据
        val json = """
        {
            "content": "$content",
            "articleId": $articleId,
            "parentCommentId": ${parentCommentId ?: "null"}
        }
    """.trimIndent()

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json
        )

        // 创建请求
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", token)
            .post(body)
            .build()

        // 在后台线程中进行网络请求
        Thread {
            try {
                val response: Response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("CommentService", "Response: $responseBody")  // 打印响应内容

                    // 使用 Gson 解析响应的 JSON 数据
                    val jsonResponse = JsonParser.parseString(responseBody).asJsonObject
                    val code = jsonResponse.get("code")?.asInt ?: -1

                    if (code == 0) {
                        // 请求成功，code == 0
                        Handler(Looper.getMainLooper()).post {
                            callback(true)  // 通知UI线程评论成功
                        }
                    } else {
                        Log.e("CommentService", "发送评论失败: ${jsonResponse.get("message")?.asString}")
                        Handler(Looper.getMainLooper()).post {
                            callback(false)  // 通知UI线程评论失败
                        }
                    }
                } else {
                    Log.e("CommentService", "发送评论请求失败: ${response.code}")
                    Handler(Looper.getMainLooper()).post {
                        callback(false)  // 通知UI线程评论失败
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CommentService", "网络请求异常: ${e.message}")
                Handler(Looper.getMainLooper()).post {
                    callback(false)  // 通知UI线程评论失败
                }
            }
        }.start()
    }


}
