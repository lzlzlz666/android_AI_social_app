package com.example.sqltest2

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.example.sqltest2.adapters.CarouselAdapter
import com.example.sqltest2.databinding.ActivityMessagesBinding

class MessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessagesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化时进入分布大厅
        enterHall()

        // 设置分布大厅的点击事件
        binding.textViewHall.setOnClickListener {
            enterHall()
        }

        // 设置我的发布的点击事件
        binding.textViewMyPublish.setOnClickListener {
            myPublish()
        }
    }

    private fun enterHall() {
        // 设置分布大厅的颜色为选中状态
        binding.textViewHall.setTextColor(resources.getColor(R.color.black))
        // 设置我的发布的颜色为未选中状态
        binding.textViewMyPublish.setTextColor(resources.getColor(R.color.gray))
        // 显示分布大厅的内容
        showHallContent()
    }

    private fun myPublish() {
        // 设置我的发布的颜色为选中状态
        binding.textViewMyPublish.setTextColor(resources.getColor(R.color.black))
        // 设置分布大厅的颜色为未选中状态
        binding.textViewHall.setTextColor(resources.getColor(R.color.gray))
        // 显示我的发布的内容
        showMyPublishContent()
    }

    private fun showHallContent() {
        // 清除 FrameLayout 中的所有视图
        binding.frameLayoutContent.removeAllViews()

        // 加载 HallFragment
        val fragment = HallFragment()
        supportFragmentManager.beginTransaction()
            .replace(binding.frameLayoutContent.id, fragment)
            .commit()
    }

    private fun showMyPublishContent() {
        // 清除 FrameLayout 中的所有视图
        binding.frameLayoutContent.removeAllViews()

        val fragment = MyPublishFragment()
        supportFragmentManager.beginTransaction()
            .replace(binding.frameLayoutContent.id, fragment)
            .commit()
    }
}
