package com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractReportInitPrice

import android.graphics.drawable.Drawable
import com.example.cotacaofacil.R

data class StateReportInitPriceLiveData(
    var textValueQuantityProducts: String = "",
    var textValueVariablesProducts: String = "",
    var textDateInitPrice: String = "",
    var textDateFinishPrice: String = "",
    var textDateDeliveryPrice: String = "",
    var textPriorityPrice: String = "",
    var colorPriorityPrice: Drawable? = null,
    var descriptionPrice: String? = "",
    val isLoading : Boolean = false
)