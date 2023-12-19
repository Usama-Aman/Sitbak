package com.android.sitbak.home.past_orders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.sitbak.activities.order_webview.OrderWebViewActivity
import com.android.sitbak.base.AppController
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.FragmentPastOrderBinding
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call


class PastOrderFragment : Fragment(), PastOrderAdapter.PastOrderInterface {

    private lateinit var binding: FragmentPastOrderBinding
    private lateinit var pastOrderAdapter: PastOrderAdapter
    private lateinit var mContext: Context
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>
    private lateinit var viewModel: PastOrderFragmentVM
    private val pastOrdersList: MutableList<PastOrdersData?> = ArrayList()

    private var skip = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPastOrderBinding.inflate(inflater, container, false)
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

        getPastOrders()
    }

    private fun getPastOrders() {
        apiCall = retrofitClient.getOrders("past", skip)
        viewModel.getOrders(apiCall)
    }

    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(PastOrderFragmentVM::class.java)
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
                        apiCall.cancel()
                    }
                }
                ApiStatus.ERROR -> {
                    Loader.hideLoader()
                    AppUtils.showToast(requireActivity(), it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.GET_PAST_ORDERS -> {
                            val model = Gson().fromJson(it.data.toString(), PastOrdersModel::class.java)
                            handlePastOrderResponse(model.data)
                        }
                        ApiTags.SET_FAV_ORDER -> {
                            AppUtils.showToast(requireActivity(), it.data?.getString("message")!!, true)
                            pastOrdersList.sortByDescending { it?.is_favourite == 1 }
                            pastOrderAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        })
    }

    private fun handlePastOrderResponse(data: ArrayList<PastOrdersData>) {
        if (binding.pullToRefresh.isRefreshing)
            binding.pullToRefresh.isRefreshing = false

        if (pastOrdersList.size > 0)
            pastOrdersList.removeAt(pastOrdersList.size - 1)

        pastOrdersList.addAll(data)

        if (data.size >= AppController.pageCount) {
            skip += AppController.pageCount
            pastOrdersList.add(null)
        }


        if (pastOrdersList.isEmpty()) {
            binding.pastOrderRecyclerView.viewGone()
            binding.llNoData.viewVisible()
        } else {
            binding.llNoData.viewGone()
            binding.pastOrderRecyclerView.viewVisible()
        }

        pastOrderAdapter.notifyDataSetChanged()

    }


    private fun initViews() {

    }

    private fun initListeners() {

        binding.pullToRefresh.setOnRefreshListener {
            pastOrdersList.clear()
            skip = 0
            getPastOrders()
        }

    }

    private fun initAdapter() {
        pastOrderAdapter = PastOrderAdapter(this, pastOrdersList)
        val lm = LinearLayoutManager(mContext)
        binding.pastOrderRecyclerView.layoutManager = lm
        binding.pastOrderRecyclerView.adapter = pastOrderAdapter
    }

    override fun onStarClicked(position: Int) {
        pastOrdersList[position]?.is_favourite = if (pastOrdersList[position]?.is_favourite == 0) 1 else 0
        apiCall = retrofitClient.setFavouriteOrder(pastOrdersList[position]?.id!!)
        viewModel.setFavouriteOrder(apiCall)
    }

    override fun orderAgainClicked(position: Int) {
        val intent = Intent(mContext, OrderWebViewActivity::class.java)
        val accessToken = SharedPreference.getSimpleString(mContext, Constants.accessToken)
        val url = "http://178.128.29.7/sitbak/web_views_product/${
            pastOrdersList[position]?.store?.id
        }/${pastOrdersList[position]?.product_id.toString()}/$accessToken"
        intent.putExtra("url", url)
        mContext.startActivity(intent)
    }

    override fun onOrderDetailClicked(position: Int) {
        val intent = Intent(mContext, OrderWebViewActivity::class.java)
        val accessToken = SharedPreference.getSimpleString(mContext, Constants.accessToken)
        val url = "http://178.128.29.7/sitbak/web_views_order_details/${pastOrdersList[position]?.id}/$accessToken"
        intent.putExtra("url", url)
        mContext.startActivity(intent)
    }

    override fun onLoadMoreClicked() {
        getPastOrders()
    }

}
