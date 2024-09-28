package com.example.sqltest2.models

data class ChatMessage(
    val message: String,
    val isUser: Boolean // `true` 表示用户发送的消息，`false` 表示 AI 回复的消息
)