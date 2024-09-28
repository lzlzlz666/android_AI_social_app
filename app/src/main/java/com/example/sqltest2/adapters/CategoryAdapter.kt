package com.example.sqltest2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sqltest2.R
import com.example.sqltest2.models.CategoryItem

class CategoryAdapter(
    private val categories: List<CategoryItem>,
    private val onDelete: (Int) -> Unit, // 删除操作的回调
    private val onUpdate: (CategoryItem) -> Unit, // 修改操作的回调，传递分类对象
    private val onCategoryClick: (CategoryItem) -> Unit // 点击回调
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    var deleteMode = false // 控制是否显示删除按钮
    var updateMode = false // 控制是否显示修改按钮

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryImage: ImageView = itemView.findViewById(R.id.categoryImage)
        val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        val categoryAlias: TextView = itemView.findViewById(R.id.categoryAlias)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
        val updateButton: ImageView = itemView.findViewById(R.id.updateButton) // 修改按钮
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categoryItem = categories[position]
        holder.categoryName.text = categoryItem.categoryName
        holder.categoryAlias.text = categoryItem.categoryAlias
        holder.categoryImage.setImageResource(categoryItem.imageResId)

        // 控制删除按钮的可见性
        holder.deleteButton.visibility = if (deleteMode) View.VISIBLE else View.GONE
        // 控制修改按钮的可见性
        holder.updateButton.visibility = if (updateMode) View.VISIBLE else View.GONE

        // 设置点击删除按钮的监听器
        holder.deleteButton.setOnClickListener {
            onDelete(categoryItem.id) // 传递分类的 id，执行删除操作
        }

        // 设置点击修改按钮的监听器
        holder.updateButton.setOnClickListener {
            onUpdate(categoryItem) // 传递分类对象，执行修改操作
        }

        // 设置点击事件
        holder.itemView.setOnClickListener {
            onCategoryClick(categoryItem) // 传递点击的分类项
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    fun toggleDeleteMode() {
        deleteMode = !deleteMode
        notifyDataSetChanged()
    }

    fun toggleUpdateMode() {
        updateMode = !updateMode
        notifyDataSetChanged()
    }
}



