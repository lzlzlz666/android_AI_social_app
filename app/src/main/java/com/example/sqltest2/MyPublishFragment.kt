package com.example.sqltest2 // 确保包名正确

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.sqltest2.adapters.ItemMyAdapter
import com.example.sqltest2.api.ApiCategoryService
import com.example.sqltest2.databinding.FragmentMyPublishBinding
import kotlinx.coroutines.launch

class MyPublishFragment : Fragment() {

    private var _binding: FragmentMyPublishBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyPublishBinding.inflate(inflater, container, false)

        // 设置 RecyclerView
        setupRecyclerView()
        return binding.root
    }

    private fun setupRecyclerView() {
        // 使用协程获取数据
        lifecycleScope.launch {
            val (itemMyList, errorMessage) = ApiCategoryService.getPublishedMyArticles(requireContext())

            if (itemMyList != null) {
                // 创建适配器并设置给 RecyclerView
                val itemMyAdapter = ItemMyAdapter(itemMyList)
                binding.recyclerView.adapter = itemMyAdapter
                binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
            } else {
                // 处理错误信息，例如弹出提示
                errorMessage?.let { Log.e("MyPublishFragment", it) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 清理绑定
    }
}
