package com.example.cotacaofacil.presentation.viewmodel.buyer.price

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cotacaofacil.R
import com.example.cotacaofacil.data.helper.UserHelper
import com.example.cotacaofacil.domain.exception.DefaultException
import com.example.cotacaofacil.domain.exception.ListEmptyException
import com.example.cotacaofacil.domain.exception.NoConnectionInternetException
import com.example.cotacaofacil.domain.model.*
import com.example.cotacaofacil.domain.usecase.date.contract.DateCurrentUseCase
import com.example.cotacaofacil.domain.usecase.price.contract.EditPriceUseCase
import com.example.cotacaofacil.domain.usecase.price.contract.GetPricesBuyerUserCase
import com.example.cotacaofacil.domain.usecase.price.contract.UpdateHourPricesUseCase
import com.example.cotacaofacil.domain.usecase.price.contract.ValidationStatusPriceUseCase
import com.example.cotacaofacil.presentation.viewmodel.base.SingleLiveEvent
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractPrice.PriceEvent
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractPrice.PriceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PriceBuyerViewModel(
    private val getPricesBuyerUserCase: GetPricesBuyerUserCase,
    private val validationStatusPriceUseCase: ValidationStatusPriceUseCase,
    private val updateHourPricesUseCase: UpdateHourPricesUseCase,
    private val dateCurrentUseCase: DateCurrentUseCase,
    private val editPriceUseCase: EditPriceUseCase,
    private val userHelper: UserHelper,
    private val context: Context,
) : ViewModel() {
    val priceEvent = SingleLiveEvent<PriceEvent>()
    val priceState = MutableLiveData(PriceState())

    init {
        updateListPrices()
    }

    fun updateListPrices() {
        priceState.postValue(priceState.value?.copy(showProgressBar = true))
        viewModelScope.launch(Dispatchers.IO) {
            userHelper.user?.let { userModel ->
                dateCurrentUseCase.invoke().onSuccess { currentDate ->
                    getPricesBuyerUserCase.invoke(userModel.cnpj, userModel.userTypeSelected, userModel, currentDate)
                        .onSuccess { prices ->
                            if (prices.isEmpty()) {
                                priceState.postValue(
                                    priceState.value?.copy(
                                        pricesModel = mutableListOf(),
                                        messageError = context.getString(R.string.price_empty_message_error),
                                        showProgressBar = false
                                    )
                                )
                            } else {
                                validationStatusPriceUseCase.invoke(prices).onSuccess {
                                    priceState.postValue(priceState.value?.copy(pricesModel = prices.sortedByDescending { it.dateStartPrice }
                                        .toMutableList(), messageError = "", showProgressBar = false))
                                }.onFailure {
                                    priceState.postValue(
                                        priceState.value?.copy(
                                            pricesModel = mutableListOf(),
                                            messageError = context.getString(R.string.message_error_default_price),
                                            showProgressBar = false
                                        )
                                    )
                                }
                            }
                        }
                        .onFailure { exception ->
                            when (exception) {
                                is ListEmptyException -> {
                                    priceState.postValue(
                                        priceState.value?.copy(
                                            pricesModel = mutableListOf(),
                                            messageError = context.getString(R.string.price_empty_message_error),
                                            showProgressBar = false
                                        )
                                    )
                                }
                                is DefaultException -> {
                                    priceState.postValue(
                                        priceState.value?.copy(
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
                }
            }
        }
    }

    fun tapOnCreatePrice() {
        priceEvent.postValue(PriceEvent.CreatePrice)
    }

    fun showDialogSuccess(it: String) {
        priceEvent.postValue(PriceEvent.ShowDialogSuccess(it))
    }

    fun tapOnPrice(priceModel: PriceModel) {
        updatePricesHour(priceModel)
        when (priceModel.status) {
            StatusPrice.OPEN -> {
                priceEvent.postValue(PriceEvent.TapOnPriceOpen(priceModel))
            }
            StatusPrice.CANCELED -> {
                priceEvent.postValue(PriceEvent.TapOnPriceCanceled(priceModel))
            }
            StatusPrice.FINISHED -> {
                priceEvent.postValue(PriceEvent.TapOnPriceFinished(priceModel))
            }
            StatusPrice.PENDENCY -> {
                priceEvent.postValue(PriceEvent.TapOnPricePendency(priceModel))
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

    fun cancelOrFinishPrice(statusPrice: StatusPrice, priceModelEdit: PriceModel? = null) {
        priceModelEdit?.let { priceModel ->
            viewModelScope.launch {
                dateCurrentUseCase.invoke()
                    .onSuccess { date ->
                       priceModel.getConflicts()
                        priceModelEdit.apply {
                            status = statusPrice
                            dateFinishPendency = date
                        }
                        editPriceUseCase.invoke(priceModel)
                            .onSuccess {
                                val messageError = context.getString(R.string.finish)
                                updateListPrices()
                                priceEvent.postValue(
                                    PriceEvent.ReolveConflictSuccess(
                                        message = context.getString(R.string.canceled_or_finished_with_success, priceModel.code, messageError)
                                    )
                                )
                            }
                            .onFailure {
                                //todo tratamento de erro
                            }
                    }
                    .onFailure {
                        //todo tratamento de erro
                    }
            }

        }
    }

    private fun PriceModel.getConflicts() {
        this.productsPrice.forEach { productPrice ->
            if (productPrice.userWinner == null) {
                val usersPricesWitSmallerValue = findSmallerPrice(productPrice)
                if (usersPricesWitSmallerValue.size == 1) {
                    productPrice.userWinner = usersPricesWitSmallerValue[0]
                }
            }
        }
    }

    private fun findSmallerPrice(productPrice: ProductPriceModel): ArrayList<UserPrice> {
        val userPrices = arrayListOf<UserPrice>()
        productPrice.usersPrice.forEach { userPrice ->
            if (userPrices.isEmpty()) {
                userPrices.add(userPrice)
                return@forEach
            }
            if (userPrices.isNotEmpty() && userPrice.price < userPrices[0].price) {
                userPrices.clear()
                userPrices.add(userPrice)
                return@forEach
            }
            if (userPrices.isNotEmpty() && userPrice.price == userPrices[0].price) {
                userPrices.add(userPrice)
                return@forEach
            }
        }
        return userPrices
    }

}