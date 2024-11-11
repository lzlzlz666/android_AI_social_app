package com.example.sqltest2.models

// 发布大厅--内容
data class Item(
    val id: Int,
    val title: String,
    val content: String,
    val img: String,
    val createTime: String,
    val createUserName: String,
    val createUserImg: String,
    val userId: Int,
    var isLiked: Boolean = false,
    var likeCount: Int = 0 // 添加点赞数字段
)
