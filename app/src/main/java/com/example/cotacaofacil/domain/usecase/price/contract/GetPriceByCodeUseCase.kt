package com.example.cotacaofacil.domain.usecase.price.contract

import com.example.cotacaofacil.domain.model.PriceModel

interface GetPriceByCodeUseCase {
    suspend fun invoke(codePrice : String, cnpjBuyerCreator : String, currentDate : Long): Result<PriceModel>
}