package com.example.sqltest2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sqltest2.api.ApiCategoryService
import com.example.sqltest2.model.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddCategoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_category)

        val editCategoryName = findViewById<EditText>(R.id.editCategoryName)
        val editCategoryAlias = findViewById<EditText>(R.id.editCategoryAlias)
        val buttonConfirm = findViewById<Button>(R.id.buttonConfirm)

        buttonConfirm.setOnClickListener {
            val categoryName = editCategoryName.text.toString()
            val categoryAlias = editCategoryAlias.text.toString()

            // 检查输入是否为空
            if (categoryName.isNotEmpty() && categoryAlias.isNotEmpty()) {
                // 创建 Category 对象
                val category = Category(categoryName, categoryAlias)

                // 使用协程在后台线程中发起网络请求
                CoroutineScope(Dispatchers.IO).launch {
                    val (success, message) = ApiCategoryService.addCategory(this@AddCategoryActivity, category)

                    // 在主线程处理结果
                    withContext(Dispatchers.Main) {
                        if (success) {
                            // 新增成功，显示提示信息并关闭 Activity
                            Toast.makeText(this@AddCategoryActivity, "新增成功: $categoryName ($categoryAlias)", Toast.LENGTH_SHORT).show()

                            // 使用 this@AddCategoryActivity 代替 requireContext()
                            ApiCategoryService.getCategories(this@AddCategoryActivity)
                            finish() // 结束当前Activity
                        } else {
                            // 显示错误信息
                            Toast.makeText(this@AddCategoryActivity, message ?: "新增失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                // 输入为空时，提示用户输入
                Toast.makeText(this, "请输入分类名称和别名", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
