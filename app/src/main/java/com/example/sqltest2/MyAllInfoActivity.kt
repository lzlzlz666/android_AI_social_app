package com.example.sqltest2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.sqltest2.api.ApiUserService
import com.example.sqltest2.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyAllInfoActivity : AppCompatActivity() {

    private lateinit var usernameTextView: TextView
    private lateinit var nicknameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var createTimeTextView: TextView
    private lateinit var updateButton: ImageView
    private lateinit var returnButton: ImageView
    private lateinit var profileImage: ImageView
    private var currentUserId: Int? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_all_info)

        // 初始化 TextView 和按钮
        usernameTextView = findViewById(R.id.username)
        nicknameTextView = findViewById(R.id.nickname)
        emailTextView = findViewById(R.id.email)
        createTimeTextView = findViewById(R.id.createtime)
        updateButton = findViewById(R.id.update_button)
        returnButton = findViewById(R.id.arrowHead)
        profileImage = findViewById(R.id.profileImage)

        loadUserPic()


        // 调用获取用户信息的方法
        fetchUserInfo()

        // 设置更新按钮的点击事件
        updateButton.setOnClickListener {
            showUpdateDialog()
        }

        returnButton.setOnClickListener {
            val intent = Intent(this, MyInfoFragment::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserPic() {
        lifecycleScope.launch(Dispatchers.IO) {
            val (user, errorMessage) = ApiUserService.getUserInfo(this@MyAllInfoActivity)

            withContext(Dispatchers.Main) {
                if (user != null) {
                    val imageUrl = user.userPic

                    Glide.with(this@MyAllInfoActivity)
                        .load(imageUrl)
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.error)
                        .circleCrop()
                        .into(profileImage)

                } else {
                    Toast.makeText(this@MyAllInfoActivity, errorMessage ?: "加载用户信息失败", Toast.LENGTH_SHORT).show()
                }
            }
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
                        createTimeTextView.text = formatDateTime(it.createTime)
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

    private fun formatDateTime(dateTime: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
        return try {
            val date: Date = inputFormat.parse(dateTime) ?: return dateTime
            outputFormat.format(date)
        } catch (e: Exception) {
            dateTime // 如果解析失败，返回原始字符串
        }
    }

}
