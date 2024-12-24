package com.example.sqltest2.api

import android.content.Context
import android.util.Log
import com.example.sqltest2.model.Password
import com.example.sqltest2.model.User
import com.example.sqltest2.utils.JWTStorageHelper
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import com.google.gson.Gson
import okhttp3.MultipartBody


object ApiUserService {

    private val client = OkHttpClient()
    private val gson = Gson()

    // 封装用户注册的API调用
    fun registerUser(user: User): Response {
        val json = """
            {
              "username": "${user.username}",
              "password": "${user.password}"
            }
        """.trimIndent()

        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, json)

        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/user/register")
            .post(requestBody)
            .build()

        return client.newCall(request).execute()
    }

    // 封装用户登录的API调用，使用 POST 和 URL 参数
    fun loginUser(user: User): Response {
        // 使用 toHttpUrlOrNull 扩展函数构建URL并附加查询参数
        val url = "${com.example.sqltest2.utils.Constants.BASE_URL}/user/login"
            .toHttpUrlOrNull()!!
            .newBuilder()
            .addQueryParameter("username", user.username)
            .addQueryParameter("password", user.password)
            .build()

        // 创建 POST 请求
        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create(null, ByteArray(0)))  // 空的 POST 请求体
            .build()

        return client.newCall(request).execute()
    }

    // 获取用户信息，自动从 JWTStorageHelper 获取 token
    fun getUserInfo(context: Context): Pair<User?, String?> {
        val token = JWTStorageHelper.getJwtToken(context)

        if (token == null) {
            Log.e("ApiUserService", "JWT 令牌不存在")
            return Pair(null, "用户未登录或 JWT 令牌不存在")
        }

        Log.d("ApiUserService", "JWT 令牌: Bearer $token")

        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/user/userInfo")
            .addHeader("Authorization",token)  // 自动从 JWTStorageHelper 获取 token
            .get()
            .build()

        return try {
            val response: Response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e("ApiUserService", "Failed to fetch user info: ${response.code}")
                return Pair(null, "请求失败，请稍后重试。")
            }

            val responseData = response.body?.string() ?: return Pair(null, "服务器响应为空")
            val jsonResponse = JSONObject(responseData)

            // 检查返回的 code 字段
            val code = jsonResponse.getInt("code")
            if (code == 0) {
                // 成功获取用户数据
                val userJson = jsonResponse.getJSONObject("data")
                val user = gson.fromJson(userJson.toString(), User::class.java)
                print("test " + user)

                return Pair(user, null)
            } else {
                val message = jsonResponse.getString("message")
                Log.e("ApiUserService", "Operation failed: $message")
                return Pair(null, message)
            }

        } catch (e: IOException) {
            Log.e("ApiUserService", "Exception during fetching user info", e)
            return Pair(null, "网络错误，请检查网络连接。")
        }
    }

    // 修改用户信息的API调用
// 在 ApiUserService 中添加这个方法
    // 更新用户信息
    fun updateUserInfo(context: Context, user: User): Response {
        val json = """
        {
            "id": ${user.id},
            "username": "${user.username}",
            "nickname": "${user.nickname}",
            "email": "${user.email}"
        }
    """.trimIndent()

        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, json)

        // 从 JWTStorageHelper 获取 JWT token
        val token = JWTStorageHelper.getJwtToken(context) ?: throw IllegalArgumentException("JWT token cannot be null")

        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/user/update")
            .put(requestBody)
            .addHeader("Authorization", token)  // 使用从 JWTStorageHelper 获取的 token
            .build()

        return client.newCall(request).execute()
    }


    // 封装更新密码的API调用
    fun updatePassword(context: Context, password: Password): Pair<Boolean, String?> {
        val token = JWTStorageHelper.getJwtToken(context)

        if (token == null) {
            Log.e("ApiUserService", "JWT 令牌不存在")
            return Pair(false, "用户未登录或 JWT 令牌不存在")
        }

        val json = gson.toJson(password)
        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, json)

        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/user/updatePwd")
            .patch(requestBody)
            .addHeader("Authorization", token)
            .build()

        return try {
            val response: Response = client.newCall(request).execute()

            val responseData = response.body?.string() ?: return Pair(false, "服务器响应为空")
            val jsonResponse = JSONObject(responseData)

            val code = jsonResponse.getInt("code")
            val message = jsonResponse.getString("message")

            if (code == 0) {
                Pair(true, message)
            } else {
                Pair(false, message)
            }
        } catch (e: IOException) {
            Log.e("ApiUserService", "更新密码时发生异常", e)
            Pair(false, "网络错误，请检查网络连接。")
        }
    }


}
