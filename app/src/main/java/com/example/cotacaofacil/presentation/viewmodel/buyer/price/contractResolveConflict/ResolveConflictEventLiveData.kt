package com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractResolveConflict

import com.example.cotacaofacil.domain.model.PriceModel

sealed class ResolveConflictEventLiveData {
    data class FinishActivityError(val message : String) : ResolveConflictEventLiveData()
    data class FinishScreenAndPrice(val priceModel: PriceModel) : ResolveConflictEventLiveData()
}