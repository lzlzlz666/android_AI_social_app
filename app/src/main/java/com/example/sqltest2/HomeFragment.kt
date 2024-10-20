package com.example.sqltest2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sqltest2.adapters.CategoryAdapter
import com.example.sqltest2.api.ApiCategoryService
import com.example.sqltest2.models.CategoryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private val categories = ArrayList<CategoryItem>()
    private lateinit var cancelDeleteButton: Button
    private lateinit var cancelUpdateButton: Button
    private lateinit var descriptionTextView: TextView
    private lateinit var updateDescriptionTextView: TextView
    private lateinit var searchInput: EditText
    private lateinit var searchButton: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_home_fragment, container, false)

        // 初始化RecyclerView
        recyclerView = view.findViewById(R.id.Categories)
        recyclerView.layoutManager = GridLayoutManager(context, 2) // 设置为2列的网格布局

        // 初始化适配器
        categoryAdapter = CategoryAdapter(
            categories,
            { id -> deleteCategory(id) },
            { category -> updateCategory(category) }, // 新增修改功能的回调
            { category ->
                // 点击分类项时跳转到 CategoryDetailActivity 并传递数据
                val intent = Intent(requireContext(), CategoryDetailActivity::class.java)
                intent.putExtra("categoryId", category.id)
                intent.putExtra("categoryName", category.categoryName)
                intent.putExtra("categoryAlias", category.categoryAlias)
                startActivity(intent)
            }
        )
        recyclerView.adapter = categoryAdapter

        // 初始化取消删除和修改按钮
        cancelDeleteButton = view.findViewById(R.id.cancelDeleteButton)
        cancelUpdateButton = view.findViewById(R.id.updateButton)
        descriptionTextView = view.findViewById(R.id.description)
        updateDescriptionTextView = view.findViewById(R.id.updateDescription)

        // 设置取消删除的监听器
        cancelDeleteButton.setOnClickListener {
            categoryAdapter.toggleDeleteMode()
            cancelDeleteButton.visibility = View.GONE
            descriptionTextView.visibility = View.GONE
        }

        // 设置取消修改的监听器
        cancelUpdateButton.setOnClickListener {
            categoryAdapter.toggleUpdateMode()
            cancelUpdateButton.visibility = View.GONE
            updateDescriptionTextView.visibility = View.GONE
        }

        // 发起API请求加载分类数据
        loadCategories()

        // 添加点击更多选项事件
        val moreOptions = view.findViewById<ImageView>(R.id.moreOptions)
        moreOptions.setOnClickListener { showPopupMenu(it) }

        // 初始化搜索框和搜索按钮
        searchInput = view.findViewById(R.id.searchInput)
        searchButton = view.findViewById(R.id.searchButton)

        // 设置搜索按钮点击事件
        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()

            // 调用搜索功能
            searchCategories(query)
        }

        return view
    }

    private fun loadCategories() {
        // 使用协程异步发起API请求
        CoroutineScope(Dispatchers.IO).launch {
            val (categoryItems, errorMessage) = ApiCategoryService.getCategories(requireContext())

            withContext(Dispatchers.Main) {
                if (errorMessage != null) {
                    // 请求失败，显示错误提示
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                } else if (categoryItems != null) {
                    // 请求成功，更新数据
                    categories.clear()
                    categories.addAll(categoryItems)
                    categoryAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.add_option -> {
                    // 处理新增操作
                    val intent = Intent(requireContext(), AddCategoryActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.delete_option -> {
                    // 进入删除模式，显示垃圾桶图标和描述文本
                    categoryAdapter.toggleDeleteMode()
                    cancelDeleteButton.visibility = View.VISIBLE
                    descriptionTextView.visibility = View.VISIBLE
                    true
                }
                R.id.update_option -> {
                    // 进入修改模式，显示修改图标和描述文本
                    categoryAdapter.toggleUpdateMode()
                    cancelUpdateButton.visibility = View.VISIBLE
                    updateDescriptionTextView.visibility = View.VISIBLE
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    // 删除分类的方法，调用后端 DELETE API
    private fun deleteCategory(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val (success, errorMessage) = ApiCategoryService.deleteCategory(requireContext(), id)

            withContext(Dispatchers.Main) {
                if (success) {
                    // 成功删除，更新UI
                    categories.removeAll { it.id == id }
                    categoryAdapter.notifyDataSetChanged()
                    Toast.makeText(context, "删除成功！", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, errorMessage ?: "删除失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 修改分类的方法，启动修改界面
    private fun updateCategory(category: CategoryItem) {
        val intent = Intent(requireContext(), UpdateCategoryActivity::class.java)
        intent.putExtra("categoryId", category.id)
        intent.putExtra("categoryName", category.categoryName)
        intent.putExtra("categoryAlias", category.categoryAlias)
        startActivity(intent)
    }

    // 搜索分类
    private fun searchCategories(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val (categoryItems, errorMessage) = ApiCategoryService.searchCategories(requireContext(), query)

            withContext(Dispatchers.Main) {
                if (errorMessage != null) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                } else if (categoryItems != null) {
                    categories.clear()
                    categories.addAll(categoryItems)
                    categoryAdapter.notifyDataSetChanged()
                }
            }
        }
    }

}
