package com.example.cotacaofacil.domain.model

import android.graphics.drawable.Drawable

data class PriceWinner(
    val type: PriceWinnerType,
    val productPrice: ProductPriceEditPriceModel? = null,
    val userPrice: UserPrice? = null,
    val textDescription: TextDescription? = null,
    val textEndLine: String = "",
)

data class TextDescription(
    val text : String = "",
    val icon : Drawable? = null
)

enum class PriceWinnerType {
    PRODUCT_PRICE,
    USER_PRICE,
    LINE_DIVIDER,
    TEXT_DESCRIPTION
}