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
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sqltest2.api.ChatService
import com.example.sqltest2.models.ChatMessage
import com.example.sqltest2.utils.JWTStorageHelper
import com.google.android.material.navigation.NavigationView
import com.iflytek.sparkchain.core.LLM
import com.iflytek.sparkchain.core.LLMFactory
import com.iflytek.sparkchain.core.Memory
import com.iflytek.sparkchain.core.SparkChain
import com.iflytek.sparkchain.core.SparkChainConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class UnknownFragment : Fragment() {
    private val client = OkHttpClient()

    private lateinit var recyclerView: RecyclerView
    private lateinit var inputText: EditText
    private lateinit var sendButton: Button
    private lateinit var chatLLM: LLM
    private val chatMessages = mutableListOf<ChatMessage>() // 存储聊天记录
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var gptImageView: ImageView
    private val savedMessages = mutableListOf<ChatMessage>() // 存储已经保存过的消息
    private var currentConversationId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.activity_unknown_fragment, container, false)

        gptImageView = view.findViewById(R.id.gptImage)
        drawerLayout = view.findViewById(R.id.drawer_layout)
        navigationView = view.findViewById(R.id.nav_view)

        val navView = view.findViewById<NavigationView>(R.id.nav_view)
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        val layoutParams = navView.layoutParams
        layoutParams.width = (screenWidth * 0.8).toInt() // 80% 宽度
        navView.layoutParams = layoutParams

        recyclerView = view.findViewById(R.id.recyclerView)
        inputText = view.findViewById(R.id.inputText)
        sendButton = view.findViewById(R.id.sendButton)

        chatAdapter = ChatAdapter(chatMessages)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = chatAdapter

        initSDK() // 初始化 SDK

        sendButton.setOnClickListener {
            val question = inputText.text.toString()
            if (question.isNotEmpty()) {
                addMessage(question, true) // 添加用户消息
                inputText.text.clear() // 清空输入框
                sendMessage(question) // 调用 AI 进行回复
            }
        }

        val moreOptions = view.findViewById<ImageView>(R.id.moreOptions)
        moreOptions.setOnClickListener { showAiMenu(it) }

        fetchConversationGroups()
        checkChatMessages()

        return view
    }

    private fun initSDK() {
        val config = SparkChainConfig.builder()
            .appID("87911696") // 替换为你的 appID
            .apiKey("60cae27277c84f7b0068e553df3ff601") // 替换为你的 apiKey
            .apiSecret("ZTgyMDRiYmJjYTZiZTg4MmEyZDczOWQw") // 替换为你的 apiSecret

        val result = SparkChain.getInst().init(requireContext(), config)
        if (result != 0) {
            addMessage("SDK 初始化失败，错误码: $result", false)
            return
        }

        val memory = Memory.windowMemory(5)
        chatLLM = LLMFactory.textGeneration(memory)
    }

    private fun sendMessage(question: String) {
        lifecycleScope.launch {
            try {
                val syncOutput = withContext(Dispatchers.IO) {
                    chatLLM.run(question)
                }
                val aiResponse = syncOutput.getContent()
                addMessage(aiResponse, false) // 添加 AI 的回复

                // 只在这里保存 AI 的回复
                if (currentConversationId != null) {
                    saveChatMessages(currentConversationId!!)
                }

                // 在消息发送和添加后，检查消息状态，隐藏或显示图标
                checkChatMessages()

            } catch (e: Exception) {
                addMessage("调用失败: ${e.message}", false)
            }
        }
    }

    private fun addMessage(message: String, isUser: Boolean) {
        val newMessage = ChatMessage(message, isUser)
        chatMessages.add(newMessage)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        recyclerView.scrollToPosition(chatMessages.size - 1)

        checkChatMessages()
    }

    override fun onDestroy() {
        super.onDestroy()
        SparkChain.getInst().unInit()
    }

    private fun showAiMenu(view: View) {
        val aiMenu = PopupMenu(requireContext(), view)
        aiMenu.menuInflater.inflate(R.menu.ai_menu, aiMenu.menu)

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
                    showSaveDialog(requireContext())
                    true
                }
                R.id.claer_option -> {
                    clearChatHistory()
                    checkChatMessages()
                    true
                }
                R.id.history_option -> {
                    drawerLayout.openDrawer(GravityCompat.START)
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

        val input = EditText(context)
        builder.setView(input)

        builder.setPositiveButton("确认") { _, _ ->
            val conversationName = input.text.toString()
            if (conversationName.isNotEmpty()) {
                saveConversation(conversationName)
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

        chatService.saveConversation(conversationName) { success, conversationId ->
            lifecycleScope.launch(Dispatchers.Main) {
                if (success && conversationId != null) {
                    saveChatMessages(conversationId)
                    Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show()
                    fetchConversationGroups()
                } else {
                    Toast.makeText(requireContext(), "保存对话失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveChatMessages(conversationId: Int) {
        val chatService = ChatService(requireContext())

        lifecycleScope.launch(Dispatchers.IO) {
            val newMessages = chatMessages.filter { !savedMessages.contains(it) }

            newMessages.forEach { chatMessage ->
                val senderType = if (chatMessage.isUser) "YOU" else "CHATGPT"

                if (chatMessage.message.isNotBlank()) {
                    val success = chatService.saveMessage(conversationId, chatMessage.message, senderType)

                    withContext(Dispatchers.Main) {
                        if (success) {
                            Log.d("UnknownFragment", "消息保存成功: ${chatMessage.message}")
                            savedMessages.add(chatMessage)
                        } else {
                            Log.e("UnknownFragment", "消息保存失败: ${chatMessage.message}")
                        }
                    }
                }
            }
        }
    }

    private fun fetchConversationGroups() {
        val chatService = ChatService(requireContext())

        chatService.fetchConversationGroups { success, dataArray ->
            if (success && dataArray != null) {
                requireActivity().runOnUiThread {
                    updateMenu(dataArray)
                }
            } else {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "获取对话组失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateMenu(dataArray: JSONArray) {
        val menu = navigationView.menu
        menu.clear()

        for (i in 0 until dataArray.length()) {
            val itemObject = dataArray.getJSONObject(i)
            val conversationName = itemObject.getString("conversationName")
            val conversationId = itemObject.getInt("conversationId")

            val menuItem = menu.add(conversationName)
            menuItem.setOnMenuItemClickListener {
                currentConversationId = conversationId
                fetchMessagesByConversationId(conversationId)
                Toast.makeText(requireContext(), "点击了对话: $conversationName", Toast.LENGTH_SHORT).show()
                true
            }
        }

        navigationView.invalidate()
    }

    private fun clearChatHistory() {
        chatMessages.clear()
        chatAdapter.notifyDataSetChanged()
        Toast.makeText(requireContext(), "聊天记录已清空", Toast.LENGTH_SHORT).show()
    }

    private fun checkChatMessages() {
        if (chatMessages.isEmpty()) {
            gptImageView.visibility = View.VISIBLE
        } else {
            gptImageView.visibility = View.GONE
        }
    }

    private fun fetchMessagesByConversationId(conversationId: Int) {
        val chatService = ChatService(requireContext())

        chatService.fetchMessagesByConversationId(conversationId) { success, dataArray ->
            if (success && dataArray != null) {
                requireActivity().runOnUiThread {
                    updateChatMessages(dataArray)
                }
            } else {
                Log.e("UnknownFragment", "获取消息失败")
            }
        }
    }

    private fun updateChatMessages(dataArray: JSONArray) {
        chatMessages.clear()
        savedMessages.clear()

        for (i in 0 until dataArray.length()) {
            val messageObject = dataArray.getJSONObject(i)
            val senderType = messageObject.getString("senderType")
            val message = messageObject.getString("message")

            val isUser = senderType == "YOU"
            val chatMessage = ChatMessage(message, isUser)

            chatMessages.add(chatMessage)
            savedMessages.add(chatMessage)
        }

        chatAdapter.notifyDataSetChanged()
        recyclerView.scrollToPosition(chatMessages.size - 1)

        checkChatMessages()
    }
}
