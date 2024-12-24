package com.example.sqltest2.utils

import android.content.Context
import com.iflytek.sparkchain.core.LLM
import com.iflytek.sparkchain.core.LLMFactory
import com.iflytek.sparkchain.core.Memory
import com.iflytek.sparkchain.core.SparkChain
import com.iflytek.sparkchain.core.SparkChainConfig

class SparkManager private constructor() {
    private var chatLLM: LLM? = null

    companion object {
        @Volatile
        private var instance: SparkManager? = null

        fun getInstance(): SparkManager =
            instance ?: synchronized(this) {
                instance ?: SparkManager().also { instance = it }
            }
    }

    suspend fun initSDK(context: Context): Int {
        val config = SparkChainConfig.builder()
            .appID("87911696")
            .apiKey("60cae27277c84f7b0068e553df3ff601")
            .apiSecret("ZTgyMDRiYmJjYTZiZTg4MmEyZDczOWQw")

        val result = SparkChain.getInst().init(context, config)
        if (result == 0) {
            val memory = Memory.windowMemory(5)
            chatLLM = LLMFactory.textGeneration(memory)
        }
        return result
    }

    suspend fun sendMessage(message: String): String {
        return chatLLM?.run(message)?.getContent() ?: throw IllegalStateException("LLM未初始化")
    }
}