package com.example.sqltest2

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.sqltest2.adapters.CarouselAdapter
import com.example.sqltest2.adapters.ItemAdapter
import com.example.sqltest2.api.ApiCategoryService
import com.example.sqltest2.databinding.FragmentHallBinding
import com.example.sqltest2.models.Item
import kotlinx.coroutines.launch

class HallFragment : Fragment() {

    private var _binding: FragmentHallBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHallBinding.inflate(inflater, container, false)

        // 设置 ViewPager2
        setupCarousel()

        // 设置 RecyclerView
        setupRecyclerView()

        // 获取动态数据
        fetchPublishedArticles()

        return binding.root
    }

    private fun setupCarousel() {
        val imageList = listOf(
            R.drawable.lunbo_1,
            R.drawable.lunbo_2,
            R.drawable.lunbo_3
        )

        val carouselAdapter = CarouselAdapter(imageList)
        binding.viewPagerCarousel.adapter = carouselAdapter
        startAutoScroll(carouselAdapter)
    }

    private fun startAutoScroll(adapter: CarouselAdapter) {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (currentPage == adapter.itemCount) {
                    currentPage = 0
                }
                binding.viewPagerCarousel.setCurrentItem(currentPage++, true)
                handler.postDelayed(this, 2000)
            }
        }, 2000)
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
    }

    private fun fetchPublishedArticles() {
        // 使用协程获取数据
        lifecycleScope.launch {
            val (itemList, errorMessage) = ApiCategoryService.getPublishedArticles(requireContext())

            if (itemList != null) {
                // 创建适配器并设置给 RecyclerView
                val itemAdapter = ItemAdapter(itemList)
                binding.recyclerView.adapter = itemAdapter
                binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
            } else {
                // 处理错误信息，例如弹出提示
                errorMessage?.let { Log.e("HallFragment", it) }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }
}
