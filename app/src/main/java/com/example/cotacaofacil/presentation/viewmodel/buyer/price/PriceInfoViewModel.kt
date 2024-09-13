package com.example.cotacaofacil.presentation.viewmodel.buyer.price

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cotacaofacil.R
import com.example.cotacaofacil.domain.Extensions.Companion.getCnpjProviders
import com.example.cotacaofacil.domain.mapper.toPriceEditModel
import com.example.cotacaofacil.domain.model.*
import com.example.cotacaofacil.domain.usecase.date.contract.DateCurrentUseCase
import com.example.cotacaofacil.domain.usecase.price.contract.EditPriceUseCase
import com.example.cotacaofacil.domain.usecase.price.contract.GetPriceByCodeUseCase
import com.example.cotacaofacil.presentation.ui.extension.toFormattedDateTime
import com.example.cotacaofacil.presentation.viewmodel.base.SingleLiveEvent
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractPriceInfo.PriceEvent
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractPriceInfo.PriceState
import kotlinx.coroutines.launch

class PriceInfoViewModel(
    private val getPriceByCodeUseCase: GetPriceByCodeUseCase,
    private val codePrice: String,
    private val cnpjBuyerCreator: String,
    private val context: Context,
    private val editPriceUseCase: EditPriceUseCase,
    private val currentDateUseCase: DateCurrentUseCase,

    ) : ViewModel() {
    val stateLiveData = MutableLiveData(PriceState(isLoading = true))
    val eventLiveData = SingleLiveEvent<PriceEvent>()
    private var priceModelInit: PriceModel? = null
    private val conflictsProducts: ArrayList<ArrayList<UserPriceConflict>> = arrayListOf()

    init {
        getPriceModel()
    }

    private fun getPriceModel() {
        viewModelScope.launch {
            currentDateUseCase.invoke().onSuccess { currentDate ->
                getPriceByCodeUseCase.invoke(codePrice = codePrice, cnpjBuyerCreator = cnpjBuyerCreator, currentDate = currentDate)
                    .onSuccess { priceModel ->
                        priceModelInit = priceModel
                        getDataState(priceModel)
                    }
                    .onFailure {

                    }
            }
        }
    }

    private fun getDataState(priceModel: PriceModel) {
        var showBtnFinish = false
        var showBtnCancel = false
        var textFinishPrice = ""
        when (priceModel.status) {
            StatusPrice.OPEN -> {
                if (priceModel.closeAutomatic.not()) {
                    showBtnFinish = true
                    textFinishPrice = context.getString(R.string.price_finish_date, priceModel.dateFinishPrice?.toFormattedDateTime())
                } else {
                    textFinishPrice = context.getString(R.string.finish_price_manual)
                }
                showBtnCancel = true
            }
            StatusPrice.CANCELED -> {
                textFinishPrice = context.getString(R.string.price_canceled_date, priceModel.dateFinishPrice?.toFormattedDateTime())
            }
            StatusPrice.FINISHED -> {
                textFinishPrice = context.getString(R.string.price_finished_date, priceModel.dateFinishPrice?.toFormattedDateTime())
            }
            else -> {

            }
        }
        stateLiveData.postValue(
            stateLiveData.value?.copy(
                showBtnFinishPrice = showBtnFinish,
                showBtnCancelPrice = showBtnCancel,
                isLoading = false,
                productsPrice = priceModel.toPriceEditModel("").productsEdit,
                quantityProducts = priceModel.productsPrice.size.toString(),
                dateInit = context.getString(R.string.date_init_price_adapter_price, priceModel.dateStartPrice.toFormattedDateTime()),
                dateFinish = textFinishPrice,
                quantityProviders = priceModel.getCnpjProviders().size.toString()
            )
        )
    }

    fun tapOnCancelOrFinishPrice(statusPrice: StatusPrice) {
        val messageDialog =
            if (statusPrice == StatusPrice.FINISHED) context.getString(R.string.confirmation_finish_price)
            else context.getString(R.string.confirmation_cancel_price)
        eventLiveData.postValue(PriceEvent.ShowDialogWarning(messageDialog = messageDialog, statusPrice = statusPrice))
    }

    private fun validationPrices(statusPrice: StatusPrice) {
        conflictsProducts.clear()
        priceModelInit?.let { priceModel ->
            val allConflicts = priceModel.getConflicts()
            if (allConflicts.isNotEmpty()) {
                eventLiveData.postValue(PriceEvent.ShowScreenConflict(priceModel = priceModel))
            } else {
                cancelOrFinishPrice(statusPrice)
            }
        }
    }

    fun cancelOrFinishPrice(statusPrice: StatusPrice, priceModelEdit: PriceModel? = null) {
        priceModelEdit?.let { priceModelInit = priceModelEdit }
        priceModelInit?.let { priceModel ->
            viewModelScope.launch {
                currentDateUseCase.invoke()
                    .onSuccess { date ->
                        priceModelInit?.apply {
                            status = statusPrice
                            dateFinishPrice = date
                        }
                        editPriceUseCase.invoke(priceModel = priceModel)
                            .onSuccess {
                                val messageError =
                                    if (statusPrice == StatusPrice.FINISHED) context.getString(R.string.finish) else context.getString(R.string.cancelled)
                                eventLiveData.postValue(
                                    PriceEvent.FinishActivity(
                                        context.getString(R.string.canceled_or_finished_with_success, priceModel.code, messageError)
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

    private fun PriceModel.getConflicts(): ArrayList<ArrayList<UserPriceConflict>> {
        this.productsPrice.forEach { productPrice ->
            if (productPrice.userWinner == null) {
                val usersPricesWitSmallerValue = findSmallerPrice(productPrice)
                if (usersPricesWitSmallerValue.size == 1) {
                    productPrice.userWinner = usersPricesWitSmallerValue[0]
                }
                if (usersPricesWitSmallerValue.size > 1) {
                    val usersConflict = createUsersConflict(usersPricesWitSmallerValue = usersPricesWitSmallerValue, productPrice = productPrice)
                    addConflictToConflicts(usersPriceConflictsList = usersConflict)
                }
            }
        }
        return conflictsProducts
    }

    private fun addConflictToConflicts(
        usersPriceConflictsList: ArrayList<UserPriceConflict>
    ) {
        conflictsProducts.addIfNotExist(conflicts = usersPriceConflictsList)
    }

    private fun createUsersConflict(
        usersPricesWitSmallerValue: ArrayList<UserPrice>,
        productPrice: ProductPriceModel
    ): ArrayList<UserPriceConflict> {
        val listUsersConflict = arrayListOf<UserPriceConflict>()
        usersPricesWitSmallerValue.forEach { userPrice ->
            listUsersConflict.add(
                UserPriceConflict(
                    userPrice = userPrice,
                    nameCorporation = userPrice.nameUser,
                    cnpjCorporation = userPrice.cnpjProvider,
                    productPriceEditPriceModel = ProductPriceEditPriceModel(
                        productModel = productPrice.productModel,
                        price = userPrice.price,
                        isSelected = false,
                        quantityProducts = productPrice.quantityProducts
                    )
                )
            )
        }
        return listUsersConflict
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

    private fun ArrayList<ArrayList<UserPriceConflict>>.addIfNotExist(conflicts: ArrayList<UserPriceConflict>) {
        if (!this.contains(conflicts)) this.add(conflicts)
    }

    fun cancelOrFinish(statusPrice: StatusPrice) {
        if (statusPrice == StatusPrice.FINISHED) {
            validationPrices(statusPrice = statusPrice)
        } else {
            cancelOrFinishPrice(statusPrice = statusPrice)
        }
    }
}
