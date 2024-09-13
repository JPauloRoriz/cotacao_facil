package com.example.cotacaofacil.domain.usecase.price

import com.example.cotacaofacil.data.repository.price.PriceRepository
import com.example.cotacaofacil.domain.model.PriceEditModel
import com.example.cotacaofacil.domain.usecase.historic.contract.AddHistoricUseCase
import com.example.cotacaofacil.domain.usecase.price.contract.SetPricePartnerUseCase

class SetPricePartnerUseCaseUseCaseImpl(
    val repository : PriceRepository,
    val historicUseCase: AddHistoricUseCase
) : SetPricePartnerUseCase {
    override suspend fun invoke(cnpjPartner: String, productsEditPrice: PriceEditModel, nameUser : String, currentDate : Long): Result<Any>  {
        return repository.setPricesPartner(cnpjPartner, productsEditPrice, nameUser, currentDate)
    }
}