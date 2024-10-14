package com.example.sqltest2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sqltest2.adapters.ArticleAdapter
import com.example.sqltest2.api.ApiCategoryService
import com.example.sqltest2.models.ArticleItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryDetailActivity : AppCompatActivity() {

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
        val addButtonImageView = findViewById<ImageView>(R.id.addButtonImageView)

        // 点击 "+" 按钮，跳转到 CreateArticleActivity 创建文章
        addButtonImageView.setOnClickListener {
            val intent = Intent(this, CreateArticleActivity::class.java)
            intent.putExtra("categoryId", categoryId)  // 传递 categoryId
            startActivityForResult(intent, 1)  // 启动 CreateArticleActivity 并期待返回结果
        }

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

    // 加载某分类下的文章
    private fun loadArticlesForCategory(categoryId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            val (articleList, error) = ApiCategoryService.getArticlesForCategory(this@CategoryDetailActivity, categoryId)

            if (error != null) {
                Toast.makeText(this@CategoryDetailActivity, error, Toast.LENGTH_SHORT).show()
            } else {
                articles.clear()
                articles.addAll(articleList ?: emptyList())
                articleAdapter.notifyDataSetChanged()
            }
        }
    }

    // 处理 CreateArticleActivity 返回的结果
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // 如果文章保存成功，重新加载文章列表
            val categoryId = intent.getIntExtra("categoryId", -1)
            loadArticlesForCategory(categoryId)
        }
    }
}
