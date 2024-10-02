package com.example.sqltest2.api

import android.content.Context
import android.util.Log
import com.example.sqltest2.models.Theme
import com.example.sqltest2.utils.JWTStorageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ThemeApiService(private val client: OkHttpClient = OkHttpClient()) {

    // 使用 suspend 函数来处理协程中的网络请求
    suspend fun getThemes(context: Context): Pair<List<Theme>?, String?> {
        return withContext(Dispatchers.IO) {  // 在 IO 线程中执行
            val token = JWTStorageHelper.getJwtToken(context)

            if (token == null) {
                Log.e("ThemeApiService", "JWT 令牌不存在")
                return@withContext Pair(null, "用户未登录或 JWT 令牌不存在")
            }

            Log.d("ThemeApiService", "JWT 令牌: Bearer $token")

            val request = Request.Builder()
                .url("${com.example.sqltest2.utils.Constants.BASE_URL}/theme")
                .addHeader("Authorization", token)  // 自动从 JWTStorageHelper 获取 token
                .get()
                .build()

            return@withContext try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e("ThemeApiService", "Failed to fetch theme info: ${response.code}")
                    Pair(null, "请求失败，请稍后重试。")
                } else {
                    val responseData = response.body?.string() ?: return@withContext Pair(null, "服务器响应为空")
                    val jsonResponse = JSONObject(responseData)

                    // 检查返回的 code 字段
                    val code = jsonResponse.getInt("code")
                    if (code == 0) {
                        // 成功获取主题数据
                        val themeList = mutableListOf<Theme>()
                        val dataArray: JSONArray = jsonResponse.getJSONArray("data")

                        for (i in 0 until dataArray.length()) {
                            val themeJson = dataArray.getJSONObject(i)
                            val theme = Theme(
                                themeId = themeJson.getInt("themeId"),
                                themeName = themeJson.getString("themeName"),
                                themeImg = themeJson.getString("themeImg")
                            )
                            themeList.add(theme)
                        }
                        Pair(themeList, null)
                    } else {
                        val message = jsonResponse.getString("message")
                        Log.e("ThemeApiService", "Operation failed: $message")
                        Pair(null, message)
                    }
                }
            } catch (e: IOException) {
                Log.e("ThemeApiService", "Exception during fetching theme info", e)
                Pair(null, "网络错误，请检查网络连接。")
            }
        }
    }
}
