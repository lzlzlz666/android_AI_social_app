package com.example.sqltest2.model

data class User(
    val id: Int = 0,
    val username: String,
    val password: String,
    val nickname: String = "", // 提供默认值
    val email: String = "", // 提供默认值
    val userPic: String = "", // 提供默认值
    val createTime: String = "", // 时间戳为字符串
    val updateTime: String = ""  // 时间戳为字符串
)
