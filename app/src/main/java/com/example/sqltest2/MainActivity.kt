package com.example.sqltest2

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.Data
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

            // 只使用用户名和密码创建 User 对象，其他属性使用默认值
            val user = User(username = username, password = password)

            // 在点击按钮时触发网络请求
            thread {
                try {
                    // 将用户名和密码传递给 registerUser 方法
                    val response = ApiUserService.registerUser(user)

                    // 获取响应体字符串
                    val responseBody = response.body?.string()

                    // 如果响应体不为空
                    if (responseBody != null) {
                        // 解析 JSON 响应
                        val jsonResponse = JSONObject(responseBody)
                        val code = jsonResponse.getInt("code")
                        val message = jsonResponse.getString("message")
                        // 尝试获取data字段
                        val data = jsonResponse.optJSONObject("data")?.toString() ?: "No data available"

                        // 在主线程上显示Toast并打印日志
                        runOnUiThread {
                            if (code == 1) {
                                // 用户名已被占用等情况
                                Toast.makeText(this@MainActivity, "注册失败: $message", Toast.LENGTH_SHORT).show()
                            } else {
                                // 注册成功
                                Toast.makeText(this@MainActivity, "注册成功", Toast.LENGTH_SHORT).show()
                            }
                            Log.d("MainActivity", "响应信息: code=$code, message=$message, data=$data")
                        }
                    } else {
                        // 响应为空
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "服务器无响应", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("MainActivity", "网络请求出错: ${e.message}")
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "请求出错！", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
