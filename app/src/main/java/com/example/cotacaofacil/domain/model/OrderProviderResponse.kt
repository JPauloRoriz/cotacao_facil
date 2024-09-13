package com.example.cotacaofacil.domain.model

import android.os.Parcelable
import com.example.cotacaofacil.data.model.ProductPriceResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderProviderModel(
    val priceCode : String = "",
    val orderCode : String = "",
    val cnpjProvider : String = "",
    val productsPrice : MutableList<String> = mutableListOf(),
    val isDelayInDelivery : Boolean = false,
    var isResolvedOrder : Boolean = false
): Parcelable