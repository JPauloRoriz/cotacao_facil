package com.example.cotacaofacil.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserPriceConflict(
    var isSelect: Boolean = false,
    val userPrice: UserPrice = UserPrice(),
    var image: String? = null,
    val nameCorporation: String = "",
    val cnpjCorporation: String = "",
    val productPriceEditPriceModel: ProductPriceEditPriceModel,
) : Parcelable