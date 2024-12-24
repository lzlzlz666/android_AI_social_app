package com.example.sqltest2.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sqltest2.R
import com.example.sqltest2.api.ApiUserService
import com.example.sqltest2.models.ArticleItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArticleAdapter(private val articles: List<ArticleItem>, private val articleIsNull: Boolean) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val articleTitle: TextView = itemView.findViewById(R.id.articleTitleTextView)
        val articleContent: TextView = itemView.findViewById(R.id.articleContentTextView)
        val articleUserAvatar: ImageView = itemView.findViewById(R.id.articleUserAvatar)
        val articleCreateTime: TextView = itemView.findViewById(R.id.articleCreateTimeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.elem_article, parent, false)

        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        // 如果没有文章数据，直接显示 "No Article"
        if (articles.isEmpty()) {
            holder.itemView.findViewById<ImageView>(R.id.NoArticle).visibility = View.VISIBLE
            return
        }

        val article = articles[position]

        holder.articleTitle.text = article.title
        holder.articleContent.text = article.content
        holder.articleCreateTime.text = article.createTime

        // 设置用户头像
        holder.articleUserAvatar.setImageResource(article.avatarResId)

        // 异步调用 getUserInfo 获取用户头像
        CoroutineScope(Dispatchers.IO).launch {
            val (user, errorMessage) = ApiUserService.getUserInfo(holder.itemView.context)

            withContext(Dispatchers.Main) {
                if (user != null) {
                    Glide.with(holder.itemView.context)
                        .load(user.userPic)
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.error)
                        .into(holder.articleUserAvatar)
                } else {
                    holder.articleUserAvatar.setImageResource(R.drawable.error)
                }
            }
        }

        // 显示最后一个条目的 "no_article" 图片
        if (position == articles.size - 1) {
            holder.itemView.findViewById<ImageView>(R.id.NoArticle).visibility = View.VISIBLE
        } else {
            holder.itemView.findViewById<ImageView>(R.id.NoArticle).visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return articles.size
    }
}
