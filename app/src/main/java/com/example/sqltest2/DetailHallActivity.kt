package com.example.sqltest2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.sqltest2.adapters.DetailAdapter
import com.example.sqltest2.databinding.ActivityDetailHallBinding
import com.example.sqltest2.models.DetailItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailHallActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailHallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailHallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取传递的数据
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val img = intent.getStringExtra("img")
        val createUserImg = intent.getStringExtra("createUserImg")
        val createUserName = intent.getStringExtra("createUserName")
        val createTime = intent.getStringExtra("createTime")

        // 显示数据
//        binding.textViewTitle.text = title
//        binding.textViewContent.text = content
//        binding.username.text = createUserName
//        binding.dataTime.text = createTime?.let { formatDateTime(it) } ?: "未知时间" // 处理null值
//        Glide.with(this).load(img).into(binding.imageView)
        Glide.with(this).load(createUserImg).into(binding.imageUserView)

        // 获取传递的数据
        val details = listOf(
            DetailItem(
                title = intent.getStringExtra("title") ?: "无标题",
                content = intent.getStringExtra("content") ?: "无内容",
                img = intent.getStringExtra("img") ?: "",
                createUserImg = intent.getStringExtra("createUserImg") ?: "",
                createTime = formatDateTime(intent.getStringExtra("createTime") ?: "未知时间")
            )
            // 你可以在这里添加更多的 DetailItem
        )

        val adapter = DetailAdapter(details)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun formatDateTime(dateTime: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
        return try {
            val date: Date = inputFormat.parse(dateTime) ?: return dateTime
            outputFormat.format(date)
        } catch (e: Exception) {
            dateTime // 如果解析失败，返回原始字符串
        }
    }
}

