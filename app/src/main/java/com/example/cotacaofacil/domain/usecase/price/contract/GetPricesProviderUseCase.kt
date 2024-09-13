package com.example.cotacaofacil.domain.usecase.price.contract

import com.example.cotacaofacil.domain.model.PriceModel
import com.example.cotacaofacil.domain.model.UserModel
import com.example.cotacaofacil.presentation.viewmodel.register.model.UserTypeSelected

interface GetPricesProviderUseCase {
    suspend fun invoke(cnpj: MutableList<String>, cnpjProvider : String, userModel: UserModel, currentDate: Long): Result<MutableList<PriceModel>>
}