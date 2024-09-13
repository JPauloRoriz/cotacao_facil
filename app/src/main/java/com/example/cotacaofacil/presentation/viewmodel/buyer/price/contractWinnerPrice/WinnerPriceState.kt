package com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractWinnerPrice

import com.example.cotacaofacil.domain.model.PriceWinner

data class WinnerPriceState(
    var messageError: String = "",
    val pricesWinners: MutableList<PriceWinner> = mutableListOf(),
    val isLoading : Boolean = true,
    val textToolbar : String = ""
)
