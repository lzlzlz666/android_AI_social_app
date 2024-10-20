package com.example.sqltest2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.switchmaterial.SwitchMaterial
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
    private lateinit var stateSwitch: SwitchMaterial
    private lateinit var stateTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_article)

        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)
        stateSwitch = findViewById(R.id.stateSwitch)
        stateTextView = findViewById(R.id.stateTextView)
        val saveButton = findViewById<Button>(R.id.saveButton)

        // 初始状态文本
        updateStateText()

        // Switch 切换事件
        stateSwitch.setOnCheckedChangeListener { _, _ ->
            updateStateText()
        }

        val categoryId = intent.getIntExtra("categoryId", -1)

        saveButton.setOnClickListener {
            if (categoryId != -1) {
                saveArticle(categoryId)
            } else {
                Toast.makeText(this, "分类 ID 无效，无法保存文章", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStateText() {
        val state = if (stateSwitch.isChecked) "草稿" else "发布"
        stateTextView.text = state
    }

    private fun saveArticle(categoryId: Int) {
        val title = titleEditText.text.toString()
        val content = contentEditText.text.toString()
        val state = if (stateSwitch.isChecked) "草稿" else "已发布"
        val coverImg = "https://big-event-gwd.oss-cn-beijing.aliyuncs.com/9bf1cf5b-1420-4c1b-91ad-e0f4631cbed4.png"

        val jsonObject = JSONObject().apply {
            put("title", title)
            put("content", content)
            put("coverImg", coverImg)
            put("state", state)
            put("categoryId", categoryId)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val response = postArticleWithJWT(jsonObject)
            withContext(Dispatchers.Main) {
                if (response != null && response.isSuccessful) {
                    Toast.makeText(this@CreateArticleActivity, "文章保存成功", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@CreateArticleActivity, "文章保存失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun postArticleWithJWT(jsonObject: JSONObject): okhttp3.Response? {
        val token = JWTStorageHelper.getJwtToken(this) ?: return null
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/article")
            .addHeader("Authorization", token)
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }
    }
}
