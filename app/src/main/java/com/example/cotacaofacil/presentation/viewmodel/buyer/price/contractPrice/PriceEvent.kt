package com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractPrice

import com.example.cotacaofacil.domain.model.PriceModel

sealed class PriceEvent {
    object CreatePrice : PriceEvent()
    data class ShowDialogSuccess(val code : String) : PriceEvent()
    data class TapOnPriceOpen(val priceModel: PriceModel) : PriceEvent()
    data class TapOnPriceCanceled(val priceModel: PriceModel) : PriceEvent()
    data class TapOnPriceFinished(val priceModel: PriceModel) : PriceEvent()
    data class TapOnPricePendency(val priceModel: PriceModel) : PriceEvent()
    data class ReolveConflictSuccess(val message: String) : PriceEvent()
}