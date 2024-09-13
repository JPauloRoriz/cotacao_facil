package com.example.cotacaofacil.presentation.viewmodel.buyer.price

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cotacaofacil.R
import com.example.cotacaofacil.domain.containsWithoutPrice
import com.example.cotacaofacil.domain.mapper.toProductEditPriceModel
import com.example.cotacaofacil.domain.model.*
import com.example.cotacaofacil.domain.usecase.home.contract.GetImageProfileUseCase
import com.example.cotacaofacil.presentation.viewmodel.base.SingleLiveEvent
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractWinnerPrice.WinnerPriceEvent
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractWinnerPrice.WinnerPriceState
import kotlinx.coroutines.launch

class WinnerPriceViewModel(
    val price: PriceModel,
    private val getImageProfileUseCase: GetImageProfileUseCase,
    val context: Context
) : ViewModel() {
    val stateLiveData = MutableLiveData(WinnerPriceState())
    val eventLiveData = SingleLiveEvent<WinnerPriceEvent>()


    fun startScreen() {
        viewModelScope.launch {
            getImagesUsers()
            getUsersWinners()
        }
    }

    private suspend fun getImagesUsers() {
        price.productsPrice.forEach { productPriceModel ->
            productPriceModel.userWinner?.let {
                getImageProfileUseCase.invoke(it.cnpjProvider).onSuccess { image ->
                    it.imageUser = image
                }
            }
        }
    }

    private fun getUsersWinners() {
        val priceWinnerList = mutableListOf<PriceWinner>()
        val userWinnersList: MutableList<UserPrice?> = mutableListOf()
        val productsEmptyPrice: MutableList<ProductPriceModel> = mutableListOf()

        price.productsPrice.forEach { productPriceModel ->
            if (productPriceModel.userWinner != null) {
                if (userWinnersList.containsWithoutPrice(productPriceModel.userWinner).not()) {
                    userWinnersList.add(productPriceModel.userWinner)
                }
            } else {
                productsEmptyPrice.add(productPriceModel)
            }
        }
        userWinnersList.forEach { userPrice ->
            var valueTotal = 0.0
            priceWinnerList.add(PriceWinner(type = PriceWinnerType.USER_PRICE, userPrice = userPrice))
            price.productsPrice.forEach { productPrice ->
                userPrice?.cnpjProvider?.let { cnpjProvider ->
                    if (productPrice.userWinner?.cnpjProvider == userPrice.cnpjProvider) {
                        val productPriceEditModel = productPrice.toProductEditPriceModel(cnpjProvider)
                        valueTotal += productPriceEditModel.price * productPriceEditModel.quantityProducts
                        priceWinnerList.add(
                            PriceWinner(
                                type = PriceWinnerType.PRODUCT_PRICE,
                                productPrice = productPriceEditModel
                            )
                        )
                    }
                }
            }
            priceWinnerList.add(
                PriceWinner(
                    type = PriceWinnerType.LINE_DIVIDER,
                    textEndLine = context.getString(R.string.value_total_by_product, valueTotal.toString())
                )
            )
        }
        if (priceWinnerList.filter { it.type == PriceWinnerType.PRODUCT_PRICE }.isNotEmpty()) {
            priceWinnerList.add(
                index = 0, PriceWinner(
                    type = PriceWinnerType.TEXT_DESCRIPTION, textDescription = TextDescription(
                        text = context.getString(R.string.title_description_winners),
                        icon = AppCompatResources.getDrawable(context, R.drawable.ic_price_color_primary)
                    )
                )
            )
        }
        if (productsEmptyPrice.isNotEmpty()) {
            priceWinnerList.add(
                PriceWinner(
                    type = PriceWinnerType.TEXT_DESCRIPTION, textDescription = TextDescription(
                        text = context.getString(R.string.product_price_empty),
                        icon = AppCompatResources.getDrawable(context, R.drawable.icon_price_empty)
                    )
                )
            )
            productsEmptyPrice.forEach {
                priceWinnerList.add(PriceWinner(type = PriceWinnerType.PRODUCT_PRICE, it.toProductEditPriceModel("")))
            }
        }
        val textToolbar = context.getString(R.string.price_number, price.code)
        stateLiveData.postValue(stateLiveData.value?.copy(textToolbar = textToolbar, messageError = "", pricesWinners = priceWinnerList, isLoading = false))
    }
}