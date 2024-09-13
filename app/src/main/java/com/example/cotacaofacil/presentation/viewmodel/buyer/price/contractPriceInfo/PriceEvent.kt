package com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractPriceInfo

import com.example.cotacaofacil.domain.model.PriceModel
import com.example.cotacaofacil.domain.model.StatusPrice
import com.example.cotacaofacil.domain.model.UserPriceConflict

sealed class PriceEvent {
    object ErrorConflictNotResolved : PriceEvent()
    data class FinishActivity(val message: String) : PriceEvent()
    data class ShowScreenConflict(val priceModel: PriceModel) : PriceEvent()
    data class ShowDialogWarning(val messageDialog: String, val statusPrice : StatusPrice) : PriceEvent()
    data class ShowScreenConflicts(val conflicts: ArrayList<UserPriceConflict>) : PriceEvent()
}