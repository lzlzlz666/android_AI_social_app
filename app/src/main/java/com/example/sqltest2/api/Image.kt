package com.example.sqltest2.api

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import java.io.IOException
import org.json.JSONObject
import android.content.Context
import com.example.sqltest2.utils.JWTStorageHelper
import android.util.Log
import com.example.sqltest2.model.User
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object ApiUploadService {

    private val client = OkHttpClient()

    private const val TAG = "ApiUploadService"

    fun uploadImage(file: File, context: Context, callback: (String?) -> Unit) {
        Log.d(TAG, "File: $file")

        val token = JWTStorageHelper.getJwtToken(context) ?: run {
            Log.e(TAG, "JWT token is null")
            callback(null)
            return
        }

        val mediaType = "image/jpeg".toMediaType() // 使用伴生对象创建 MediaType

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, RequestBody.create(mediaType, file))
            .build()

        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/upload")
            .post(requestBody)
            .addHeader("Authorization", token) // 添加 JWT 令牌到请求头
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Request failed", e)
                e.printStackTrace()  // 打印堆栈跟踪
                val message = e.message ?: "Unknown error"
                Log.e(TAG, "Error message: $message")
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    Log.d(TAG, "Response received: ${response.code}")
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Response not successful: ${response.code} - ${response.message}")
                        val errorBody = response.body?.string()
                        Log.e(TAG, "Error body: $errorBody")
                        callback(null)
                    } else {
                        val responseData = response.body?.string()
                        responseData?.let { data ->
                            Log.d(TAG, "Response data: $data")
                            try {
                                val json = JSONObject(data)
                                val url = json.getString("data")
                                callback(url)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing JSON response", e)
                                e.printStackTrace()  // 打印堆栈跟踪
                                callback(null)
                            }
                        } ?: run {
                            Log.e(TAG, "Response body is null")
                            callback(null)
                        }
                    }
                }
            }
        })
    }

    // 更新图片
    fun updateUserImage(context: Context, avatarUrl: String): Response {
        val token = JWTStorageHelper.getJwtToken(context) ?: return Response.Builder()
            .code(401)
            .message("Unauthorized")
            .build()

        val requestUrl = "${com.example.sqltest2.utils.Constants.BASE_URL}/user/updateAvatar"
            .toHttpUrlOrNull()!!
            .newBuilder()
            .addQueryParameter("avatarUrl", avatarUrl)
            .build()

        val request = Request.Builder()
            .url(requestUrl)
            .patch(RequestBody.create(null, ByteArray(0)))
            .addHeader("Authorization", token)
            .build()

        return client.newCall(request).execute()
    }
}