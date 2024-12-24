package com.example.sqltest2.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sqltest2.databinding.ItemDetailBinding
import com.example.sqltest2.models.DetailItem

class DetailAdapter(private val details: List<DetailItem>) : RecyclerView.Adapter<DetailAdapter.DetailViewHolder>() {

    class DetailViewHolder(val binding: ItemDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        // 这里添加评论 RecyclerView 的引用
        val commentAdapter: CommentAdapter = CommentAdapter()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val binding = ItemDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val detail = details[position]
        holder.binding.textViewTitle.text = detail.title
        holder.binding.textViewContent.text = detail.content
        holder.binding.dataTime.text = "编辑于 ${detail.createTime}"
        Glide.with(holder.itemView.context).load(detail.img).into(holder.binding.imageView)

        // 判断评论是否为空，显示图片
        if (detail.comment.isEmpty()) {
            holder.binding.NoComment.visibility = View.VISIBLE     // 无评论图标显示
        }

        // 更新评论列表
        holder.commentAdapter.setComments(detail.comment,detail.articleId,detail.userId)
        holder.binding.recyclerViewComments.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.binding.recyclerViewComments.adapter = holder.commentAdapter
    }

    override fun getItemCount() = details.size



}

