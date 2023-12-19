package com.android.sitbak.home.order

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.sitbak.R
import com.android.sitbak.activities.order_webview.OrderWebViewActivity
import com.android.sitbak.base.AppController
import com.android.sitbak.databinding.FragmentOrderBinding
import com.android.sitbak.home.active_orders.ActiveOrderFragment
import com.android.sitbak.home.past_orders.PastOrderFragment
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.home.home.HomeActivity
import com.android.sitbak.home.notifications.NotificationActivity
import com.android.sitbak.utils.*
import com.google.android.material.tabs.TabLayout

class OrderFragment : Fragment() {

    private lateinit var binding: FragmentOrderBinding
    private lateinit var viewModel: OrderFragmentVM
    private lateinit var mContext: Context

    private var selectedTabPosition = 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOrderBinding.inflate(inflater, container, false)
        mContext = requireContext()
        addTabs()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVM()
        initViews()
        initListeners()

        binding.fragmentContainer.removeAllViews()
        replaceFragment(selectedTabPosition)
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(OrderFragmentVM::class.java)
        binding.viewModel = viewModel

        binding.executePendingBindings()
        binding.lifecycleOwner = this

    }

    private fun initViews() {
        binding.topBar.ivBack.viewGone()
        binding.topBar.tvTitle.text = mContext.resources.getString(R.string.order_screen_title)

//        if (!AppController.isGuest) {
//            if (!HomeActivity.cartListData.isNullOrEmpty())
//                binding.topBar.ivCart.viewVisible()
//            binding.topBar.ivNotification.viewVisible()
//        }
    }

    private fun initListeners() {
//        binding.topBar.ivNotification.setOnClickListener {
//            AppUtils.preventDoubleClick(binding.topBar.ivNotification)
//            mContext.startActivity(Intent(mContext, NotificationActivity::class.java))
//        }
//
//        binding.topBar.ivCart.setOnClickListener {
//            AppUtils.preventDoubleClick(binding.topBar.ivCart)
//            val intent = Intent(mContext, OrderWebViewActivity::class.java)
//            val accessToken = SharedPreference.getSimpleString(mContext, Constants.accessToken)
//            val url = "http://178.128.29.7/sitbak/web_views_checkout/${HomeActivity.cartListData[0].store_id}/$accessToken"
//            intent.putExtra("url", url)
//            mContext.startActivity(intent)
//        }
    }

    @SuppressLint("InflateParams")
    private fun addTabs() {
        val v1 = LayoutInflater.from(mContext).inflate(R.layout.item_custom_tab_layout, null) as TextView
        v1.text = "Active"
        v1.setTextColor(ContextCompat.getColor(mContext, R.color.app_color))
        v1.textSize = 17f
        val tab1 = binding.tabLayout.newTab().setCustomView(v1)
        binding.tabLayout.addTab(tab1)

        val v2 = LayoutInflater.from(mContext).inflate(R.layout.item_custom_tab_layout, null) as TextView
        v2.text = "Past orders"
        v2.textSize = 17f
        val tab2 = binding.tabLayout.newTab().setCustomView(v2)
        binding.tabLayout.addTab(tab2)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                replaceFragment(tab?.position!!)
//                    binding.ordersViewPager.setCurrentItem(1, false)

                val tabTextView = tab.customView
                tabTextView?.findViewById<TextView>(R.id.tvTabTitle)
                    ?.setTextColor(ContextCompat.getColor(mContext, R.color.app_color))
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val tabTextView = tab?.customView
                tabTextView?.findViewById<TextView>(R.id.tvTabTitle)
                    ?.setTextColor(ContextCompat.getColor(mContext, R.color.text_light_black))
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })

//        val orderPagerAdapter = OrderViewPager(requireActivity())
//        binding.ordersViewPager.adapter = orderPagerAdapter
//        binding.ordersViewPager.isUserInputEnabled = false
//        binding.ordersViewPager.offscreenPageLimit = 1
    }

    private fun replaceFragment(position: Int) {
        selectedTabPosition = position
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
        if (position == 0) {
            activity
                ?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragmentContainer, ActiveOrderFragment(), "ActiveOrderFragment")
                ?.commit()
        } else {
            activity
                ?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragmentContainer, PastOrderFragment(), "PastOrderFragment")
                ?.commit()
        }
    }

    inner class OrderViewPager(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return if (position == 0)
                ActiveOrderFragment()
            else
                PastOrderFragment()
        }

    }

}