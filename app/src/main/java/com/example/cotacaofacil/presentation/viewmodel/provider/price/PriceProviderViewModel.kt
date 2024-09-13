package com.example.cotacaofacil.presentation.viewmodel.provider.price

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cotacaofacil.R
import com.example.cotacaofacil.data.helper.UserHelper
import com.example.cotacaofacil.domain.Extensions.Companion.convertCnpj
import com.example.cotacaofacil.domain.exception.DefaultException
import com.example.cotacaofacil.domain.exception.ListEmptyException
import com.example.cotacaofacil.domain.exception.NoConnectionInternetException
import com.example.cotacaofacil.domain.model.PriceModel
import com.example.cotacaofacil.domain.model.StatusPrice
import com.example.cotacaofacil.domain.usecase.date.contract.DateCurrentUseCase
import com.example.cotacaofacil.domain.usecase.partner.contract.GetAllPartnerModelUseCase
import com.example.cotacaofacil.domain.usecase.price.contract.GetPricesProviderUseCase
import com.example.cotacaofacil.domain.usecase.price.contract.UpdateHourPricesUseCase
import com.example.cotacaofacil.domain.usecase.price.contract.ValidationPricesProviderUseCase
import com.example.cotacaofacil.presentation.viewmodel.base.SingleLiveEvent
import com.example.cotacaofacil.presentation.viewmodel.provider.price.contract.pricesProviderContract.PricePartnerEvent
import com.example.cotacaofacil.presentation.viewmodel.provider.price.contract.pricesProviderContract.PricePartnerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PriceProviderViewModel(
    val context: Context,
    val userHelper: UserHelper,
    private val getPricesProviderUseCase: GetPricesProviderUseCase,
    private val validationPricesProviderUseCase: ValidationPricesProviderUseCase,
    private val getAllPartnerModelUseCase: GetAllPartnerModelUseCase,
    private val updateHourPricesUseCase: UpdateHourPricesUseCase,
    private val dateCurrentUseCase: DateCurrentUseCase
) : ViewModel() {

    val stateLiveData = MutableLiveData(PricePartnerState())
    val eventLiveData = SingleLiveEvent<PricePartnerEvent>()
    private var allPrices: MutableList<PriceModel> = mutableListOf()

    init {
        userHelper.user?.cnpj?.let { cnpj ->
            eventLiveData.postValue(PricePartnerEvent.SendCnpjToAdapter(cnpjProvider = cnpj))
        }
        updateListPrices(selectedTabPosition = TAB_PRICES_OPEN)
    }

    fun updateListPrices(selectedTabPosition: Int = TAB_PRICES_OPEN) {
        stateLiveData.postValue(stateLiveData.value?.copy(showProgressBar = true))
        viewModelScope.launch(Dispatchers.IO) {
            userHelper.user?.let { userModel ->
                dateCurrentUseCase.invoke().onSuccess { currentDate ->
                    userModel.id?.let {
                        getAllPartnerModelUseCase.invoke(userModel.userTypeSelected, it, userModel.cnpj)
                    }?.onSuccess { partnersProvider ->
                        getPricesProviderUseCase.invoke(
                            cnpj = partnersProvider.map { it.cnpjCorporation.convertCnpj() }.toMutableList(),
                            cnpjProvider = userModel.cnpj,
                            userModel = userModel,
                            currentDate = currentDate
                        ).onSuccess { prices ->
                            if (prices.isEmpty()) {
                                stateLiveData.postValue(
                                    stateLiveData.value?.copy(
                                        pricesModel = mutableListOf(),
                                        messageError = context.getString(R.string.price_empty_message_error),
                                        showProgressBar = false
                                    )
                                )
                            } else {
                                allPrices = prices
                                validationPricesProviderUseCase.invoke(prices, userModel.cnpj, currentDate)
                                val pricesFilter = initFilterPricesOpen(selectedTabPosition, prices)
                                stateLiveData.postValue(
                                    stateLiveData.value?.copy(
                                        pricesModel = pricesFilter,
                                        messageError = "",
                                        showProgressBar = false
                                    )
                                )
                            }
                        }.onFailure {
                            stateLiveData.postValue(
                                stateLiveData.value?.copy(
                                    pricesModel = mutableListOf(),
                                    messageError = context.getString(R.string.message_error_default_price),
                                    showProgressBar = false
                                )
                            )
                        }
                    }?.onFailure { exception ->
                        stateLiveData.postValue(
                            stateLiveData.value?.copy(
                                pricesModel = mutableListOf(),
                                messageError = context.getString(R.string.message_error_default_price),
                                showProgressBar = false
                            )
                        )
                        when (exception) {
                            is ListEmptyException -> {
                                stateLiveData.postValue(
                                    stateLiveData.value?.copy(
                                        pricesModel = mutableListOf(),
                                        messageError = context.getString(R.string.price_empty_message_error),
                                        showProgressBar = false
                                    )
                                )
                            }
                            is DefaultException -> {
                                stateLiveData.postValue(
                                    stateLiveData.value?.copy(
                                        pricesModel = mutableListOf(),
                                        messageError = context.getString(R.string.message_error_default_price),
                                        showProgressBar = false
                                    )
                                )
                            }
                            is NoConnectionInternetException -> {
                                //todo tratamento para internet
                            }
                        }
                    }
                }.onFailure {
                    stateLiveData.postValue(
                        stateLiveData.value?.copy(
                            pricesModel = mutableListOf(),
                            messageError = context.getString(R.string.message_error_default_price),
                            showProgressBar = false
                        )
                    )
                    //todo tratamento para internet
                }
            }
        }
    }

    fun tapOnPrice(priceModel: PriceModel) {
        updatePricesHour(priceModel)
        val priceUpdate = stateLiveData.value?.pricesModel?.find { it.code == priceModel.code }
        when (priceUpdate?.status) {
            StatusPrice.OPEN -> {
                userHelper.user?.cnpj?.let {
                    eventLiveData.postValue(PricePartnerEvent.TapOnPriceOpen(priceModel, it))
                }
            }
            StatusPrice.CANCELED -> {
                eventLiveData.postValue(PricePartnerEvent.TapOnPriceFinishedOrCanceled(priceModel))
            }
            StatusPrice.FINISHED -> {
                eventLiveData.postValue(PricePartnerEvent.TapOnPriceFinishedOrCanceled(priceModel))
            }
            StatusPrice.PENDENCY -> {
                eventLiveData.postValue(PricePartnerEvent.TapOnPriceFinishedPendency(priceModel))
            }
            else -> {
                stateLiveData.postValue(
                    stateLiveData.value?.copy(
                        pricesModel = mutableListOf(),
                        messageError = context.getString(R.string.message_error_not_find_price),
                        showProgressBar = false
                    )
                )
            }
        }
    }

    private fun updatePricesHour(priceModel: PriceModel) {
        viewModelScope.launch {
            val newPriceModel = updateHourPricesUseCase.invoke(priceModel)
            if (newPriceModel == null) {
                updateListPrices()
            }
        }
    }

    private fun initFilterPricesOpen(tabSelected: Int, prices: MutableList<PriceModel>): MutableList<PriceModel> {
        return if(tabSelected == TAB_PRICES_OPEN) {
            prices.filter { it.status == StatusPrice.OPEN || it.status == StatusPrice.PENDENCY }.toMutableList()
        } else {
            prices
        }
    }

    fun tapOnTab(tabSelected: Int) {
        updateListPrices(selectedTabPosition = tabSelected)
    }

    companion object {
        private const val TAB_PRICES_OPEN = 0
    }

}