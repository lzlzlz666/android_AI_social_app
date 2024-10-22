package com.example.sqltest2 // 确保包名正确

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.sqltest2.adapters.ItemAdapter
import com.example.sqltest2.adapters.ItemMyAdapter
import com.example.sqltest2.api.ApiCategoryService
import com.example.sqltest2.databinding.FragmentMyPublishBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        lifecycleScope.launch {
            val (itemMyList, errorMessage) = ApiCategoryService.getPublishedMyArticles(requireContext())

            if (itemMyList != null) {
                val updatedItemList = itemMyList.map { item ->
                    // 使用 withContext 来确保在 IO 线程中执行
                    val (likeCount, _) = withContext(Dispatchers.IO) {
                        ApiCategoryService.getLikeCount(requireContext(), item.id)
                    }
                    item.copy(likeCount = likeCount ?: 0) // 默认设置为0
                }

                // 创建适配器并设置给 RecyclerView
                val itemMyAdapter = ItemMyAdapter(updatedItemList)
                binding.recyclerView.adapter = itemMyAdapter
                binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
            } else {
                errorMessage?.let { Log.e("MyPublishFragment", it) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 清理绑定
    }
}
