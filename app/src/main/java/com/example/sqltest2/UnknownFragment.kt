package com.example.sqltest2

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sqltest2.api.ChatService
import com.example.sqltest2.models.ChatMessage
import com.example.sqltest2.utils.JWTStorageHelper
import com.iflytek.sparkchain.core.LLM
import com.iflytek.sparkchain.core.LLMFactory
import com.iflytek.sparkchain.core.Memory
import com.iflytek.sparkchain.core.SparkChain
import com.iflytek.sparkchain.core.SparkChainConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class UnknownFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var inputText: EditText
    private lateinit var sendButton: Button
    private lateinit var chatLLM: LLM
    private val chatMessages = mutableListOf<ChatMessage>() // 存储聊天记录
    private lateinit var chatAdapter: ChatAdapter
    private val client = OkHttpClient() // OKHttpClient 实例

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.activity_unknown_fragment, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        inputText = view.findViewById(R.id.inputText)
        sendButton = view.findViewById(R.id.sendButton)

        // 设置 RecyclerView
        chatAdapter = ChatAdapter(chatMessages)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = chatAdapter

        initSDK() // 初始化 SDK

        // 设置发送按钮点击事件
        sendButton.setOnClickListener {
            val question = inputText.text.toString()
            if (question.isNotEmpty()) {
                addMessage(question, true) // 添加用户消息，true 表示用户发送
                inputText.text.clear() // 清空输入框
                sendMessage(question) // 调用 AI 进行回复
            }
        }

        // 添加点击更多选项事件
        val moreOptions = view.findViewById<ImageView>(R.id.moreOptions)
        moreOptions.setOnClickListener { showAiMenu(it) }

        return view
    }

    private fun initSDK() {
        // 使用 builder 方法创建 SparkChainConfig 实例
        val config = SparkChainConfig.builder()
            .appID("87911696") // 替换为你的 appID
            .apiKey("60cae27277c84f7b0068e553df3ff601") // 替换为你的 apiKey
            .apiSecret("ZTgyMDRiYmJjYTZiZTg4MmEyZDczOWQw") // 替换为你的 apiSecret

        // 初始化 SparkChain SDK
        val result = SparkChain.getInst().init(requireContext(), config)
        if (result != 0) {
            addMessage("SDK 初始化失败，错误码: $result", false)
            return
        }

        // 创建 LLM 实例
        val memory = Memory.windowMemory(5)
        chatLLM = LLMFactory.textGeneration(memory)
    }

    // 在 Fragment 中使用 Coroutine
    private fun sendMessage(question: String) {
        lifecycleScope.launch {
            try {
                val syncOutput = withContext(Dispatchers.IO) {
                    chatLLM.run(question) // 在 IO 线程中执行耗时操作
                }
                val aiResponse = syncOutput.getContent()
                addMessage(aiResponse, false) // 回到主线程更新 UI
            } catch (e: Exception) {
                addMessage("调用失败: ${e.message}", false)
            }
        }
    }

    private fun addMessage(message: String, isUser: Boolean) {
        chatMessages.add(ChatMessage(message, isUser))
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        recyclerView.scrollToPosition(chatMessages.size - 1) // 滚动到最新消息
    }

    override fun onDestroy() {
        super.onDestroy()
        SparkChain.getInst().unInit() // 逆初始化，释放 SDK 资源
    }

    private fun showAiMenu(view: View) {
        val aiMenu = PopupMenu(requireContext(), view)
        aiMenu.menuInflater.inflate(R.menu.ai_menu, aiMenu.menu)

        // 使用反射来显示图标
        try {
            val fields = aiMenu.javaClass.declaredFields
            for (field in fields) {
                if ("mPopup" == field.name) {
                    field.isAccessible = true
                    val menuPopupHelper = field.get(aiMenu)
                    val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons = classPopupHelper.getMethod("setForceShowIcon", Boolean::class.java)
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        aiMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save_option -> {
                    showSaveDialog(requireContext()) // 弹出对话框输入 conversationName
                    true
                }
                R.id.claer_option -> {
                    Toast.makeText(requireContext(), "清空操作", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.history_option -> {
                    Toast.makeText(requireContext(), "查找历史", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        aiMenu.show()
    }

    private fun showSaveDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("保存对话")

        // 创建一个 EditText 用于输入
        val input = EditText(context)
        builder.setView(input)

        // 设置确认和取消按钮
        builder.setPositiveButton("确认") { _, _ ->
            val conversationName = input.text.toString()
            if (conversationName.isNotEmpty()) {
                saveConversation(conversationName) // 调用保存方法
            } else {
                Toast.makeText(context, "名称不能为空", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("取消") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun saveConversation(conversationName: String) {
        val chatService = ChatService(requireContext())
        chatService.saveConversation(conversationName) { success ->
            lifecycleScope.launch(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "保存失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}