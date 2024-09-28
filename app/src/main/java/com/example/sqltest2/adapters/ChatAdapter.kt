package com.example.sqltest2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sqltest2.api.ApiUserService
import com.example.sqltest2.models.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatAdapter(private val chatMessages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderTextView: TextView = itemView.findViewById(R.id.senderTextView)
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        val avatarImageView: ImageView = itemView.findViewById(R.id.chatUserAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatMessage = chatMessages[position]

        // 根据 `isUser` 标志设置发送者名字
        if (chatMessage.isUser) {
            holder.senderTextView.text = "YOU"
            // 异步调用 getUserInfo 获取用户头像
            CoroutineScope(Dispatchers.IO).launch {
                val (user, errorMessage) = ApiUserService.getUserInfo(holder.itemView.context)

                withContext(Dispatchers.Main) {
                    if (user != null) {
                        // 使用 Glide 加载用户头像
                        Glide.with(holder.itemView.context)
                            .load(user.userPic)  // 加载用户头像
                            .placeholder(R.drawable.loading)  // 占位符图片
                            .error(R.drawable.error)  // 错误时显示的图片
                            .into(holder.avatarImageView)
                    } else {
                        // 如果获取用户头像失败，显示错误图片
                        holder.avatarImageView.setImageResource(R.drawable.error)
                    }
                }
            }
        } else {
            holder.senderTextView.text = "CHATGPT"
            holder.avatarImageView.setImageResource(R.drawable.gpt)
        }

        holder.messageTextView.text = chatMessage.message
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }
}
