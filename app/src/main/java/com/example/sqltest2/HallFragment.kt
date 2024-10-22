package com.example.sqltest2

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.sqltest2.adapters.CarouselAdapter
import com.example.sqltest2.adapters.ItemAdapter
import com.example.sqltest2.adapters.OnItemLikedListener
import com.example.sqltest2.api.ApiCategoryService
import com.example.sqltest2.databinding.FragmentHallBinding
import com.example.sqltest2.models.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HallFragment : Fragment(), OnItemLikedListener {

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
        lifecycleScope.launch {
            val (itemList, errorMessage) = ApiCategoryService.getPublishedArticles(requireContext())

            if (itemList != null) {
                val updatedItemList = itemList.map { item ->
                    val (likeCount, _) = withContext(Dispatchers.IO) {
                        ApiCategoryService.getLikeCount(requireContext(), item.id)
                    }
                    item.copy(likeCount = likeCount ?: 0)
                }

                val itemAdapter = ItemAdapter(updatedItemList, this@HallFragment)
                binding.recyclerView.adapter = itemAdapter
                binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
            } else {
                errorMessage?.let { Log.e("HallFragment", it) }
            }
        }
    }

    override fun onItemLiked(itemId: Int) {
        lifecycleScope.launch {
            val (success, errorMessage) = ApiCategoryService.addLike(requireContext(), itemId)
            if (success) {
                // 点赞成功的处理逻辑
                Toast.makeText(requireContext(), "点赞成功！", Toast.LENGTH_SHORT).show()
                fetchPublishedArticles()
            } else {
                // 处理错误信息
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
