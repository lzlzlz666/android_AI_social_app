package com.example.sqltest2.api

import android.content.Context
import android.util.Log
import com.example.sqltest2.R
import com.example.sqltest2.model.Category
import com.example.sqltest2.models.CategoryItem
import com.example.sqltest2.utils.JWTStorageHelper
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

object ApiCategoryService {

    private val client = OkHttpClient()

    // 获取分类数据，增加错误处理并加入 JWT 令牌
    fun getCategories(context: Context): Pair<List<CategoryItem>?, String?> {
        val token = JWTStorageHelper.getJwtToken(context)

        if (token == null) {
            Log.e("ApiCategoryService", "JWT 令牌不存在")
            return Pair(null, "用户未登录或 JWT 令牌不存在")
        }

        Log.d("ApiCategoryService", "JWT 令牌: Bearer $token")

        // 构建请求并在 Authorization 头中加入 JWT 令牌
        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/category")
            .addHeader("Authorization", token)  // 添加 JWT 令牌到请求头
            .get()
            .build()

        return try {
            val response: Response = client.newCall(request).execute()

            // 检查响应码
            if (!response.isSuccessful) {
                Log.e("ApiService", "Request failed with code: ${response.code}")
                return Pair(null, "请求失败，请稍后重试。")
            }

            val responseData = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseData)

            // 检查返回的 code 字段
            val code = jsonResponse.getInt("code")
            if (code == 0) {
                val jsonArray: JSONArray = jsonResponse.getJSONArray("data")
                val categoryList = ArrayList<CategoryItem>()

                // 图片资源 ID 列表 1-10 的蜡笔小新图
                val imageResources = listOf(
                    R.drawable.labixiaoxin_1,
                    R.drawable.labixiaoxin_2,
                    R.drawable.labixiaoxin_3,
                    R.drawable.labixiaoxin_4,
                    R.drawable.labixiaoxin_5,
                    R.drawable.labixiaoxin_6,
                    R.drawable.labixiaoxin_7,
                    R.drawable.labixiaoxin_8,
                    R.drawable.labixiaoxin_9,
                    R.drawable.labixiaoxin_10
                )

                // 将图片资源列表随机打乱，避免重复
                val shuffledImages = imageResources.toMutableList()
                shuffledImages.shuffle()

                for (i in 0 until jsonArray.length()) {
                    val categoryObject = jsonArray.getJSONObject(i)
                    val id = categoryObject.getInt("id")  // 提取 id
                    val categoryName = categoryObject.getString("categoryName")
                    val categoryAlias = categoryObject.getString("categoryAlias")

                    // 分配图片，不重复，循环使用打乱的列表
                    val imageIndex = i % shuffledImages.size
                    val assignedImage = shuffledImages[imageIndex]

                    // 创建 CategoryItem，包含 id、图片、名称和别名
                    categoryList.add(CategoryItem(id, assignedImage, categoryName, categoryAlias))
                }

                // 返回成功的数据和 null 错误
                return Pair(categoryList, null)

            } else if (code == 1) {
                Log.e("ApiService", "Operation failed: ${jsonResponse.getString("message")}")
                return Pair(null, jsonResponse.getString("message"))
            } else {
                Log.e("ApiService", "Unknown code: $code")
                return Pair(null, "未知错误，请稍后重试。")
            }

        } catch (e: IOException) {
            Log.e("ApiService", "Exception during API request", e)
            return Pair(null, "网络错误，请检查网络连接。")
        }
    }



    // 新增分类
    fun addCategory(context: Context, category: Category): Pair<Boolean, String?> {
        val token = JWTStorageHelper.getJwtToken(context)

        if (token == null) {
            Log.e("ApiCategoryService", "JWT 令牌不存在")
            return Pair(false, "用户未登录或 JWT 令牌不存在")
        }

        Log.d("ApiCategoryService", "JWT 令牌: Bearer $token")

        // 构建请求体，将 Category 对象转换为 JSON
        val jsonObject = JSONObject().apply {
            put("categoryName", category.categoryName)
            put("categoryAlias", category.categoryAlias)
        }
        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        // 构建 POST 请求并在 Authorization 头中加入 JWT 令牌
        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/category")
            .addHeader("Authorization", token)  // 添加 JWT 令牌到请求头
            .post(requestBody)
            .build()

        return try {
            val response: Response = client.newCall(request).execute()

            // 检查响应码
            if (!response.isSuccessful) {
                return Pair(false, "请求失败，请稍后重试。")
            }

            val responseData = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseData)

            // 检查返回的 code 字段
            val code = jsonResponse.getInt("code")
            if (code == 0) {
                // 新增分类成功
                return Pair(true, "新增分类成功")
            } else {
                return Pair(false, jsonResponse.getString("message"))
            }
        } catch (e: IOException) {
            Log.e("ApiService", "Exception during API request", e)
            return Pair(false, "网络错误，请检查网络连接。")
        }
    }

    // 删除分类的方法
    fun deleteCategory(context: Context, id: Int): Pair<Boolean, String?> {
        val token = JWTStorageHelper.getJwtToken(context)

        if (token == null) {
            Log.e("ApiCategoryService", "JWT 令牌不存在")
            return Pair(false, "用户未登录或 JWT 令牌不存在")
        }

        Log.d("ApiCategoryService", "JWT 令牌: Bearer $token")

        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/category?id=$id")
            .addHeader("Authorization", token)
            .delete() // 发起 DELETE 请求
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Pair(true, null) // 成功
            } else {
                Pair(false, "请求失败，请稍后重试。")
            }
        } catch (e: IOException) {
            Log.e("ApiService", "Exception during API request", e)
            Pair(false, "网络错误，请检查网络连接。")
        }
    }

    // 更新分类信息的方法
    fun updateCategory(context: Context, id: Int, categoryName: String, categoryAlias: String): Pair<Boolean, String?> {
        val token = JWTStorageHelper.getJwtToken(context) ?: return Pair(false, "用户未登录")

        val jsonBody = JSONObject()
        jsonBody.put("id", id)
        jsonBody.put("categoryName", categoryName)
        jsonBody.put("categoryAlias", categoryAlias)

        val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/category")
            .addHeader("Authorization", token)
            .put(requestBody)
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Pair(true, null)
            } else {
                Pair(false, "修改失败")
            }
        } catch (e: IOException) {
            Pair(false, "网络错误")
        }
    }

    // 根据关键词搜索分类
    fun searchCategories(context: Context, query: String): Pair<List<CategoryItem>?, String?> {
        val token = JWTStorageHelper.getJwtToken(context) ?: return Pair(null, "用户未登录")

        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/category/listCategory?categoryName=$query")
            .addHeader("Authorization", token)
            .get()
            .build()

        return try {
            val response: Response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return Pair(null, "搜索请求失败，请稍后重试。")
            }

            val responseData = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseData)
            val code = jsonResponse.getInt("code")
            if (code == 0) {
                val jsonArray: JSONArray = jsonResponse.getJSONArray("data")
                val categoryList = ArrayList<CategoryItem>()

                val imageResources = listOf(
                    R.drawable.labixiaoxin_1,
                    R.drawable.labixiaoxin_2,
                    R.drawable.labixiaoxin_3,
                    R.drawable.labixiaoxin_4,
                    R.drawable.labixiaoxin_5,
                    R.drawable.labixiaoxin_6,
                    R.drawable.labixiaoxin_7,
                    R.drawable.labixiaoxin_8,
                    R.drawable.labixiaoxin_9,
                    R.drawable.labixiaoxin_10
                )

                val shuffledImages = imageResources.toMutableList()
                shuffledImages.shuffle()

                for (i in 0 until jsonArray.length()) {
                    val categoryObject = jsonArray.getJSONObject(i)
                    val id = categoryObject.getInt("id")
                    val categoryName = categoryObject.getString("categoryName")
                    val categoryAlias = categoryObject.getString("categoryAlias")
                    val imageIndex = i % shuffledImages.size
                    val assignedImage = shuffledImages[imageIndex]

                    categoryList.add(CategoryItem(id, assignedImage, categoryName, categoryAlias))
                }
                return Pair(categoryList, null)

            } else {
                return Pair(null, jsonResponse.getString("message"))
            }

        } catch (e: IOException) {
            Log.e("ApiCategoryService", "Exception during API request", e)
            return Pair(null, "网络错误，请检查网络连接。")
        }
    }
}
