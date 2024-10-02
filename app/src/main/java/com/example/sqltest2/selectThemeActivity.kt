package com.example.sqltest2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sqltest2.models.Theme

class selectThemeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_select_theme)

        val themeList = listOf(
            Theme("Theme 1", R.drawable.gpt2),
            Theme("Theme 2", R.drawable.wrx),
            Theme("Theme 3", R.drawable.gpt)
        )

        val themeRecyclerView: RecyclerView = findViewById(R.id.themeRecyclerView)
        themeRecyclerView.layoutManager = LinearLayoutManager(this)
        themeRecyclerView.adapter = ThemeAdapter(this, themeList) { selectedTheme ->
            // 主题选择逻辑
            Toast.makeText(this, "${selectedTheme.name} selected", Toast.LENGTH_SHORT).show()
        }
    }
}