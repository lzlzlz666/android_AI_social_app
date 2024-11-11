package com.example.sqltest2.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sqltest2.databinding.ItemCommentBinding
import com.example.sqltest2.model.Comment
import android.app.Dialog
import android.view.WindowManager
import com.example.sqltest2.R
import com.example.sqltest2.api.CommentService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentAdapter : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private var comments: List<Comment> = emptyList()
    private var articleId: Int = -1                      // 该评论对应的文章编号

    class CommentViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)

    fun setComments(comments: List<Comment>, articleId: Int) {
        this.comments = comments
        this.articleId = articleId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        val parentCommentId = comment.id

        holder.binding.textViewUsername.text = comment.username
        holder.binding.textViewContent.text = comment.content
        holder.binding.textViewCreateTime.text = formatDateTime(comment.createTime)

        // 加载用户头像
        Glide.with(holder.itemView.context).load(comment.userImg).into(holder.binding.imageViewUser)

        // 判断是否是父评论，如果是，则显示回复按钮，否则隐藏
        if (comment.parentCommentId == null) {
            holder.binding.textViewReply.visibility = View.VISIBLE  // 显示回复按钮
            // 设置父评论头像大小为 30dp
            holder.binding.imageViewUser.layoutParams.width = holder.itemView.context.resources.getDimensionPixelSize(R.dimen.avatar_size_large)
            holder.binding.imageViewUser.layoutParams.height = holder.itemView.context.resources.getDimensionPixelSize(R.dimen.avatar_size_large)
        } else {
            holder.binding.textViewReply.visibility = View.GONE  // 隐藏回复按钮
            // 设置子评论头像大小为 20dp
            holder.binding.imageViewUser.layoutParams.width = holder.itemView.context.resources.getDimensionPixelSize(R.dimen.avatar_size_small)
            holder.binding.imageViewUser.layoutParams.height = holder.itemView.context.resources.getDimensionPixelSize(R.dimen.avatar_size_small)
        }

        // 处理子评论
        if (comment.replies.isNotEmpty()) {
            val replyAdapter = CommentAdapter() // 为子评论创建新的适配器
            replyAdapter.setComments(comment.replies, articleId)
            holder.binding.recyclerViewReplies.layoutManager = LinearLayoutManager(holder.itemView.context)
            holder.binding.recyclerViewReplies.adapter = replyAdapter
        }

        // 设置“回复”按钮点击事件
        holder.binding.textViewReply.setOnClickListener {
            showReplyDialog(holder.itemView.context, parentCommentId)
        }
    }

    override fun getItemCount() = comments.size

    // 显示回复弹窗
    private fun showReplyDialog(context: Context, parentCommentId: Int?) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_reply)

        // 设置弹窗宽高为全屏，适应不同屏幕尺寸
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        // 设置弹窗背景为默认的（避免闪烁效果）
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 设置背景变灰
        dialog.window?.setDimAmount(0.7f)

        val editTextComment = dialog.findViewById<EditText>(R.id.editTextComment)
        val btnSend = dialog.findViewById<View>(R.id.btnSend)

        // 发送按钮事件
        btnSend.setOnClickListener {
            val commentText = editTextComment.text.toString()
            if (commentText.isNotEmpty()) {
                // 提交评论，传递 articleId 和 parentCommentId
                val commentService = CommentService(context)
                commentService.postComment(commentText, articleId, parentCommentId) { success ->
                    if (success) {
                        Toast.makeText(context, "评论已发送", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()

                        // 这里可以刷新评论列表，重新请求评论数据
                        refreshComments(context)
                    } else {
                        Toast.makeText(context, "评论发送失败，请稍后再试", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "请输入评论内容", Toast.LENGTH_SHORT).show()
            }
        }

        // 设置点击其他区域关闭弹窗（去掉闪烁效果）
        dialog.setCanceledOnTouchOutside(true)

        dialog.show()
    }

    // 刷新评论数据
    private fun refreshComments(context: Context) {
        // 请求新的评论数据并更新 RecyclerView
        val commentService = CommentService(context)
        commentService.fetchComments(articleId) { updatedComments ->
            // 使用空合并运算符确保评论列表非空
            comments = updatedComments ?: emptyList()  // 确保 comments 不是 null
            notifyDataSetChanged()
        }
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



