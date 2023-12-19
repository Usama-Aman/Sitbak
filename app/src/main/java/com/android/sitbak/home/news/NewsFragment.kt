package com.android.sitbak.home.news

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.sitbak.R
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.FragmentNewsBinding
import com.android.sitbak.home.home.HomeActivity
import com.android.sitbak.home.popups.GuestAccessPopUp
import com.android.sitbak.network.Api
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import com.bumptech.glide.Glide
import com.github.florent37.glidepalette.BitmapPalette.Profile.*
import com.github.florent37.glidepalette.GlidePalette
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import java.util.ArrayList

class NewsFragment : Fragment(), NewsAdapter.NewsItemInterface, GuestAccessPopUp.GuestAccessInterface {

    private lateinit var binding: FragmentNewsBinding
    private lateinit var mContext: Context
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var viewModel: NewsFragmentVM
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>
    private val newsList: MutableList<NewsData> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNewsBinding.inflate(inflater, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        initVM()
        initObservers()
        initViews()
        initAdapter()
        initListeners()

        getNews()
    }

    private fun getNews() {
        apiCall = retrofitClient.getNews()
        viewModel.getNews(apiCall)
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(NewsFragmentVM::class.java)
        binding.viewModel = viewModel

        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initObservers() {
        viewModel.getApiResponse().observe(viewLifecycleOwner, {
            when (it.status) {
                ApiStatus.LOADING -> {
                    Loader.showLoader(mContext)
                    Loader.progressKHUD?.setCancellable {
                        if (this::apiCall.isInitialized)
                            apiCall.cancel()
                    }
                }
                ApiStatus.ERROR -> {
                    Loader.hideLoader()
                    AppUtils.showToast(requireActivity(), it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    Loader.hideLoader()

                    if (binding.pullToRefresh.isRefreshing)
                        binding.pullToRefresh.isRefreshing = false

                    val model = Gson().fromJson(it.data.toString(), NewsModel::class.java)
                    handlerNewsResponse(model.data)
                }
            }
        })
    }

    private fun handlerNewsResponse(data: ArrayList<NewsData>) {

        if (data.isNotEmpty()) {
            newsList.clear()
            newsList.addAll(data.filter { it.is_heading == 0 })

            val headingNews = data.singleOrNull { it.is_heading == 1 }

            if (headingNews != null) {
                binding.ivNewHeader.viewVisible()
                binding.tvNewsHeadline.viewVisible()
                binding.tvDate.viewVisible()
                binding.btnStartShopping.viewVisible()
                binding.tvNewsHeadline.text = headingNews.title
                binding.tvDate.text = headingNews.created_at
                Glide.with(mContext)
                    .load(headingNews.image_path)
                    .listener(
                        GlidePalette.with(headingNews.image_path)
                            .use(MUTED_DARK)
                            .intoTextColor(binding.tvDate)
                            .intoTextColor(binding.tvNewsHeadline)
                    )
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(binding.ivNewHeader)
            } else {
                binding.ivNewHeader.viewGone()
                binding.tvNewsHeadline.viewGone()
                binding.tvDate.viewGone()
                binding.tvNewsHeadline.viewGone()
                binding.btnStartShopping.viewGone()
            }

            newsAdapter.notifyDataSetChanged()

            if (newsList.isNotEmpty()) {
                binding.llNoData.viewGone()
                binding.newsRecyclerView.viewVisible()
            } else {
                binding.llNoData.viewVisible()
                binding.newsRecyclerView.viewGone()
            }


        } else {
            binding.llNoData.viewVisible()
            binding.newsRecyclerView.viewGone()
        }
    }


    private fun initViews() {
        Boom(binding.btnStartShopping)
        binding.topBarLayout.ivBack.viewGone()
        binding.topBarLayout.tvTitle.text = mContext.resources.getString(R.string.news_screen_title)
    }

    private fun initAdapter() {
        newsAdapter = NewsAdapter(this, newsList)
        binding.newsRecyclerView.layoutManager = LinearLayoutManager(mContext)
        binding.newsRecyclerView.adapter = newsAdapter
    }

    private fun initListeners() {
        binding.btnStartShopping.setOnClickListener {
            (activity as HomeActivity).gotoShopping()
        }

        binding.pullToRefresh.setOnRefreshListener {
            getNews()
        }
    }

    override fun onNewsItemClicked(position: Int) {
        val intent = Intent(mContext, NewsDetailActivity::class.java)
        intent.putExtra("newsHeadline", newsList[position].title)
        intent.putExtra("newsImagePath", newsList[position].image_path)
        intent.putExtra("newDetails", newsList[position].detail)
        intent.putExtra("newsDate", newsList[position].created_at)
        startActivity(Intent(intent))
    }

    private fun showGuestAccess() {
        val dialog = GuestAccessPopUp(this)
        dialog.show(activity?.supportFragmentManager!!, "GuestAccessPopUp")
    }

    override fun guestClickedLogin() {
        (activity as HomeActivity).gotoBoarding()
    }


}