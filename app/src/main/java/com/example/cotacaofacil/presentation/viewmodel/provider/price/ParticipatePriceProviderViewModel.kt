package com.example.cotacaofacil.presentation.viewmodel.provider.price

import android.content.Context
import android.text.Editable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cotacaofacil.R
import com.example.cotacaofacil.data.helper.UserHelper
import com.example.cotacaofacil.domain.model.PriceEditModel
import com.example.cotacaofacil.domain.model.ProductPriceEditPriceModel
import com.example.cotacaofacil.domain.usecase.date.contract.DateCurrentUseCase
import com.example.cotacaofacil.domain.usecase.price.contract.SetPricePartnerUseCase
import com.example.cotacaofacil.presentation.viewmodel.base.SingleLiveEvent
import com.example.cotacaofacil.presentation.viewmodel.provider.price.contract.participatePricesProviderContract.ParticipatePriceEvent
import com.example.cotacaofacil.presentation.viewmodel.provider.price.contract.participatePricesProviderContract.ParticipatePriceState
import kotlinx.coroutines.launch

class ParticipatePriceProviderViewModel(
    val context: Context,
    val userHelper: UserHelper,
    private val priceEditModel: PriceEditModel,
    private val setPricePartnerUseCase: SetPricePartnerUseCase,
    private val dateCurrentUseCase: DateCurrentUseCase,
) : ViewModel() {
    private val initStatePriceEditModel = priceEditModel.productsEdit.map { it.price }
    val stateLiveData = MutableLiveData(ParticipatePriceState())
    val eventLiveData = SingleLiveEvent<ParticipatePriceEvent>()

    init {
        stateLiveData.postValue(
            stateLiveData.value?.copy(
                productsPricesModel = priceEditModel.productsEdit,
                isLoading = false,
                showError = false,
                textViewCodePrice = context.getString(R.string.price_number, priceEditModel.code),
                productEditSelect = priceEditModel.productsEdit.first(),
            )
        )
    }

    fun tapOnProductPriceModel(productPriceModel: ProductPriceEditPriceModel) {
        setProductSelect(productPriceModel = productPriceModel)
    }

    fun tapOnRegisterPrices() {
        if (initStatePriceEditModel == stateLiveData.value?.productsPricesModel?.map { it.price }) {
            eventLiveData.postValue(ParticipatePriceEvent.ErrorSetPrice(context.getString(R.string.error_edit_price)))
        } else {
            viewModelScope.launch {
                dateCurrentUseCase.invoke()
                    .onSuccess { currentDate ->
                        userHelper.user?.let { user ->
                            val dateFinish = priceEditModel.dateFinishPrice
                            if ((dateFinish == null || dateFinish == -1L) || currentDate < dateFinish) {
                                setPricePartnerUseCase.invoke(cnpjPartner = user.cnpj, productsEditPrice = priceEditModel,nameUser = user.nameCorporation, currentDate)
                                    .onSuccess {
                                        val messageSuccess = context.getString(R.string.edit_price_success, stateLiveData.value?.textViewCodePrice)
                                        eventLiveData.postValue(ParticipatePriceEvent.EditPriceSuccess(messageSuccess))
                                    }.onFailure {
                                        //todo tratamento de erro editar preços
                                    }
                            } else {
                                //todo tratamento encerrar a cotação pelo prazo esgotado
                            }
                        }
                    }
                    .onFailure {
                        //todo tratamento de erro buscar data atual
                    }

            }
        }
    }

    fun tapOnArrowLeft(positionSelect: Int, listTableProduct: MutableList<ProductPriceEditPriceModel>) {
        if (positionSelect > 0) {
            setProductSelect(productPriceModel = listTableProduct[positionSelect - 1])
        }
    }

    fun tapOnArrowRight(positionSelect: Int, listTableProduct: MutableList<ProductPriceEditPriceModel>) {
        if (positionSelect < listTableProduct.size - 1) {
            setProductSelect(productPriceModel = listTableProduct[positionSelect + 1])
        }
    }

    private fun setProductSelect(productPriceModel: ProductPriceEditPriceModel) {
        stateLiveData.postValue(stateLiveData.value?.copy(productEditSelect = productPriceModel))
    }

    fun editProduct(text: Editable?, listTableProduct: MutableList<ProductPriceEditPriceModel>, positionSelect: Int) {
        if (listTableProduct.size > 0) {
            val productEditPrice = listTableProduct[positionSelect]
            productEditPrice.price = text.toString().toDoubleOrNull() ?: 0.0
        }
    }
}