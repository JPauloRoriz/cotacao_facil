package com.example.cotacaofacil.presentation.viewmodel.buyer.price

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cotacaofacil.R
import com.example.cotacaofacil.domain.findSmallerPrice
import com.example.cotacaofacil.domain.model.*
import com.example.cotacaofacil.domain.usecase.home.contract.GetImageProfileUseCase
import com.example.cotacaofacil.presentation.viewmodel.base.SingleLiveEvent
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractResolveConflict.ResolveConflictEventLiveData
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractResolveConflict.ResolveConflictStateLiveData
import kotlinx.coroutines.launch

class ResolveConflictViewModel(
    private val getImageProfileUseCase: GetImageProfileUseCase,
    private val priceModel: PriceModel,
    private val context: Context
) : ViewModel() {
    val state = MutableLiveData(ResolveConflictStateLiveData())
    val event = SingleLiveEvent<ResolveConflictEventLiveData>()

    private var currentConflict = 1

    fun verifyStatusButton(listUsersConflict: List<UserPriceConflict>) {
        if (listUsersConflict.map { it.isSelect }.contains(true)) {
            state.postValue(
                state.value?.copy(
                    buttonClickable = true,
                    colorBackgroundButton = R.color.colorPrimary,
                    colorTextButton = R.color.white
                )
            )
        } else {
            state.postValue(
                state.value?.copy(
                    buttonClickable = false,
                    colorBackgroundButton = R.color.slow_gray_button,
                    colorTextButton = R.color.black
                )
            )
        }
    }

    private suspend fun getImageUser(cnpj: String, userPriceConflict: UserPriceConflict) {
        getImageProfileUseCase.invoke(cnpj)
            .onSuccess { imageUser ->
                userPriceConflict.image = imageUser
            }
        state.postValue(state.value?.copy(isLoading = false))
    }


    private fun setDataScreen(listUserConflict: ArrayList<UserPriceConflict>) {
        val product = listUserConflict[0].productPriceEditPriceModel
        state.postValue(
            state.value?.copy(
                isLoading = false,
                usersPriceConflict = listUserConflict,
                textNumberConflict = context.getString(
                    R.string.number_conflicts,
                    currentConflict.toString(),
                    getInitTotalConflicts().toString()
                ),
                codeProduct = context.getString(R.string.code_product_buyer, product.productModel.code),
                productName = product.productModel.name,
                productDescription = product.productModel.description.descriptionEmpty(),
                productQuantity = context.getString(R.string.quantity_measurements_editable, product.quantityProducts.toString()),
                valueProduct = context.getString(R.string.value_editable, product.price.toString()),
                valueTotal = product.price.getValueTotal(quantity = product.quantityProducts),
                buttonClickable = false, colorBackgroundButton = R.color.slow_gray_button, colorTextButton = R.color.black,
                textButton = verifyTextButton(),
            )
        )
    }

    private fun verifyTextButton(): String {
        return if (currentConflict != getInitTotalConflicts()) {
            context.getString(R.string.next_conflict)
        } else {
            context.getString(R.string.finish_price)
        }
    }

    private fun Double.getValueTotal(quantity: Int) = context.getString(R.string.value_total_by_product, (this * quantity).toString())
    private fun String.descriptionEmpty() = if (this.isEmpty()) context.getString(R.string.not_description) else this

    fun tapOnNextOrSave(listUsersConflict: List<UserPriceConflict>) {
        val userWinnerFind = listUsersConflict.find { it.isSelect }?.userPrice
        priceModel.getProductConflict()?.userWinner = userWinnerFind
        currentConflict++
        verifyIfExistConflict()
    }

    fun verifyIfExistConflict(): Boolean {
        val usersConflicts = priceModel.getUsersConflicts()
        return if (usersConflicts != null) {
            setDataScreen(listUserConflict = usersConflicts)
            true
        } else {
            event.postValue(ResolveConflictEventLiveData.FinishScreenAndPrice(priceModel = priceModel))
            false
        }
    }

    private fun PriceModel.getUsersConflicts(): ArrayList<UserPriceConflict>? {
        productsPrice.forEach {
            if (it.userWinner == null && it.usersPrice.size > 1) {
                val usersWinners = findSmallerPrice(productPrice = it)
                if (usersWinners.size > 1) {
                    val usersPriceConflicts = createUsersConflict(usersWinners, it)
                    usersPriceConflicts.forEach { userPriceConflict ->
                        viewModelScope.launch { getImageUser(userPriceConflict.cnpjCorporation, userPriceConflict) }
                    }
                    return usersPriceConflicts
                }
            }
        }
        return null
    }

    private fun PriceModel.getProductConflict(): ProductPriceModel? {
        productsPrice.forEach {
            if (it.userWinner == null && it.usersPrice.size > 1) {
                if(findSmallerPrice(it).size > 1){
                    return it
                }
            }
        }
        return null
    }

    private fun getInitTotalConflicts(): Int {
        var totalConflicts = 0
        priceModel.productsPrice.forEach {
            val usersPricesWitSmallerValue = findSmallerPrice(productPrice = it)
            if (usersPricesWitSmallerValue.size > 1) {
                totalConflicts++
            }
        }
        return totalConflicts
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
}