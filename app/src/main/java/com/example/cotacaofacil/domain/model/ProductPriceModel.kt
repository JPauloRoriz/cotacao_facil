package com.example.cotacaofacil.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductPriceModel(
     var productModel: ProductModel = ProductModel(),
     var usersPrice: MutableList<UserPrice> = mutableListOf(),
     var isSelected : Boolean = false,
     var quantityProducts : Int = 1,
     var userWinner : UserPrice? = null,
) : Parcelable