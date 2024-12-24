package com.example.sqltest2.utils

import android.content.Context
import android.content.SharedPreferences

object JWTStorageHelper {

    private const val PREF_NAME = "AppPrefs"
    private const val JWT_KEY = "JWT_TOKEN"

    // 保存 JWT 令牌到 SharedPreferences
    fun saveJwtToken(context: Context, jwtToken: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(JWT_KEY, jwtToken.trim())  // 使用 .trim() 去除前后空格
        editor.apply()  // 异步存储，效率更高
    }

    // 读取jwt
    fun getJwtToken(context: Context): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(JWT_KEY, null)?.trim()  // 获取时也 .trim() 去除空格
    }


    // 删除 JWT 令牌（比如用户登出时）
    fun removeJwtToken(context: Context) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(JWT_KEY)
        editor.apply()
    }
}
