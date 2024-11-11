package com.example.sqltest2.models

import com.example.sqltest2.model.Comment

data class DetailItem(
    val articleId: Int,
    val title: String,
    val content: String,
    val img: String,
    val createUserImg: String,
    val createTime: String,
    val comment: List<Comment>
)
