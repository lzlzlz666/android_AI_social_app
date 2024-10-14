package com.example.sqltest2

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sqltest2.utils.JWTStorageHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class CreateArticleActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var stateSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_article)

        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)
        stateSpinner = findViewById(R.id.stateSpinner)
        val saveButton = findViewById<Button>(R.id.saveButton)

        // 设置 Spinner 的适配器
        val states = arrayOf("草稿", "已发布")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, states)
        stateSpinner.adapter = adapter

        // 从 Intent 中获取传递的 categoryId
        val categoryId = intent.getIntExtra("categoryId", -1)  // 默认值为 -1，表示未传递或获取失败

        saveButton.setOnClickListener {
            if (categoryId != -1) {
                // 调用 saveArticle 方法并传递 categoryId
                saveArticle(categoryId)
            } else {
                Toast.makeText(this, "分类 ID 无效，无法保存文章", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveArticle(categoryId: Int) {
        val title = titleEditText.text.toString()
        val content = contentEditText.text.toString()
        val state = stateSpinner.selectedItem.toString()
        val coverImg = "https://big-event-gwd.oss-cn-beijing.aliyuncs.com/9bf1cf5b-1420-4c1b-91ad-e0f4631cbed4.png"

        val jsonObject = JSONObject().apply {
            put("title", title)
            put("content", content)
            put("coverImg", coverImg)
            put("state", state)
            put("categoryId", categoryId)  // 使用传递过来的 categoryId
        }

        CoroutineScope(Dispatchers.IO).launch {
            val response = postArticleWithJWT(jsonObject)
            withContext(Dispatchers.Main) {
                if (response != null && response.isSuccessful) {  // 添加 null 检查
                    Toast.makeText(this@CreateArticleActivity, "文章保存成功", Toast.LENGTH_SHORT).show()
                    // 通知前一个界面刷新数据
                    val resultIntent = Intent()
                    setResult(RESULT_OK, resultIntent)

                    finish()  // 返回到前一个界面，并触发刷新
                } else {
                    Toast.makeText(this@CreateArticleActivity, "文章保存失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun postArticleWithJWT(jsonObject: JSONObject): okhttp3.Response? {
        // 获取 JWT token
        val token = JWTStorageHelper.getJwtToken(this) ?: return null

        // 创建请求体
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaType())

        // 创建请求
        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/article")
            .addHeader("Authorization", token)  // 添加 JWT token
            .post(requestBody)
            .build()

        // 执行网络请求
        return withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }
    }
}
