package com.example.sqltest2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sqltest2.api.ApiUserService
import com.example.sqltest2.databinding.ActivityLoginBinding
import com.example.sqltest2.model.User
import com.example.sqltest2.utils.JWTStorageHelper
import org.json.JSONObject
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 使用 ViewBinding 初始化视图
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置跳转到注册界面的点击事件
        val loginText: TextView = binding.registerText
        loginText.setOnClickListener {
            // 跳转到 MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // 设置登录按钮的逻辑
        binding.loginButton.setOnClickListener {
            // 获取用户名和密码
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()

            // 在这里执行登录逻辑
            val user = User(username = username, password = password)

            // 在点击按钮时触发网络请求
            thread {
                try {
                    val response = ApiUserService.loginUser(user)

                    // 获取响应体字符串
                    val responseBody = response.body?.string()

                    // 如果响应体不为空
                    if (responseBody != null) {
                        val jsonResponse = JSONObject(responseBody)
                        val code = jsonResponse.getInt("code")
                        val message = jsonResponse.getString("message")
                        val data = jsonResponse.getString("data")  // JWT 令牌

                        // 在主线程上显示Toast并保存 JWT 到 SharedPreferences
                        runOnUiThread {
                            if (code == 1) {
                                // 登录失败
                                Toast.makeText(this@LoginActivity, "登录失败: $message", Toast.LENGTH_SHORT).show()
                            } else {
                                // 登录成功，存储 JWT 令牌
                                // saveJwtToken(data)
                                // 假设这是在 LoginActivity 中，保存登录后的 JWT 令牌
                                JWTStorageHelper.saveJwtToken(this, data)  // 保存令牌


                                Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()

                                // 跳转到 LayoutActivity
                                val intent = Intent(this, LayoutActivity::class.java)
                                startActivity(intent)
                            }
                            Log.d("LoginActivity", "响应信息: code=$code, message=$message, data=$data")
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "服务器无响应", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("MainActivity", "网络请求出错: ${e.message}")
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "请求出错！", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


}
