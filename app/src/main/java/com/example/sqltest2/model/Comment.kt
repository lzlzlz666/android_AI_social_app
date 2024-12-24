package com.example.sqltest2.model

data class Comment(
    val id: Int,
    val content: String,
    val createTime: String,
    val userId: Int,
    val userImg: String,
    val username: String,
    val parentCommentId: Int?,
    val replies: List<Comment>
)

data class CommentResponse(
    val code: Int,
    val message: String,
    val data: List<Comment>
)

