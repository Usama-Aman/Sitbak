package com.android.sitbak.home.shop_search_result

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.sitbak.R
import com.android.sitbak.databinding.DialogShopSearchBinding
import com.android.sitbak.home.shops.ShopsFragment
import com.android.sitbak.home.shops.StoresData
import com.android.sitbak.home.shops.StoresModel
import com.android.sitbak.utils.AppUtils
import com.android.sitbak.utils.viewGone
import com.android.sitbak.utils.viewVisible


class ShopSearchDialog(_shopsFragment: ShopsFragment) : DialogFragment(), ShopsFragment.ShopSearchInterface, ShopSearchAdapter.ShopSearchResult {

    private var searchWord = ""
    private lateinit var binding: DialogShopSearchBinding
    private lateinit var mContext: Context
    private lateinit var shopsAdapter: ShopSearchAdapter
    private lateinit var productsAdapter: ShopSearchAdapter

    private val shopsFragment = _shopsFragment
    private val searchData: MutableList<StoresData> = ArrayList()


    override fun onResume() {
        val window: Window? = dialog!!.window
        val size = Point()
        // Store dimensions of the screen in `size`
        // Store dimensions of the screen in `size`
        val display: Display = window!!.windowManager.defaultDisplay
        display.getSize(size)
        // Set the width of the dialog proportional to 75% of the screen width
        // Set the width of the dialog proportional to 75% of the screen width
        window.setLayout((size.x * 0.90).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.TOP)
        // Call super onResume after sizing
        // Call super onResume after sizing
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DialogShopSearchBinding.inflate(layoutInflater, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        initViews()
        initShopAdapter()
        initProductAdapter()
    }

    private fun initViews() {

        binding.tvNoData.text = "Search"
        AppUtils.showKeyboardOnEdittext(binding.etShopSearch, mContext)
        binding.etShopSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length!! > 0)
                    binding.searchCross.viewVisible()
                else
                    binding.searchCross.viewGone()
            }

        })

        binding.tvAllStores.setOnClickListener {
            dismiss()
            shopsFragment.gotoSearchResultActivity(searchWord)
        }

        binding.etShopSearch.setOnEditorActionListener { v, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || event.action == KeyEvent.ACTION_DOWN
                || event.keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                searchWord = binding.etShopSearch.text.toString()
                shopsFragment.searchResult(searchWord, this)
                AppUtils.hideKeyBoardFromEdittext(binding.etShopSearch, mContext)

                true
            }
            false
        }

        binding.searchCross.setOnClickListener {
            binding.etShopSearch.setText("")
            AppUtils.hideKeyBoardFromEdittext(binding.etShopSearch, mContext)
        }
    }

    private fun initShopAdapter() {
        shopsAdapter = ShopSearchAdapter(searchData, this)
        binding.shopsRecyclerView.layoutManager = LinearLayoutManager(mContext)
        binding.shopsRecyclerView.adapter = shopsAdapter
    }

    private fun initProductAdapter() {
        productsAdapter = ShopSearchAdapter(searchData, this)
        binding.productsRecyclerView.layoutManager = LinearLayoutManager(mContext)
        binding.productsRecyclerView.adapter = productsAdapter
    }

    override fun shopSearchResult(model: StoresModel) {

        binding.tvAllStores.text = mContext.resources.getString(R.string.all_stores_count, model.stores_count.toString())
        binding.tvAllStores.viewVisible()

        val data = model.data

        if (data.isNullOrEmpty()) {
            binding.shopsRecyclerView.viewGone()
            binding.tvAllStores.viewGone()
            binding.tvNoData.viewVisible()
            binding.tvNoData.text = mContext.resources.getString(R.string.no_data_found)

        } else {
            binding.shopsRecyclerView.viewVisible()
            binding.tvNoData.viewGone()
            searchData.clear()

            if (model.data.size > 3) {
                for (i in model.data.indices)
                    searchData.add(model.data[i])
            } else
                searchData.addAll(data)
            shopsAdapter.notifyDataSetChanged()
        }

    }

    override fun shopSearchItemClicked(position: Int) {
        shopsFragment.gotoSearchResultActivity(searchWord)
    }


}