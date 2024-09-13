package com.example.cotacaofacil.presentation.viewmodel.provider.price.contract.pricesProviderContract

import com.example.cotacaofacil.domain.model.PriceModel

sealed class PricePartnerEvent {
    data class TapOnPriceOpen(val priceModel: PriceModel, val cnpjUser: String) : PricePartnerEvent()
    data class TapOnPriceFinishedOrCanceled(val priceModel: PriceModel) : PricePartnerEvent()
    data class TapOnPriceFinishedPendency(val priceModel: PriceModel) : PricePartnerEvent()
    data class SendCnpjToAdapter(val cnpjProvider: String) : PricePartnerEvent()
}