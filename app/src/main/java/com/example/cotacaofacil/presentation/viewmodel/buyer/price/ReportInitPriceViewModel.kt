package com.example.cotacaofacil.presentation.viewmodel.buyer.price

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cotacaofacil.R
import com.example.cotacaofacil.data.helper.UserHelper
import com.example.cotacaofacil.domain.exception.NotCreatePriceException
import com.example.cotacaofacil.domain.mapper.toProductEditPrice
import com.example.cotacaofacil.domain.model.PriceModel
import com.example.cotacaofacil.domain.model.ProductPriceEditPriceModel
import com.example.cotacaofacil.domain.model.TypeHistory
import com.example.cotacaofacil.domain.usecase.historic.contract.AddHistoricUseCase
import com.example.cotacaofacil.domain.usecase.price.contract.CreatePriceUseCase
import com.example.cotacaofacil.presentation.ui.extension.*
import com.example.cotacaofacil.presentation.viewmodel.base.SingleLiveEvent
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractReportInitPrice.EventReportInitPriceLiveData
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractReportInitPrice.StateReportInitPriceLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReportInitPriceViewModel(
    private val priceModel: PriceModel,
    private val createPriceUseCase: CreatePriceUseCase,
    private val historicUseCase: AddHistoricUseCase,
    private val context: Context,
    private val userHelper: UserHelper
) : ViewModel() {
    val stateLiveData = MutableLiveData(StateReportInitPriceLiveData())
    val eventLiveData = SingleLiveEvent<EventReportInitPriceLiveData>()

    init {
        eventLiveData.postValue(EventReportInitPriceLiveData.UpdateListProducts(priceModel.productsPrice.toProductEditPrice("")))
        stateLiveData.postValue(
            stateLiveData.value?.copy(
                textValueVariablesProducts = priceModel.productsPrice.size.toString(),
                textValueQuantityProducts = priceModel.productsPrice.size.toString(),
                textDateDeliveryPrice = priceModel.deliveryDate.toFormattedDateTime(),
                textDateInitPrice = priceModel.dateStartPrice.toFormattedDateTime(),
                textDateFinishPrice = priceModel.dateFinishPrice.dateEmpty(context, priceModel.closeAutomatic)
                    ?: context.getString(R.string.default_date),
                colorPriorityPrice = priceModel.priority.toColorPriority(context) ?: ContextCompat.getDrawable(context, R.drawable.ic_ball_yellow),
                textPriorityPrice = priceModel.priority.toTextPriority(context),
                descriptionPrice = priceModel.description.notEmpty(context)
            )
        )
    }

    fun updateValuesTotal(productPriceModelList: MutableList<ProductPriceEditPriceModel>) {
        var count = 0
        productPriceModelList.forEach {
            count += it.quantityProducts
        }
        stateLiveData.postValue(stateLiveData.value?.copy(textValueQuantityProducts = count.toString()))
    }

    fun tapOnInitPrice() {
        stateLiveData.postValue(stateLiveData.value?.copy(isLoading = true))
        viewModelScope.launch(Dispatchers.IO) {
            createPriceUseCase.invoke(priceModel)
                .onSuccess { codePrice ->
                    stateLiveData.postValue(stateLiveData.value?.copy(isLoading = false))
                    historicUseCase.addHistoric(priceModel.dateStartPrice, priceModel.cnpjBuyerCreator, codePrice, typeHistory = TypeHistory.CREATE_PRICE)
                    eventLiveData.postValue(EventReportInitPriceLiveData.SuccessAddPrice(codePrice))
                }
                .onFailure {
                    stateLiveData.postValue(stateLiveData.value?.copy(isLoading = false))
                    when (it) {
                        is NotCreatePriceException -> {}
                        else -> {}
                    }
                }
        }

    }
}