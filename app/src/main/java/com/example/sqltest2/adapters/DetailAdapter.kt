package com.example.sqltest2.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sqltest2.databinding.ItemDetailBinding
import com.example.sqltest2.models.DetailItem

class DetailAdapter(private val details: List<DetailItem>) : RecyclerView.Adapter<DetailAdapter.DetailViewHolder>() {

    class DetailViewHolder(val binding: ItemDetailBinding) : RecyclerView.ViewHolder(binding.root)

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
//        Glide.with(holder.itemView.context).load(detail.createUserImg).into(holder.binding.imageUserView)
    }

    override fun getItemCount() = details.size
}
