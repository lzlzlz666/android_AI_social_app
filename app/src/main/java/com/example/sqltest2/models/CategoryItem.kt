package com.example.sqltest2.models

data class CategoryItem(
    val id: Int,  // 新增 id 字段
    val imageResId: Int,
    val categoryName: String,
    val categoryAlias: String
)

