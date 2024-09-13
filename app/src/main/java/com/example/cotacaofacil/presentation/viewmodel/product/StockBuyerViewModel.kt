package com.example.cotacaofacil.presentation.viewmodel.product

import android.content.Context
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cotacaofacil.R
import com.example.cotacaofacil.data.helper.UserHelper
import com.example.cotacaofacil.domain.exception.DefaultException
import com.example.cotacaofacil.domain.exception.ListEmptyException
import com.example.cotacaofacil.domain.model.ProductModel
import com.example.cotacaofacil.domain.usecase.product.contract.ChangeFavoriteProductUseCase
import com.example.cotacaofacil.domain.usecase.product.contract.DeleteProductUseCase
import com.example.cotacaofacil.domain.usecase.product.contract.GetAllByCnpjProductsUseCase
import com.example.cotacaofacil.presentation.viewmodel.base.SingleLiveEvent
import com.example.cotacaofacil.presentation.viewmodel.product.model.StockEvent
import com.example.cotacaofacil.presentation.viewmodel.product.model.StockState
import com.example.cotacaofacil.presentation.viewmodel.product.model.TypeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StockBuyerViewModel(
    private val getAllProductsUseCase: GetAllByCnpjProductsUseCase,
    private val changeFavoriteProductUseCase: ChangeFavoriteProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val userHelper: UserHelper,
    private val context: Context
) : ViewModel() {

    val stateLiveData = MutableLiveData(StockState())
    val eventLiveData = SingleLiveEvent<StockEvent>()

    private val user = userHelper.user
    private var typeFilter = TypeFilter.Code

    init {
        initViewModel(true)
    }

    fun initViewModel(isAllProducts: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            stateLiveData.postValue(stateLiveData.value?.copy(isLoading = true))
            user?.cnpj?.let {
                val listProducts: MutableList<ProductModel>
                getAllProductsUseCase.invoke(it)
                    .onSuccess {
                        listProducts = if (!isAllProducts) it.filter { it.isFavorite }.toMutableList() else it
                        listProducts.sortByDescending { it.date }
                        stateLiveData.postValue(
                            stateLiveData.value?.copy(
                                productsList = listProducts,
                                messageError = "",
                                isLoading = false,
                                showError = View.GONE,
                                showListProducts = View.VISIBLE,
                                hintTextFindProduct = context.getString(R.string.search_hint_code_product)
                            )
                        )
                    }
                    .onFailure { exception ->
                        when (exception) {
                            is ListEmptyException -> {
                                stateLiveData.postValue(
                                    StockState().copy(
                                        isLoading = false,
                                        showError = View.VISIBLE
                                    )
                                )
                                eventLiveData.postValue(StockEvent.StockEmptyEvent)
                            }
                            is DefaultException -> {
                                stateLiveData.postValue(
                                    StockState().copy(
                                        messageError = context.getString(R.string.message_error_impossible_load_products_stock),
                                        showListProducts = View.GONE,
                                        showError = View.VISIBLE,
                                        isLoading = false
                                    )
                                )
                            }

                        }
                    }
            }
        }
    }

    fun tapOnSelectedFilter(optionFilter: Int) {
        if (typeFilter != optionFilter.toTypeSelected()) {
            when (optionFilter) {
                0 -> {
                    typeFilter = TypeFilter.Code
                    hintSelected(typeFilter)
                }
                1 -> {
                    typeFilter = TypeFilter.Name
                    hintSelected(typeFilter)
                }
                2 -> {
                    typeFilter = TypeFilter.Brand
                    hintSelected(typeFilter)
                }
            }
            eventLiveData.postValue(StockEvent.SelectedFilter)
        }
    }

    private fun hintSelected(typeFilter: TypeFilter) {
        when (typeFilter) {
            TypeFilter.Code -> stateLiveData.postValue(stateLiveData.value?.copy(hintTextFindProduct = context.getString(R.string.search_hint_code_product)))
            TypeFilter.Name -> stateLiveData.postValue(stateLiveData.value?.copy(hintTextFindProduct = context.getString(R.string.search_hint_name_product)))
            TypeFilter.Brand -> stateLiveData.postValue(stateLiveData.value?.copy(hintTextFindProduct = context.getString(R.string.search_hint_brand_product)))
        }
    }

    fun performSearch(query: String?, selectedTabPosition: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            user?.cnpj?.let {
                var newListProducts: MutableList<ProductModel>?
                getAllProductsUseCase.invoke(it)
                    .onSuccess { listProducts ->
                        if (query?.isNotEmpty() == true) {
                            newListProducts = if (selectedTabPosition == 1) {
                                listProducts.filter { it.isFavorite }.toMutableList()
                            } else {
                                listProducts
                            }
                            val result = mutableListOf<ProductModel>()
                            newListProducts?.forEach {
                                when (typeFilter) {
                                    TypeFilter.Code -> {
                                        if (it.code.contains(query, true)) {
                                            result.add(it)
                                        }
                                    }
                                    TypeFilter.Name -> {
                                        if (it.name.contains(query, true)) {
                                            result.add(it)
                                        }
                                    }
                                    TypeFilter.Brand -> {
                                        if (it.brand.contains(query, true)) {
                                            result.add(it)
                                        }
                                    }
                                }
                            }
                            stateLiveData.postValue(stateLiveData.value?.copy(productsList = result))
                        } else {
                            if (selectedTabPosition == 1) {
                                val listFavorites = listProducts.filter { it.isFavorite }.toMutableList()
                                stateLiveData.postValue(stateLiveData.value?.copy(productsList = listFavorites))
                            } else {
                                stateLiveData.postValue(stateLiveData.value?.copy(productsList = listProducts))
                            }

                        }
                    }.onFailure {
                        stateLiveData.postValue(stateLiveData.value?.copy(showError = View.VISIBLE, showListProducts = View.GONE))
                    }
            }
        }


    }

    private fun Int.toTypeSelected(): TypeFilter {
        return when (this) {
            0 -> TypeFilter.Code
            1 -> TypeFilter.Name
            2 -> TypeFilter.Brand
            else -> TypeFilter.Code
        }
    }

    fun clickFavorite(productModel: ProductModel) {
        viewModelScope.launch(Dispatchers.IO) {
            changeFavoriteProductUseCase.invoke(productModel)
                .onSuccess {
                    eventLiveData.postValue(stateLiveData.value?.productsList?.let { it1 -> StockEvent.UpdateList(it1) })
                }.onFailure {

                }
        }
    }

    fun tapOnProduct(productModel: ProductModel) {
        eventLiveData.postValue(StockEvent.EditProduct(productModel))
    }

    fun tapOnTrash(productModel: ProductModel?) {
        viewModelScope.launch(Dispatchers.IO) {
            productModel?.let { productModel ->
                deleteProductUseCase.invoke(productModel = productModel)
                    ?.onSuccess {
                        user?.cnpj?.let { it1 ->
                            getAllProductsUseCase.invoke(cnpjUser = it1)
                                .onSuccess {
                                    eventLiveData.postValue(StockEvent.UpdateList(it))
                                    eventLiveData.postValue(StockEvent.FeedbackMessage(context.getString(R.string.product_deleted_success)))
                                }
                                .onFailure {
                                    stateLiveData.postValue(
                                        StockState().copy(
                                            messageError = context.getString(R.string.message_error_impossible_load_products_stock),
                                            showListProducts = View.GONE,
                                            showError = View.VISIBLE,
                                            isLoading = false
                                        )
                                    )
                                }
                        }
                    }
                    ?.onFailure {
                        eventLiveData.postValue(StockEvent.FeedbackMessage(message = context.getString(R.string.impossible_delete_product)))
                    }
            }
        }

    }
}
