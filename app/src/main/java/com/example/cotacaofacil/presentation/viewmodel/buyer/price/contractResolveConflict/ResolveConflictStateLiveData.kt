package com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractResolveConflict

import com.example.cotacaofacil.R
import com.example.cotacaofacil.domain.model.UserPriceConflict

data class ResolveConflictStateLiveData(
    val isLoading : Boolean = false,
    val buttonClickable : Boolean = false,
    val usersPriceConflict : MutableList<UserPriceConflict> = mutableListOf(),
    val textNumberConflict : String = "",
    val codeProduct : String = "",
    val productName : String = "",
    val productDescription : String = "",
    val productQuantity : String = "",
    val valueProduct : String = "",
    val valueTotal : String = "",
    val textButton : String = "",
    val colorBackgroundButton : Int = R.color.colorPrimary,
    val colorTextButton : Int = R.color.white,
    val sizeTotalInitConflicts : Int = 1,
    val sizeCurrentConflict : Int = 1,
)
