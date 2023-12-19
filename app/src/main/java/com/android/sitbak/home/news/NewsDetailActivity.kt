package com.android.sitbak.home.news

import android.content.Context
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.android.sitbak.R
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.databinding.ActivityHomeBinding
import com.android.sitbak.databinding.ActivityNewsDetailsBinding
import com.android.sitbak.network.Api
import com.android.sitbak.network.RetrofitClient
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_news_details.*

class NewsDetailActivity : BaseActivity() {

    private lateinit var mContext: Context
    private lateinit var binding: ActivityNewsDetailsBinding

    private var newsDetail = ""
    private var newsImagePath = ""
    private var newsDate = ""
    private var newsHeadLine = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_details)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_news_details)
        mContext = this

        changeStatusBarColor(R.color.app_background)
        blackStatusBarIcons()
        initViews()
    }

    private fun initViews() {
        newsHeadLine = intent?.getStringExtra("newsHeadline")!!
        newsImagePath = intent?.getStringExtra("newsImagePath")!!
        newsDetail = intent?.getStringExtra("newDetails")!!
        newsDate = intent?.getStringExtra("newsDate")!!

        binding.topBar.tvTitle.text = mContext.resources.getString(R.string.news_detail_screen_title)

        Glide.with(mContext)
            .load(newsImagePath)
            .placeholder(R.drawable.new_item)
            .into(binding.ivNewsImage)

        binding.tvNewsHeadline.text = newsHeadLine
        binding.tvNewsDate.text = newsDate
        binding.tvNewsSubHeadline.text = newsDetail


        binding.topBar.ivBack.setOnClickListener {
            finish()
        }
    }

}