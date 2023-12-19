package com.android.sitbak.home.login_info

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.R
import com.android.sitbak.databinding.ActivityLoginInfoBinding
import com.google.android.material.tabs.TabLayout

class LoginInfoActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginInfoBinding
    private lateinit var mContext: Context


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_info)
        mContext = this
        changeStatusBarColor(R.color.app_background)
        blackStatusBarIcons()
        onSetupViewGroup(binding.loginInfoMainConstraint)
        initViews()
        initListeners()
        addTabs()
    }

    private fun initViews() {

    }

    private fun initListeners() {
        binding.topBar.ivBack.setOnClickListener { finish() }
    }

    private fun addTabs() {
        binding.topBar.tvTitle.text = resources.getString(R.string.login_info_screen_title)
        val v1 = LayoutInflater.from(mContext).inflate(R.layout.item_custom_tab_layout, null) as TextView
        v1.text = "Password"
        v1.setTextColor(ContextCompat.getColor(mContext, R.color.app_color))
        v1.textSize = 17f
        val tab1 = binding.tabLayout.newTab().setCustomView(v1)
        binding.tabLayout.addTab(tab1)

        val v2 = LayoutInflater.from(mContext).inflate(R.layout.item_custom_tab_layout, null) as TextView
        v2.text = "Email"
        v2.textSize = 17f
        val tab2 = binding.tabLayout.newTab().setCustomView(v2)
        binding.tabLayout.addTab(tab2)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0)
                    binding.loginInfoViewPager.setCurrentItem(0, false)
                else
                    binding.loginInfoViewPager.setCurrentItem(1, false)

                val tabTextView = tab?.customView
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

        val orderPagerAdapter = LoginInfoViewPager(this)
        binding.loginInfoViewPager.adapter = orderPagerAdapter
        binding.loginInfoViewPager.isUserInputEnabled = false

    }


    inner class LoginInfoViewPager(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return if (position == 0)
                InfoPasswordFragment()
            else
                InfoEmailFragment()
        }
    }


}