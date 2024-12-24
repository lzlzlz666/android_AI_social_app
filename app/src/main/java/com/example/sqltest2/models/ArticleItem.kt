package com.example.sqltest2.models

data class ArticleItem(
    val title: String,
    val content: String,
    val createUser: Int,
    val createTime: String,  // 添加创建时间
    val avatarResId: Int     // 添加用户头像资源ID
)
