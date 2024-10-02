// ThemeAdapter.kt
package com.example.sqltest2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sqltest2.models.Theme

class ThemeAdapter(
    private val context: Context,
    private val themes: List<Theme>,
    private val onThemeSelected: (Theme) -> Unit  // 添加主题选择回调
) : RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder>() {

    class ThemeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val themeImage: ImageView = itemView.findViewById(R.id.themeImage)
        val themeName: TextView = itemView.findViewById(R.id.themeName)
        val selectButton: Button = itemView.findViewById(R.id.selectThemeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_theme_card, parent, false)
        return ThemeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        val theme = themes[position]

        // 设置主题名称和图片
        holder.themeName.text = theme.name
        holder.themeImage.setImageResource(theme.imageResId)

        // 设置按钮点击事件
        holder.selectButton.setOnClickListener {
            onThemeSelected(theme)
        }
    }

    override fun getItemCount(): Int {
        return themes.size
    }
}
