package com.example.sqltest2.model

data class Password(
    val old_pwd: String,
    val new_pwd: String,
    val re_pwd: String
)
