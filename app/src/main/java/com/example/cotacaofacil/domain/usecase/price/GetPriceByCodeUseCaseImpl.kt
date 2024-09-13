package com.example.cotacaofacil.domain.usecase.price

import com.example.cotacaofacil.data.repository.price.PriceRepository
import com.example.cotacaofacil.domain.model.PriceModel
import com.example.cotacaofacil.domain.usecase.price.contract.GetPriceByCodeUseCase

class GetPriceByCodeUseCaseImpl(
    private val repository: PriceRepository
) : GetPriceByCodeUseCase {
    override suspend fun invoke(codePrice : String, cnpjBuyerCreator : String, currentDate : Long): Result<PriceModel> {
      return  repository.getPriceByCode(priceCode = codePrice, cnpjBuyerCreator = cnpjBuyerCreator, currentDate = currentDate)
    }
}