package com.example.sqltest2

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sqltest2.adapters.ArticleAdapter
import com.example.sqltest2.models.ArticleItem
import com.example.sqltest2.utils.JWTStorageHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class CategoryDetailActivity : AppCompatActivity() {

    private val client = OkHttpClient()  // OkHttpClient 实例


    private lateinit var articleRecyclerView: RecyclerView
    private lateinit var articleAdapter: ArticleAdapter
    private val articles = ArrayList<ArticleItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_detail)

        // 获取传递过来的数据
        val categoryId = intent.getIntExtra("categoryId", -1)
        val categoryName = intent.getStringExtra("categoryName")
        val categoryAlias = intent.getStringExtra("categoryAlias")

        // 初始化TextView并显示数据
        val categoryNameTextView = findViewById<TextView>(R.id.categoryNameTextView)
        val categoryAliasTextView = findViewById<TextView>(R.id.categoryAliasTextView)

        categoryNameTextView.text = "分类名称: $categoryName"
        categoryAliasTextView.text = "分类别名: $categoryAlias"

        // 初始化 RecyclerView
        articleRecyclerView = findViewById(R.id.articleRecyclerView)
        articleRecyclerView.layoutManager = LinearLayoutManager(this)

        // 创建适配器并设置给 RecyclerView
        articleAdapter = ArticleAdapter(articles)
        articleRecyclerView.adapter = articleAdapter

        // 加载文章列表
        loadArticlesForCategory(categoryId)
    }

//    private fun loadArticlesForCategory(categoryId: Int) {
//        // 这里你可以调用后端 API，传递 categoryId 来获取文章数据
//        // 假设从服务器获取到的文章数据如下：
//
//        // 模拟文章数据
//        val mockArticles = listOf(
//            ArticleItem("文章标题1", "文章内容1", 123, "2024-01-01 12:00", R.drawable.wrx),
//            ArticleItem("文章标题2", "文章内容2", 456, "2024-01-02 14:00", R.drawable.wrx),
//            ArticleItem("文章标题3", "文章内容3", 789, "2024-01-03 16:00", R.drawable.wrx)
//        )
//
//        // 更新文章列表并通知适配器
//        articles.clear()
//        articles.addAll(mockArticles)
//        articleAdapter.notifyDataSetChanged()
//    }

    private fun loadArticlesForCategory(categoryId: Int) {
        // 获取 JWT 令牌
        val token = JWTStorageHelper.getJwtToken(this)
        if (token == null) {
            Toast.makeText(this, "用户未登录，无法获取文章列表", Toast.LENGTH_SHORT).show()
            return
        }

        // 异步请求后端 API 获取文章数据
        CoroutineScope(Dispatchers.IO).launch {
            val request = Request.Builder()
                .url("${com.example.sqltest2.utils.Constants.BASE_URL}/article?pageNum=1&pageSize=5&categoryId=$categoryId&state=草稿")
                .addHeader("Authorization", token)  // 添加 JWT 令牌到请求头
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        val jsonResponse = JSONObject(responseData)
                        val articlesJsonArray: JSONArray = jsonResponse.getJSONObject("data").getJSONArray("items")

                        // 解析 JSON 数据
                        val newArticles = ArrayList<ArticleItem>()
                        for (i in 0 until articlesJsonArray.length()) {
                            val articleObject = articlesJsonArray.getJSONObject(i)
                            val id = articleObject.getInt("id")
                            val title = articleObject.getString("title")
                            val content = articleObject.getString("content")
                            val createUser = articleObject.getInt("createUser")
                            val createTime = articleObject.getString("createTime")
//                            val coverImg = articleObject.getString("coverImg")  // 获取封面图片链接

                            // 构建 ArticleItem(R.drawable.wrx 后续在ArticleAdapter.kt 里面进行动态更改)
                            val articleItem = ArticleItem(title, content, createUser, createTime, R.drawable.wrx)
                            newArticles.add(articleItem)
                        }

                        withContext(Dispatchers.Main) {
                            // 更新文章列表并通知适配器
                            articles.clear()
                            articles.addAll(newArticles)
                            articleAdapter.notifyDataSetChanged()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CategoryDetailActivity, "请求失败，代码：" + response.code, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CategoryDetailActivity, "网络错误，请检查网络连接", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
