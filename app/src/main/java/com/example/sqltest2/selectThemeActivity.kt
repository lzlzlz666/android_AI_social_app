package com.example.sqltest2

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sqltest2.adapters.ThemeAdapter
import com.example.sqltest2.api.ThemeApiService
import kotlinx.coroutines.launch

class selectThemeActivity : AppCompatActivity() {

    private lateinit var themeAdapter: ThemeAdapter
    private lateinit var themeApiService: ThemeApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_theme)

        // 初始化 API 服务
        themeApiService = ThemeApiService()

        // 设置 RecyclerView
        val themeRecyclerView: RecyclerView = findViewById(R.id.themeRecyclerView)
        themeRecyclerView.layoutManager = LinearLayoutManager(this)
        themeAdapter = ThemeAdapter(this, emptyList()) { selectedTheme ->
            Toast.makeText(this, "${selectedTheme.themeName} selected", Toast.LENGTH_SHORT).show()
        }
        themeRecyclerView.adapter = themeAdapter

        // 获取主题数据并更新适配器
        fetchThemes()
    }

    private fun fetchThemes() {
        // 使用协程启动网络请求
        lifecycleScope.launch {
            val (themes, errorMessage) = themeApiService.getThemes(this@selectThemeActivity)

            if (themes != null) {
                themeAdapter.updateThemes(themes)
            } else {
                Toast.makeText(this@selectThemeActivity, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
