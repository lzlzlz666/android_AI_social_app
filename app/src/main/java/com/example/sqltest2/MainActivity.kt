package com.example.sqltest2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sqltest2.api.ApiUserService
import com.example.sqltest2.databinding.ActivityMainBinding
import com.example.sqltest2.model.User
import org.json.JSONObject
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    // 定义 ViewBinding 对象
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 使用 ViewBinding 初始化视图
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置跳转到登录界面的点击事件
        val loginText: TextView = binding.loginText
        loginText.setOnClickListener {
            // 跳转到 LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // 设置点击事件监听器
        binding.registerButton.setOnClickListener {
            // 获取用户输入的用户名和密码
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()

            // 输入验证
            if (!validateInput(username, password)) {
                return@setOnClickListener
            }

            // 禁用注册按钮，防止重复点击
            binding.registerButton.isEnabled = false

            // 创建 User 对象
            val user = User(username = username, password = password)

            // 在后台线程中执行网络请求
            thread {
                try {
                    // 调用API注册用户
                    val response = ApiUserService.registerUser(user)

                    // 处理服务器响应
                    handleResponse(response)

                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("MainActivity", "网络请求出错: ${e.message}")
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "请求出错！", Toast.LENGTH_SHORT).show()
                        binding.registerButton.isEnabled = true
                    }
                }
            }
        }
    }

    // 输入验证函数
    private fun validateInput(username: String, password: String): Boolean {
        // 用户名验证
        if (username.length !in 5..16) {
            binding.usernameLayout.error = "用户名长度需要在5到16个字符之间"
            return false
        } else {
            binding.usernameLayout.error = null // 清除错误信息
        }

        // 密码验证（可以根据需求增加验证逻辑）
        if (password.length < 6) {
            binding.passwordLayout.error = "密码长度不能少于6个字符"
            return false
        } else {
            binding.passwordLayout.error = null // 清除错误信息
        }

        return true
    }

    // 处理服务器响应
    private fun handleResponse(response: okhttp3.Response) {
        val responseBody = response.body?.string()
        if (responseBody != null) {
            // 解析JSON响应
            val jsonResponse = JSONObject(responseBody)
            val code = jsonResponse.getInt("code")
            val message = jsonResponse.getString("message")
            val data = jsonResponse.optJSONObject("data")?.toString() ?: "No data available"

            runOnUiThread {
                if (code == 1) {
                    // 用户名已被占用或其他错误
                    Toast.makeText(this@MainActivity, "注册失败: $message", Toast.LENGTH_SHORT).show()
                } else {
                    // 注册成功
                    Toast.makeText(this@MainActivity, "注册成功", Toast.LENGTH_SHORT).show()
                    // 可以在这里添加跳转到登录界面的逻辑
                }
                Log.d("MainActivity", "响应信息: code=$code, message=$message, data=$data")
                // 启用按钮
                binding.registerButton.isEnabled = true
            }
        } else {
            // 响应为空
            runOnUiThread {
                Toast.makeText(this@MainActivity, "服务器无响应", Toast.LENGTH_SHORT).show()
                binding.registerButton.isEnabled = true
            }
        }
    }
}
