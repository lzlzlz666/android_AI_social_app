package com.example.sqltest2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.example.sqltest2.api.ApiUserService
import com.example.sqltest2.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*

class MyAllInfoActivity : AppCompatActivity() {

    private lateinit var usernameTextView: TextView
    private lateinit var nicknameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var createTimeTextView: TextView
    private lateinit var updateButton: Button
    private var currentUserId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_all_info)

        // 初始化 TextView 和按钮
        usernameTextView = findViewById(R.id.username)
        nicknameTextView = findViewById(R.id.nickname)
        emailTextView = findViewById(R.id.email)
        createTimeTextView = findViewById(R.id.createtime)
        updateButton = findViewById(R.id.update_button) // 确保有这个按钮

        // 调用获取用户信息的方法
        fetchUserInfo()

        // 设置更新按钮的点击事件
        updateButton.setOnClickListener {
            showUpdateDialog()
        }
    }

    private fun fetchUserInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            val (user, error) = ApiUserService.getUserInfo(this@MyAllInfoActivity)

            withContext(Dispatchers.Main) {
                if (error != null) {
                    Toast.makeText(this@MyAllInfoActivity, error, Toast.LENGTH_LONG).show()
                } else {
                    user?.let {
                        currentUserId = it.id  // 保存用户 ID
                        usernameTextView.text = it.username
                        nicknameTextView.text = it.nickname ?: "未设置"
                        emailTextView.text = it.email ?: "未设置"
                        createTimeTextView.text = it.createTime
                    }
                }
            }
        }
    }

    private fun showUpdateDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_info, null)
        val editUsername = dialogView.findViewById<EditText>(R.id.edit_username)
        val editNickname = dialogView.findViewById<EditText>(R.id.edit_nickname)
        val editEmail = dialogView.findViewById<EditText>(R.id.edit_email)

        // 设置现有数据到编辑框
        editUsername.setText(usernameTextView.text)
        editNickname.setText(nicknameTextView.text)
        editEmail.setText(emailTextView.text)

        AlertDialog.Builder(this)
            .setTitle("更新信息")
            .setView(dialogView)
            .setPositiveButton("确定") { dialog, _ ->
                // 获取输入的值
                val newUsername = editUsername.text.toString()
                val newNickname = editNickname.text.toString()
                val newEmail = editEmail.text.toString()

                // 更新用户信息逻辑
                updateUserInfo(newUsername, newNickname, newEmail)
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun updateUserInfo(username: String, nickname: String, email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val (user, error) = ApiUserService.getUserInfo(this@MyAllInfoActivity)

            if (error == null && user != null) {
                val updatedUser = user.copy(username = username, nickname = nickname, email = email)

                // 调用 API 更新用户信息
                val updateResponse = ApiUserService.updateUserInfo(this@MyAllInfoActivity, updatedUser)

                withContext(Dispatchers.Main) {
                    if (updateResponse.isSuccessful) {
                        Toast.makeText(this@MyAllInfoActivity, "用户信息更新成功", Toast.LENGTH_SHORT).show()
                        fetchUserInfo()  // 刷新信息
                    } else {
                        Toast.makeText(this@MyAllInfoActivity, "更新失败，请重试", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


}
