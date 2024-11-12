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
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sqltest2.api.ApiUserService.getUserInfo
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
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject

class UnknownFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var inputText: EditText
    private lateinit var sendButton: Button
    private lateinit var chatLLM: LLM
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var gptImageView: ImageView
    private lateinit var userInfoImageView: ImageView
    private lateinit var usernameTextView: TextView
    private val savedMessages = mutableListOf<ChatMessage>()
    private var currentConversationId: Int? = null
    private var isInitialized = false // 控制初始化状态

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 确保在这里返回视图
        val view = inflater.inflate(R.layout.activity_unknown_fragment, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isInitialized) {
            isInitialized = true

            gptImageView = view.findViewById(R.id.gptImage)
            drawerLayout = view.findViewById(R.id.drawer_layout)
            navigationView = view.findViewById(R.id.nav_view)
            userInfoImageView = view.findViewById(R.id.userInfo)
            usernameTextView = view.findViewById(R.id.username)

            recyclerView = view.findViewById(R.id.recyclerView)
            inputText = view.findViewById(R.id.inputText)
            sendButton = view.findViewById(R.id.sendButton)

            chatAdapter = ChatAdapter(chatMessages)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = chatAdapter

            initSDK()

            sendButton.setOnClickListener {
                val question = inputText.text.toString()
                if (question.isNotEmpty()) {
                    addMessage(question, true)
                    inputText.text.clear()
                    sendMessage(question)
                }
            }

            val moreOptions = view.findViewById<ImageView>(R.id.moreOptions)
            moreOptions.setOnClickListener { showAiMenu(it) }

            fetchConversationGroups()
            checkChatMessages()
            fetchUserInfo()
        }
    }


    private fun initSDK() {
        val config = SparkChainConfig.builder()
            .appID("87911696")
            .apiKey("60cae27277c84f7b0068e553df3ff601")
            .apiSecret("ZTgyMDRiYmJjYTZiZTg4MmEyZDczOWQw")

        val result = SparkChain.getInst().init(requireContext(), config)
        if (result != 0) {
            addMessage("SDK 初始化失败，错误码: $result", false)
            return
        }

        val memory = Memory.windowMemory(5)
        chatLLM = LLMFactory.textGeneration(memory)
    }

    // 获取用户信息并更新UI
    private fun fetchUserInfo() {
        lifecycleScope.launch(Dispatchers.IO) {
            val (user, errorMessage) = getUserInfo(requireContext())
            withContext(Dispatchers.Main) {
                if (user != null) {
                    // 更新用户头像和用户名
                    usernameTextView.text = user.username

                    // 设置头像，假设图片的 URL 或 drawable 已经在 user.avatar 中
                    val avatarUrl = user.userPic  // 假设获取到的是URL
                    loadUserAvatar(avatarUrl)
                } else {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadUserAvatar(avatarUrl: String) {
        Glide.with(this)
            .load(avatarUrl) // 如果是 URL
            .placeholder(R.drawable.loading) // 加载过程中显示默认头像
            .into(userInfoImageView)
    }

    private fun sendMessage(question: String) {
        lifecycleScope.launch {
            try {
                val syncOutput = withContext(Dispatchers.IO) {
                    chatLLM.run(question)
                }
                val aiResponse = syncOutput.getContent()
                addMessage(aiResponse, false)

                if (currentConversationId != null) {
                    saveChatMessages(currentConversationId!!)
                }

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

        // 强制显示图标（适用于某些情况下图标不显示的问题）
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

        // 设置菜单项点击事件
        aiMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save_option -> {
                    showSaveDialog(requireContext()) // 调用保存对话框
                    true
                }
                R.id.claer_option -> {
                    clearChatHistory()  // 调用清空聊天历史
                    checkChatMessages() // 检查并更新 UI
                    true
                }
                R.id.history_option -> {
                    drawerLayout.openDrawer(GravityCompat.START)  // 打开侧边栏
                    true
                }
                else -> false
            }
        }
        aiMenu.show() // 显示菜单
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
        val historyContainer = view?.findViewById<LinearLayout>(R.id.historyContainer)
        historyContainer?.removeAllViews()

        for (i in 0 until dataArray.length()) {
            val itemObject = dataArray.getJSONObject(i)
            val conversationName = itemObject.getString("conversationName")
            val conversationId = itemObject.getInt("conversationId")

            val menuItem = TextView(requireContext()).apply {
                text = conversationName
                textSize = 16f
                setPadding(50, 16, 16, 16) // 可选: 设置内容的 padding

                setOnClickListener {
                    currentConversationId = conversationId
                    fetchMessagesByConversationId(conversationId)
                    Toast.makeText(requireContext(), "点击了对话: $conversationName", Toast.LENGTH_SHORT).show()
                }

                setOnLongClickListener {
                    showOptionsMenu(it, conversationName, conversationId)
                    true
                }
            }

            // 设置 TextView 的外边距（margin）
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // 宽度为 match_parent
                LinearLayout.LayoutParams.WRAP_CONTENT  // 高度为 wrap_content
            ).apply {
                setMargins(0, 20, 0, 20) // 设置 margin 值，top 和 bottom 为 20px
            }

            menuItem.layoutParams = layoutParams

            historyContainer?.addView(menuItem)
        }
    }

    private fun showOptionsMenu(view: View, conversationName: String, conversationId: Int) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.history_menu, popupMenu.menu) // 创建一个菜单资源

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.rename_option -> {
                    showRenameDialog(conversationName, conversationId)
                    true
                }
                R.id.delete_option -> {
                    deleteConversation(conversationId)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showRenameDialog(oldName: String, conversationId: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("重命名对话")

        val input = EditText(requireContext()).apply {
            setText(oldName)
        }
        builder.setView(input)

        builder.setPositiveButton("确认") { _, _ ->
            val newName = input.text.toString()
            if (newName.isNotEmpty()) {
                renameConversation(conversationId, newName)
            } else {
                Toast.makeText(requireContext(), "名称不能为空", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("取消") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun renameConversation(conversationId: Int, newName: String) {
        lifecycleScope.launch {
            val chatService = ChatService(requireContext())
            val success = chatService.updateConversationName(conversationId, newName) // 调用更新接口
            if (success) {
                Toast.makeText(requireContext(), "对话名称已更新", Toast.LENGTH_SHORT).show()
                fetchConversationGroups() // 重新加载对话组以更新 UI
            } else {
                Toast.makeText(requireContext(), "更新名称失败", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun deleteConversation(conversationId: Int) {
        lifecycleScope.launch {
            val chatService = ChatService(requireContext())
            val success = chatService.deleteConversation(conversationId)

            if (success) {
                Toast.makeText(requireContext(), "对话已删除", Toast.LENGTH_SHORT).show()
                clearChatHistory()  // 调用清空聊天历史
                checkChatMessages() // 检查并更新 UI
                fetchConversationGroups() // 重新渲染对话组
            } else {
                Toast.makeText(requireContext(), "删除对话失败", Toast.LENGTH_SHORT).show()
            }
        }
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