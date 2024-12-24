package com.example.sqltest2.adapters

import android.content.Intent
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sqltest2.DetailHallActivity
import com.example.sqltest2.R
import com.example.sqltest2.models.Item
import com.example.sqltest2.databinding.ItemLayoutBinding
import kotlin.random.Random

// 回调
interface OnItemLikedListener {
    fun onItemLiked(itemId: Int)
}

class ItemAdapter(private val itemList: List<Item>,  private val listener: OnItemLikedListener) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item) {
            binding.textViewTitle.text = item.title
            binding.textViewUser.text = item.createUserName

            Glide.with(binding.imageView.context)
                .load(item.img)
                .into(binding.imageView)
            Glide.with(binding.imageCreateUser.context)
                .load(item.createUserImg)
                .into(binding.imageCreateUser)

            // 随机高度设置
//            val randomHeight = Random.nextInt(230, 271) // 随机生成200到300之间的值
            val randomHeight = 250
            val layoutParams = binding.imageView.layoutParams
            layoutParams.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, randomHeight.toFloat(), binding.imageView.context.resources.displayMetrics).toInt()
            binding.imageView.layoutParams = layoutParams

            // 设置喜欢图标颜色
            updateLikeIcon(item)
            binding.likeCounts.text = item.likeCount.toString()

            // 点击事件
            binding.imageViewLike.setOnClickListener {
                item.isLiked = !item.isLiked // 切换状态
                updateLikeIcon(item) // 更新图标颜色
                listener.onItemLiked(item.id)
            }

            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, DetailHallActivity::class.java).apply {
                    putExtra("id",item.id)
                    putExtra("title", item.title)
                    putExtra("content", item.content)
                    putExtra("img", item.img)
                    putExtra("createUserImg",item.createUserImg)
                    putExtra("createUserName",item.createUserName)
                    putExtra("createTime",item.createTime)
                    putExtra("userId",item.userId)
                }
                context.startActivity(intent)
            }
        }

        private fun updateLikeIcon(item: Item) {
            val colorResId = if (item.isLiked) R.color.red else R.color.graygray // 根据状态设置颜色
            binding.imageViewLike.setColorFilter(binding.imageViewLike.context.getColor(colorResId))

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}

