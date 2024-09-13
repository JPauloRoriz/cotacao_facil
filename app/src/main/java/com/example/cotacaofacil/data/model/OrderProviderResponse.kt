package com.example.cotacaofacil.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderProviderResponse(
    val priceCode : String = "",
    val orderCode : String = "",
    val cnpjProvider : String = "",
    val isResolvedOrder : Boolean = false,
    val productsPrice : MutableList<String> = mutableListOf()
): Parcelable