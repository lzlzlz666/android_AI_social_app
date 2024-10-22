package com.example.sqltest2.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sqltest2.R
import com.example.sqltest2.databinding.ItemMyLayoutBinding
import com.example.sqltest2.models.Item

class ItemMyAdapter(private val itemMyList: List<Item>) : RecyclerView.Adapter<ItemMyAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemMyLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item) {
            binding.textViewTitle.text = item.title
            binding.textViewUser.text = item.createUserName // 确保这个字段存在

            Glide.with(binding.imageView.context)
                .load(item.img)
                .into(binding.imageView)

            Glide.with(binding.imageCreateUser.context)
                .load(item.createUserImg)
                .into(binding.imageCreateUser)

            // 设置喜欢图标颜色
            updateLikeIcon(item)
            binding.likeCounts.text = item.likeCount.toString()

            // 点击事件
            binding.imageViewLike.setOnClickListener {
                item.isLiked = !item.isLiked // 切换状态
                updateLikeIcon(item) // 更新图标颜色
            }
        }

        private fun updateLikeIcon(item: Item) {
            val colorResId = if (item.isLiked) R.color.red else R.color.black // 根据状态设置颜色
            binding.imageViewLike.setColorFilter(binding.imageViewLike.context.getColor(colorResId))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMyLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemMyList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemMyList.size
    }
}

