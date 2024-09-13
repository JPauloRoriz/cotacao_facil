package com.example.cotacaofacil.domain.usecase.price.contract

import com.example.cotacaofacil.domain.model.PriceModel

interface UpdateHourPricesUseCase {
    suspend fun invoke(priceModel: PriceModel) : PriceModel?
}