package com.example.sqltest2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sqltest2.utils.JWTStorageHelper
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

class CreateArticleActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var stateSwitch: SwitchMaterial
    private lateinit var stateTextView: TextView

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private val REQUEST_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_article)

        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)
        stateSwitch = findViewById(R.id.stateSwitch)
        stateTextView = findViewById(R.id.stateTextView)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val imageUploadLayout = findViewById<LinearLayout>(R.id.imageUploadLayout)
        val uploadImageView = findViewById<ImageView>(R.id.uploadImageView)

        // 请求读取外部存储权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION_CODE)
        }

        updateStateText()

        stateSwitch.setOnCheckedChangeListener { _, _ ->
            updateStateText()
            imageUploadLayout.visibility = if (stateSwitch.isChecked) View.GONE else View.VISIBLE
        }


        val categoryId = intent.getIntExtra("categoryId", -1)

        saveButton.setOnClickListener {
            if (categoryId != -1) {
                saveArticle(categoryId)
            } else {
                Toast.makeText(this, "分类 ID 无效，无法保存文章", Toast.LENGTH_SHORT).show()
            }
        }

        uploadImageView.setOnClickListener {
            openImageChooser()
        }
    }

    private fun updateStateText() {
        stateTextView.text = if (stateSwitch.isChecked) "草稿" else "已发布"
    }

    private fun saveArticle(categoryId: Int) {
        val title = titleEditText.text.toString().trim()
        val content = contentEditText.text.toString().trim()
        val state = if (stateSwitch.isChecked) "草稿" else "已发布"

        if (state == "已发布" && selectedImageUri != null) {
            selectedImageUri?.let { uri ->
                uploadImage(uri, this) { imageUrl ->
                    if (imageUrl != null) {
                        postArticle(title, content, imageUrl, state, categoryId)
                    } else {
                        Toast.makeText(this, "图片上传失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            val coverImg = "https://big-event-gwd.oss-cn-beijing.aliyuncs.com/9bf1cf5b-1420-4c1b-91ad-e0f4631cbed4.png"
            postArticle(title, content, coverImg, state, categoryId)
        }
    }

    private fun postArticle(title: String, content: String, coverImg: String, state: String, categoryId: Int) {
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

    private suspend fun postArticleWithJWT(jsonObject: JSONObject): Response? {
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

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            findViewById<ImageView>(R.id.uploadImageView).setImageURI(selectedImageUri)
        }
    }

    private fun uploadImage(uri: Uri, context: Context, callback: (String?) -> Unit) {
        Log.d("UploadService", "Uri: $uri")

        val token = JWTStorageHelper.getJwtToken(context) ?: run {
            Log.e("UploadService", "JWT token is null")
            callback(null)
            return
        }

        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File.createTempFile("upload", ".jpg", context.cacheDir)

        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val mediaType = "image/jpeg".toMediaType()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, RequestBody.create(mediaType, file))
            .build()

        val request = Request.Builder()
            .url("${com.example.sqltest2.utils.Constants.BASE_URL}/upload")
            .post(requestBody)
            .addHeader("Authorization", token)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UploadService", "Request failed", e)
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e("UploadService", "Response not successful: ${response.code} - ${response.message}")
                        callback(null)
                    } else {
                        val responseData = response.body?.string()
                        responseData?.let { data ->
                            try {
                                val json = JSONObject(data)
                                val url = json.getString("data")
                                callback(url)
                            } catch (e: Exception) {
                                Log.e("UploadService", "Error parsing JSON response", e)
                                callback(null)
                            }
                        } ?: run {
                            Log.e("UploadService", "Response body is null")
                            callback(null)
                        }
                    }
                }
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // 权限被授予
            } else {
                Toast.makeText(this, "权限被拒绝，无法访问外部存储", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onTagClick(view: View) {
        val tagText = (view as TextView).text.toString()
        val currentText = contentEditText.text.toString()
        contentEditText.setText("$currentText $tagText ")
        contentEditText.setSelection(contentEditText.text.length)
    }
}

