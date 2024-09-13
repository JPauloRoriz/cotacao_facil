package com.example.cotacaofacil.presentation.viewmodel.product.model

import com.example.cotacaofacil.domain.model.ProductModel

sealed class StockEvent {
    object StockEmptyEvent : StockEvent()
    data class UpdateList(val products: MutableList<ProductModel>) : StockEvent()
    data class EditProduct(val product: ProductModel) : StockEvent()
    data class FeedbackMessage(val message: String) : StockEvent()

    object SelectedFilter : StockEvent()


}