package com.example.sqltest2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.sqltest2.api.ApiUserService
import com.example.sqltest2.model.Password
import com.google.android.material.textfield.TextInputLayout


class UpdartePasswordActivity : AppCompatActivity() {

    private lateinit var oldPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var rePasswordEditText: EditText
    private lateinit var newPasswordLayout: TextInputLayout
    private lateinit var rePasswordLayout: TextInputLayout
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_updarte_password)

        // 初始化视图
        oldPasswordEditText = findViewById(R.id.old_password)
        newPasswordEditText = findViewById(R.id.new_password)
        rePasswordEditText = findViewById(R.id.re_password)
        newPasswordLayout = findViewById(R.id.new_password_layout)
        rePasswordLayout = findViewById(R.id.re_password_layout)
        submitButton = findViewById(R.id.submit_button)

        // 监听确认密码输入框的变化
        rePasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validatePasswords()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 提交按钮点击事件
        submitButton.setOnClickListener {
            val oldPwd = oldPasswordEditText.text.toString()
            val newPwd = newPasswordEditText.text.toString()
            val rePwd = rePasswordEditText.text.toString()

            if (validatePasswords()) {
                val password = Password(oldPwd, newPwd, rePwd)
                updatePassword(this, password)
            }
        }
    }

    // 校验密码是否匹配并显示错误信息
    private fun validatePasswords(): Boolean {
        val newPwd = newPasswordEditText.text.toString()
        val rePwd = rePasswordEditText.text.toString()

        return if (newPwd != rePwd) {
            rePasswordLayout.error = "新密码与确认密码不匹配"
            false
        } else {
            rePasswordLayout.error = null // 清除错误
            true
        }
    }

    private fun updatePassword(context: Context, password: Password) {
        Thread {
            val (success, message) = ApiUserService.updatePassword(context, password)

            runOnUiThread {
                if (success) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    // 跳转到 MyInfoActivity
                    val intent = Intent(this, MyInfoFragment::class.java)
                    startActivity(intent)
                    finish() // 关闭当前 Activity
                } else {
                    Toast.makeText(context, "错误：$message", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
