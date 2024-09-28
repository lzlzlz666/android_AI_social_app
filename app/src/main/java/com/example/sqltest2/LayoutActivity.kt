package com.example.sqltest2

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sqltest2.MyInfoFragment
import com.example.sqltest2.databinding.ActivityLayoutBinding
import com.example.sqltest2.utils.JWTStorageHelper

class LayoutActivity : AppCompatActivity() {

    // ViewBinding 实例
    private lateinit var binding: ActivityLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 ViewBinding
        binding = ActivityLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 从 JWTStorageHelper 读取 JWT 令牌
        val jwtToken = JWTStorageHelper.getJwtToken(this)
        if (jwtToken != null) {
            Log.d("MyApp", "JWT 令牌: $jwtToken")
        } else {
            Log.d("MyApp", "没有找到 JWT 令牌")
        }

        // 默认显示 HomeFragment
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        // 设置 BottomNavigationView 的点击监听器
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.LayoutFirstPage -> {
                    replaceFragment(HomeFragment()) // 切换到 HomeFragment
                    true
                }
                R.id.what -> {
                    replaceFragment(UnknownFragment()) // 切换到 UnknownFragment
                    true
                }
                R.id.myInfo -> {
                    replaceFragment(MyInfoFragment()) // 切换到 MyInfoFragment
                    true
                }
                else -> false
            }
        }
    }

    // 用于切换 Fragment 的方法
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }
}
