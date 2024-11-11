package com.example.sqltest2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.sqltest2.adapters.DetailAdapter
import com.example.sqltest2.api.CommentService
import com.example.sqltest2.databinding.ActivityDetailHallBinding
import com.example.sqltest2.model.Comment
import com.example.sqltest2.models.DetailItem
import com.google.gson.Gson
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
        val articleId = intent.getIntExtra("id", -1)

        // 获取评论数据
        val commentService = CommentService(this)
        commentService.fetchComments(articleId) { comments ->
            // 显示数据
            binding.username.text = createUserName
            Glide.with(this).load(createUserImg).into(binding.imageUserView)

            // 设置 RecyclerView
            val details = listOf(
                DetailItem(
                    articleId = articleId,
                    title = title ?: "无标题",
                    content = content ?: "无内容",
                    img = img ?: "",
                    createUserImg = createUserImg ?: "",
                    createTime = formatDateTime(createTime ?: "未知时间"),
                    comment = comments ?: emptyList()
                )
            )
            val adapter = DetailAdapter(details)
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
            binding.recyclerView.adapter = adapter
        }

        // 发送按钮分享事件
        binding.send.setOnClickListener {
            shareContent(title, content)
        }


        // 提交评论
        binding.sendButton.setOnClickListener {
            val commentContent = binding.commentSubmit.text.toString().trim()
            if (commentContent.isNotEmpty()) {
                // 发送评论
                commentService.postComment(commentContent, articleId, null) { success ->
                    if (success) {
                        runOnUiThread {
                            binding.commentSubmit.setText("")  // 清空输入框
                            Toast.makeText(this, "孩子，你评论成功了", Toast.LENGTH_SHORT).show()
                        }

                        // 评论提交成功后刷新评论数据
                        commentService.fetchComments(articleId) { updatedComments ->
                            // 更新评论列表
                            val details = listOf(
                                DetailItem(
                                    articleId = articleId,
                                    title = title ?: "无标题",
                                    content = content ?: "无内容",
                                    img = img ?: "",
                                    createUserImg = createUserImg ?: "",
                                    createTime = formatDateTime(createTime ?: "未知时间"),
                                    comment = updatedComments ?: emptyList()
                                )
                            )
                            val adapter = DetailAdapter(details)
                            binding.recyclerView.layoutManager = LinearLayoutManager(this)
                            binding.recyclerView.adapter = adapter
                        }
                    } else {
                        // 在主线程中显示 Toast
                        runOnUiThread {
                            Toast.makeText(this, "评论提交失败，请稍后再试", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                // 在主线程中显示 Toast
                runOnUiThread {
                    Toast.makeText(this, "评论内容不能为空", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun shareContent(title: String?, content: String?) {
        val shareText = "$title\n$content" // 创建分享的文本内容
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "分享至"))
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

