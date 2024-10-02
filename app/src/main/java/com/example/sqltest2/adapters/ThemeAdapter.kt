package com.example.sqltest2.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sqltest2.HomeFragment
import com.example.sqltest2.R
import com.example.sqltest2.api.ThemeApiService
import com.example.sqltest2.models.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class ThemeAdapter(
    private val context: Context,
    private var themes: List<Theme>,
    private val onSelect: (Theme) -> Unit
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

        holder.themeName.text = theme.themeName

        // 使用 Glide 加载图片
        Glide.with(context)
            .load(theme.themeImg)
            .into(holder.themeImage)

        // 设置“选择”按钮点击事件
        holder.selectButton.setOnClickListener {
            // 调用更新接口
            updateUserTheme(theme.themeId)

            // 传递选中的主题给调用者
            onSelect(theme)
        }
    }

    override fun getItemCount(): Int {
        return themes.size
    }

    // 更新主题的方法，调用后端接口
    private fun updateUserTheme(themeId: Int) {
        val client = OkHttpClient()
        val themeApiService = ThemeApiService(client)

        // 启动协程进行网络请求
        CoroutineScope(Dispatchers.IO).launch {
            val response = themeApiService.updateUserTheme(context, themeId)
            if (response.first == 0) {
                // 在主线程显示更新成功的消息
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "主题更新成功", Toast.LENGTH_SHORT).show()

                    // 跳转到 HomeActivity
                    val intent = Intent(context, HomeFragment::class.java)
                    context.startActivity(intent)
                }

            } else {
                // 在主线程显示更新失败的消息
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "主题更新失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 更新主题列表
    fun updateThemes(newThemes: List<Theme>) {
        this.themes = newThemes
        notifyDataSetChanged()
    }
}
