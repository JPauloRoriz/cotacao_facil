package com.example.cotacaofacil.domain.usecase.price

import com.example.cotacaofacil.data.repository.price.PriceRepository
import com.example.cotacaofacil.domain.model.PriceModel
import com.example.cotacaofacil.domain.model.UserModel
import com.example.cotacaofacil.domain.usecase.price.contract.GetPricesProviderUseCase

class GetPricesProviderUseCaseImpl(
    private val repository: PriceRepository
) : GetPricesProviderUseCase {
    override suspend fun invoke(cnpj: MutableList<String>, cnpjProvider : String, userModel: UserModel, currentDate: Long): Result<MutableList<PriceModel>> {
        return repository.getPricesProvider(cnpj, cnpjProvider, userModel, currentDate)
    }
}