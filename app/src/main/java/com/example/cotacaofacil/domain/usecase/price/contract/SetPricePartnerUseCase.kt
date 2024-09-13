package com.example.cotacaofacil.domain.usecase.price.contract

import com.example.cotacaofacil.domain.model.PriceEditModel

interface SetPricePartnerUseCase {
    suspend fun invoke(cnpjPartner: String, productsEditPrice: PriceEditModel, nameUser : String, currentDate : Long): Result<Any>
}