package com.example.sqltest2.api

import android.content.Context
import android.util.Log
import com.example.sqltest2.R
import com.example.sqltest2.model.Category
import com.example.sqltest2.models.CategoryItem
import com.example.sqltest2.utils.JWTStorageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

// ThemeApiService 导入
import com.example.sqltest2.api.ThemeApiService
import com.example.sqltest2.models.ArticleItem
import com.example.sqltest2.models.Item
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody


object ApiCategoryService {

    private val client = OkHttpClient()

    // 全局变量来保存从 getCategories 函数中获取的 userThemeName
    private var userThemeName: String? = null

    // 获取用户的主题 ID
    private suspend fun getUserTheme(context: Context): Int? {
        return withContext(Dispatchers.IO) {
            val token = JWTStorageHelper.getJwtToken(context)

            if (token == null) {
                Log.e("ApiCategoryService", "JWT 令牌不存在")
                return@withContext null
            }

            val request = Request.Builder()
                .url("${com.example.sqltest2.utils.Constants.BASE_URL}/theme/userTheme")
                .addHeader("Authorization", token)
                .get()
                .build()

            return@withContext try {
                val response: Response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e("ApiCategoryService", "Failed to fetch user theme: ${response.code}")
                    return@withContext null
                }

                val responseData = response.body?.string() ?: return@withContext null
                val jsonResponse = JSONObject(responseData)
                val code = jsonResponse.getInt("code")

                if (code == 0) {
                    val data = jsonResponse.getJSONObject("data")
                    return@withContext data.getInt("userTheme")
                } else {
                    Log.e("ApiCategoryService", "Failed to get user theme: ${jsonResponse.getString("message")}")
                    return@withContext null
                }
            } catch (e: IOException) {
                Log.e("ApiCategoryService", "Exception during fetching user theme", e)
                return@withContext null
            }
        }
    }

    // 根据用户的主题加载分类数据并动态调整图片
    suspend fun getCategories(context: Context): Pair<List<CategoryItem>?, String?> {
        val token = JWTStorageHelper.getJwtToken(context)

        if (token == null) {
            Log.e("ApiCategoryService", "JWT 令牌不存在")
            return Pair(null, "用户未登录或 JWT 令牌不存在")
        }

        // 获取用户的主题 ID
        val userThemeId = getUserTheme(context) ?: return Pair(null, "无法获取用户主题")

        // 调用封装好的 `ThemeApiService.getThemes()` 来获取所有主题信息
        val themes = ThemeApiService().getThemes(context).first ?: return Pair(null, "无法获取主题列表")

        // 根据 userThemeId 找到对应的 themeName
        val userTheme = themes.find { it.themeId == userThemeId } ?: return Pair(null, "无法匹配用户主题")

        // 保存主题名称到全局变量
        userThemeName = userTheme.themeName

        // 构建分类数据请求
        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/category")
            .addHeader("Authorization", token)
            .get()
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response: Response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e("ApiService", "Request failed with code: ${response.code}")
                    return@withContext Pair(null, "请求失败，请稍后重试。")
                }

                val responseData = response.body?.string() ?: ""
                val jsonResponse = JSONObject(responseData)

                val code = jsonResponse.getInt("code")
                if (code == 0) {
                    val jsonArray: JSONArray = jsonResponse.getJSONArray("data")
                    val categoryList = ArrayList<CategoryItem>()

                    // 根据 themeName 动态加载图片资源
                    val imageResources = if (userTheme.themeName == "ordinary") {
                        // 如果是 "ordinary"，只放一张固定的图片
                        listOf(R.drawable.wenjianjia)
                    } else {
                        // 动态生成 10 张图片的资源 ID，格式为 themeName_1 到 themeName_10
                        val resourceList = mutableListOf<Int>()
                        for (i in 1..10) {
                            val resourceId = context.resources.getIdentifier(
                                "${userTheme.themeName}_$i",
                                "drawable",
                                context.packageName
                            )
                            if (resourceId != 0) {
                                resourceList.add(resourceId)
                            } else {
                                Log.e("ApiCategoryService", "无法找到资源: ${userTheme.themeName}_$i")
                            }
                        }
                        resourceList
                    }

                    for (i in 0 until jsonArray.length()) {
                        val categoryObject = jsonArray.getJSONObject(i)
                        val id = categoryObject.getInt("id")
                        val categoryName = categoryObject.getString("categoryName")
                        val categoryAlias = categoryObject.getString("categoryAlias")

                        val imageIndex = i % imageResources.size
                        val assignedImage = imageResources[imageIndex]

                        categoryList.add(CategoryItem(id, assignedImage, categoryName, categoryAlias))
                    }

                    return@withContext Pair(categoryList, null)
                } else {
                    return@withContext Pair(null, jsonResponse.getString("message"))
                }

            } catch (e: IOException) {
                Log.e("ApiService", "Exception during API request", e)
                return@withContext Pair(null, "网络错误，请检查网络连接。")
            }
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

    // 根据关键词搜索分类，并动态渲染图片
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

                // 根据全局变量 `userThemeName` 动态渲染图片资源
                val imageResources = if (userThemeName == "ordinary") {
                    // 如果是 "ordinary"，只放一张固定的图片
                    listOf(R.drawable.wenjianjia)
                } else {
                    // 动态生成 10 张图片的资源 ID，格式为 themeName_1 到 themeName_10
                    val resourceList = mutableListOf<Int>()
                    for (i in 1..10) {
                        val resourceId = context.resources.getIdentifier(
                            "${userThemeName}_$i",
                            "drawable", // 资源类型
                            context.packageName // 包名
                        )
                        if (resourceId != 0) {
                            resourceList.add(resourceId)
                        } else {
                            Log.e("ApiCategoryService", "无法找到资源: ${userThemeName}_$i")
                        }
                    }
                    resourceList
                }

                for (i in 0 until jsonArray.length()) {
                    val categoryObject = jsonArray.getJSONObject(i)
                    val id = categoryObject.getInt("id")
                    val categoryName = categoryObject.getString("categoryName")
                    val categoryAlias = categoryObject.getString("categoryAlias")

                    val imageIndex = i % imageResources.size
                    val assignedImage = imageResources[imageIndex]

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

    // ApiCategoryService.kt
    suspend fun getArticlesForCategory(context: Context, categoryId: Int): Pair<List<ArticleItem>?, String?> {
        val token = JWTStorageHelper.getJwtToken(context)
        if (token == null) {
            Log.e("ApiCategoryService", "JWT 令牌不存在")
            return Pair(null, "用户未登录，无法获取文章列表")
        }

        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/article?pageNum=1&pageSize=100&categoryId=$categoryId&state=草稿")
            .addHeader("Authorization", token)
            .get()
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response: Response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e("ApiCategoryService", "请求失败，代码：${response.code}")
                    return@withContext Pair(null, "请求失败，请稍后重试。")
                }

                val responseData = response.body?.string()
                val jsonResponse = JSONObject(responseData)
                val articlesJsonArray: JSONArray = jsonResponse.getJSONObject("data").getJSONArray("items")

                val articleList = ArrayList<ArticleItem>()
                for (i in 0 until articlesJsonArray.length()) {
                    val articleObject = articlesJsonArray.getJSONObject(i)
                    val id = articleObject.getInt("id")
                    val title = articleObject.getString("title")
                    val content = articleObject.getString("content")
                    val createUser = articleObject.getInt("createUser")
                    val createTime = articleObject.getString("createTime")
                    articleList.add(ArticleItem(title, content, createUser, createTime, R.drawable.wrx))
                }

                Pair(articleList, null)
            } catch (e: IOException) {
                Log.e("ApiCategoryService", "网络错误", e)
                Pair(null, "网络错误，请检查网络连接")
            }
        }
    }

    suspend fun getPublishedArticles(context: Context): Pair<List<Item>?, String?> {
        val token = JWTStorageHelper.getJwtToken(context)
        if (token == null) {
            Log.e("ApiCategoryService", "JWT 令牌不存在")
            return Pair(null, "用户未登录，无法获取已发布文章")
        }

        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/article/allArticleVTO?state=已发布")
            .addHeader("Authorization", token)
            .get()
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response: Response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e("ApiCategoryService", "请求失败，代码：${response.code}")
                    return@withContext Pair(null, "请求失败，请稍后重试。")
                }

                val responseData = response.body?.string()
                val jsonResponse = JSONObject(responseData)
                val articlesJsonArray: JSONArray = jsonResponse.getJSONArray("data")

                val itemList = ArrayList<Item>()
                for (i in 0 until articlesJsonArray.length()) {
                    val articleObject = articlesJsonArray.getJSONObject(i)
                    val id = articleObject.getInt("id")
                    val title = articleObject.getString("title")
                    val content = articleObject.getString("content")
                    val img = articleObject.getString("img")
                    val createTime = articleObject.getString("createTime")
                    val createUserName = articleObject.getString("createUserName")
                    val createUserImg = articleObject.getString("createUserImg")
                    itemList.add(Item(id, title, content, img,createTime, createUserName, createUserImg))
                }

                Pair(itemList, null)
            } catch (e: IOException) {
                Log.e("ApiCategoryService", "网络错误", e)
                Pair(null, "网络错误，请检查网络连接")
            }
        }
    }

    suspend fun getPublishedMyArticles(context: Context): Pair<List<Item>?, String?> {
        val token = JWTStorageHelper.getJwtToken(context)
        if (token == null) {
            Log.e("ApiCategoryService", "JWT 令牌不存在")
            return Pair(null, "用户未登录，无法获取已发布文章")
        }

        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/article/MyArticleVTO?state=已发布")
            .addHeader("Authorization", token)
            .get()
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response: Response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e("ApiCategoryService", "请求失败，代码：${response.code}")
                    return@withContext Pair(null, "请求失败，请稍后重试。")
                }

                val responseData = response.body?.string()
                val jsonResponse = JSONObject(responseData)
                val articlesJsonArray: JSONArray = jsonResponse.getJSONArray("data")

                val itemMyList = ArrayList<Item>()
                for (i in 0 until articlesJsonArray.length()) {
                    val articleObject = articlesJsonArray.getJSONObject(i)
                    val id = articleObject.getInt("id")
                    val title = articleObject.getString("title")
                    val content = articleObject.getString("content")
                    val img = articleObject.getString("img")
                    val createTime = articleObject.getString("createTime")
                    val createUserName = articleObject.getString("createUserName")
                    val createUserImg = articleObject.getString("createUserImg")
                    itemMyList.add(Item(id, title, content, img,createTime, createUserName, createUserImg))
                }

                Pair(itemMyList, null)
            } catch (e: IOException) {
                Log.e("ApiCategoryService", "网络错误", e)
                Pair(null, "网络错误，请检查网络连接")
            }
        }
    }


    // 点赞文章
    suspend fun getLikeCount(context: Context, articleId: Int): Pair<Int?, String?> {
        val token = JWTStorageHelper.getJwtToken(context)
        if (token == null) {
            Log.e("ApiLikeService", "JWT 令牌不存在")
            return Pair(null, "用户未登录，无法获取点赞数")
        }

        val request = Request.Builder()
            .url("http://112.124.27.151:8090/article/like?articleId=$articleId")
            .addHeader("Authorization", token)
            .get()
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response: Response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e("ApiLikeService", "请求失败，代码：${response.code}")
                    return@withContext Pair(null, "请求失败，请稍后重试。")
                }

                val responseData = response.body?.string() ?: return@withContext Pair(null, "响应数据为空")
                val jsonResponse = JSONObject(responseData)
                val code = jsonResponse.getInt("code")

                if (code == 0) {
                    val likeCount = jsonResponse.getJSONObject("data").getInt("likeCount")
                    Pair(likeCount, null)
                } else {
                    Pair(null, jsonResponse.getString("message"))
                }

            } catch (e: IOException) {
                Log.e("ApiLikeService", "网络错误", e)
                Pair(null, "网络错误，请检查网络连接")
            }
        }
    }
}
