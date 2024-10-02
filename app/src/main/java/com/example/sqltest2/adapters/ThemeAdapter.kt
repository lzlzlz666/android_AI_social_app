package com.example.sqltest2.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sqltest2.R
import com.example.sqltest2.models.Theme

class ThemeAdapter(
    private val context: Context,
    private var themeList: List<Theme>,
    private val onThemeSelected: (Theme) -> Unit
) : RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder>() {

    class ThemeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val themeImage: ImageView = view.findViewById(R.id.themeImage)
        val themeName: TextView = view.findViewById(R.id.themeName)
        val selectThemeButton: Button = view.findViewById(R.id.selectThemeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_theme_card, parent, false)
        return ThemeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        val theme = themeList[position]
        holder.themeName.text = theme.themeName

        // 使用 Glide 加载图片
        Glide.with(context)
            .load(theme.themeImg)
            .placeholder(R.drawable.loading) // 占位符
            .into(holder.themeImage)

        // 设置点击事件
        holder.selectThemeButton.setOnClickListener {
            onThemeSelected(theme)
        }
    }

    override fun getItemCount(): Int {
        return themeList.size
    }

    // 更新数据的方法
    fun updateThemes(newThemes: List<Theme>) {
        themeList = newThemes
        notifyDataSetChanged()
    }
}
