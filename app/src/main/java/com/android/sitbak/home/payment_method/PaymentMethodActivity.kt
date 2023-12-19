package com.android.sitbak.home.payment_method

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.base.BaseActivity
import com.android.sitbak.R
import com.android.sitbak.base.ViewModelFactory
import com.android.sitbak.databinding.ActivityPaymentMethodsBinding
import com.android.sitbak.home.bottom_sheets.AddNewCardBottomSheetFragment
import com.android.sitbak.network.Api
import com.android.sitbak.network.ApiTags
import com.android.sitbak.network.RetrofitClient
import com.android.sitbak.utils.*
import com.astritveliu.boom.Boom
import com.google.gson.Gson
import me.everything.android.ui.overscroll.HorizontalOverScrollBounceEffectDecorator
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter
import okhttp3.ResponseBody
import retrofit2.Call

class PaymentMethodActivity : BaseActivity(), PaymentMethodAdapter.AddCardInterface, DeleteCardDialog.DeleteCard,
    AddNewCardBottomSheetFragment.PaymentCardInterface {

    private lateinit var binding: ActivityPaymentMethodsBinding
    private lateinit var paymentMethodAdapter: PaymentMethodAdapter
    private lateinit var mContext: Context
    private lateinit var viewModel: PaymentMethodVM
    private lateinit var retrofitClient: Api
    private lateinit var apiCall: Call<ResponseBody>

    private val paymentCardList: MutableList<PaymentCardData> = ArrayList()
    private var selectedCardPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment_methods)
        mContext = this
        retrofitClient = RetrofitClient.getClient(mContext).create(Api::class.java)

        changeStatusBarColor(R.color.app_background)
        blackStatusBarIcons()

        initVM()
        initObservers()
        initViews()
        initListeners()
        initAdapter()

        getUserCards()
    }


    private fun initVM() {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(PaymentMethodVM::class.java)
        binding.viewModel = viewModel

        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private fun initObservers() {
        viewModel.getApiResponse().observe(this, {
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
                    AppUtils.showToast(this, it.message!!, false)
                }
                ApiStatus.SUCCESS -> {
                    Loader.hideLoader()
                    when (it.tag) {
                        ApiTags.ADD_USER_CARD -> {
                            AppUtils.showToast(this, it.data?.getString("message")!!, true)
                            Handler(Looper.getMainLooper()).postDelayed({
                                getUserCards()
                            }, 1000)
                        }
                        ApiTags.DELETE_USER_CARD -> {
                            AppUtils.showToast(this, it.data?.getString("message")!!, true)
                            Handler(Looper.getMainLooper()).postDelayed({
                                getUserCards()
                            }, 1000)
                        }
                        ApiTags.GET_USER_CARDS -> {
                            val model = Gson().fromJson(it.data.toString(), PaymentCardModel::class.java)
                            handlerUserCardsResponse(model.data)
                        }
                        ApiTags.UPDATE_USER_DEFAULT_CARD -> {
                            AppUtils.showToast(this, it.data?.getString("message")!!, true)
                        }
                    }
                }
            }
        })
    }

    private fun handlerUserCardsResponse(data: List<PaymentCardData>) {
        paymentCardList.clear()

        if (data.isNotEmpty()) {
            paymentCardList.addAll(data)
            paymentCardList.add(
                PaymentCardData(
                    "", 0, 0, 0, 0, "", true
                )
            )
            paymentMethodAdapter.notifyDataSetChanged()

        } else {
            paymentCardList.add(
                PaymentCardData(
                    "", 0, 0, 0, 0, "", true
                )
            )
            paymentMethodAdapter.notifyDataSetChanged()
        }

    }

    private fun initViews() {
        Boom(binding.btnChooseCard)
        binding.topBar.tvTitle.text = resources.getString(R.string.payment_methods_screen_title)
    }

    private fun initListeners() {
        binding.topBar.ivBack.viewVisible()
        binding.topBar.ivBack.setOnClickListener {
            finish()
        }

        binding.btnChooseCard.setOnClickListener {
            apiCall = retrofitClient.updateUserDefaultCard(paymentCardList[selectedCardPosition]?.id!!)
            viewModel.updateUserDefaultCard(apiCall)
        }

    }

    private fun initAdapter() {
        paymentMethodAdapter = PaymentMethodAdapter(this, paymentCardList)
        binding.paymentMethodRecyclerView.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        binding.paymentMethodRecyclerView.adapter = paymentMethodAdapter

        HorizontalOverScrollBounceEffectDecorator(RecyclerViewOverScrollDecorAdapter(binding.paymentMethodRecyclerView))
    }

    private fun getUserCards() {
        apiCall = retrofitClient.getUserCards()
        viewModel.getUserCards(apiCall)
    }

    override fun onAddCardClicked() {
        val dialog = AddNewCardBottomSheetFragment(this)
        dialog.show(supportFragmentManager, "AddNewCardBottomSheet")
    }

    override fun deleteCard(position: Int) {
        val dialog = DeleteCardDialog(this, position, paymentCardList[position])
        dialog.show(supportFragmentManager, "DeleteCard")
    }

    override fun selectCard(position: Int) {
        selectedCardPosition = position

        runOnUiThread {
            binding.btnChooseCard.visibility = View.VISIBLE
            binding.btnChooseCard.text = mContext.resources.getString(R.string.make_it_default_card)
        }

        paymentMethodAdapter.notifyDataSetChanged()
    }

    override fun addCard(token: String) {
        apiCall = retrofitClient.addUserCard(token)
        viewModel.addUserCard(apiCall)
        binding.btnChooseCard.viewGone()
    }

    override fun deleteCardDialog(position: Int) {
        apiCall = retrofitClient.deleteUserCard(paymentCardList[position].id)
        viewModel.deleteUserCard(apiCall)
    }
}