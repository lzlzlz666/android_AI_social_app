package com.example.sqltest2.models

data class ChatMessage(
    var message: String,
    var isUser: Boolean // `true` 表示用户发送的消息，`false` 表示 AI 回复的消息
)