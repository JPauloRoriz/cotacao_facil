package com.example.cotacaofacil.domain.usecase.price

import com.example.cotacaofacil.data.repository.price.PriceRepository
import com.example.cotacaofacil.domain.model.PriceModel
import com.example.cotacaofacil.domain.model.UserModel
import com.example.cotacaofacil.domain.usecase.price.contract.GetPricesBuyerUserCase
import com.example.cotacaofacil.presentation.viewmodel.register.model.UserTypeSelected

class GetPricesBuyerUserCaseImpl(
    private val repository: PriceRepository
) : GetPricesBuyerUserCase {
    override suspend fun invoke(cnpjUser: String, userTypeSelected: UserTypeSelected, userModel: UserModel, currentDate: Long): Result<MutableList<PriceModel>> {
        return repository.getPricesByCnpj(cnpjUser, userTypeSelected, userModel, currentDate)
    }
}