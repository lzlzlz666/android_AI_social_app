package com.example.sqltest2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sqltest2.api.ApiCategoryService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdateCategoryActivity : AppCompatActivity() {

    private lateinit var editCategoryName: EditText
    private lateinit var editCategoryAlias: EditText
    private lateinit var buttonUpdate: Button
    private var categoryId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_category)

        // 初始化控件
        editCategoryName = findViewById(R.id.editCategoryName)
        editCategoryAlias = findViewById(R.id.editCategoryAlias)
        buttonUpdate = findViewById(R.id.buttonUpdate)

        // 获取传递过来的分类信息
        categoryId = intent.getIntExtra("categoryId", 0) // 获取传递的分类ID
        val categoryName = intent.getStringExtra("categoryName") // 获取分类名称
        val categoryAlias = intent.getStringExtra("categoryAlias") // 获取分类别名

        // 设置输入框的初始值
        editCategoryName.setText(categoryName)
        editCategoryAlias.setText(categoryAlias)

        // 设置按钮点击监听器
        buttonUpdate.setOnClickListener {
            val newCategoryName = editCategoryName.text.toString().trim()
            val newCategoryAlias = editCategoryAlias.text.toString().trim()

            // 检查输入是否为空
            if (newCategoryName.isNotEmpty() && newCategoryAlias.isNotEmpty()) {
                updateCategory(categoryId, newCategoryName, newCategoryAlias)
            } else {
                Toast.makeText(this, "请输入完整的分类名称和别名", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 调用 API 更新分类信息
    private fun updateCategory(id: Int, newCategoryName: String, newCategoryAlias: String) {
        CoroutineScope(Dispatchers.IO).launch {
            // 调用 ApiCategoryService 中的 updateCategory 方法
            val (success, errorMessage) = ApiCategoryService.updateCategory(
                this@UpdateCategoryActivity, id, newCategoryName, newCategoryAlias
            )

            withContext(Dispatchers.Main) {
                if (success) {
                    // 成功更新分类信息
                    Toast.makeText(this@UpdateCategoryActivity, "修改成功", Toast.LENGTH_SHORT).show()
                    ApiCategoryService.getCategories(this@UpdateCategoryActivity)
                    finish() // 关闭当前 Activity，返回上一界面
                } else {
                    // 显示错误信息
                    Toast.makeText(this@UpdateCategoryActivity, errorMessage ?: "修改失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
