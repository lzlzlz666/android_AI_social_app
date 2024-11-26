package com.example.sqltest2

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sqltest2.models.MyMessages

class MyMessagesAdapter(
    private val messages: List<MyMessages>,
    private val onExitButtonClick: () -> Unit  // 添加回调
) : RecyclerView.Adapter<MyMessagesAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contentText: TextView = itemView.findViewById(R.id.contentText)
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val exitButton: Button = itemView.findViewById(R.id.exitButton)
        val layout: LinearLayout = itemView.findViewById(R.id.innerLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_my_messages, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val message = messages[position]
        holder.contentText.text = message.content

        if (message.content == "我的信息") {
            holder.icon.setImageResource(R.drawable.myinfo)
            // 设置点击事件
            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, MyAllInfoActivity::class.java)
                holder.itemView.context.startActivity(intent)
            }
        }
        else if(message.content == "修改密码") {
            holder.icon.setImageResource(R.drawable.updatepas)
            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, UpdartePasswordActivity::class.java)
                holder.itemView.context.startActivity(intent)
            }
        }
        else if(message.content == "发布信息") {
            holder.icon.setImageResource(R.drawable.fabu)
            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, MessagesActivity::class.java)
                holder.itemView.context.startActivity(intent)
            }
        }
        else if(message.content == "我的主题") {
            holder.icon.setImageResource(R.drawable.theme)
            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, selectThemeActivity::class.java)
                holder.itemView.context.startActivity(intent)
            }
        }

        // 如果是第一个项目，设置 50dp 的顶部间距
        if (position == 0) {
            val layoutParams = holder.layout.layoutParams as LinearLayout.LayoutParams
            layoutParams.topMargin = 30.dpToPx(holder.itemView.context)  // 将 30dp 转换为 px
            holder.layout.layoutParams = layoutParams
        }

        // 如果是最后一个项目，显示退出按钮，否则隐藏
        if (position == messages.size - 1) {
            holder.exitButton.visibility = View.VISIBLE
            holder.exitButton.setOnClickListener {
                onExitButtonClick()  // 调用回调
            }
        } else {
            holder.exitButton.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
        return messages.size
    }

    // 扩展函数：将 dp 转换为 px
    fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}

