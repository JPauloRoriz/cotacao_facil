package com.example.cotacaofacil.domain.usecase.price.contract

import com.example.cotacaofacil.domain.model.PriceModel
import com.example.cotacaofacil.domain.model.UserModel
import com.example.cotacaofacil.presentation.viewmodel.register.model.UserTypeSelected

interface GetPricesBuyerUserCase {
    suspend fun invoke(cnpjUser: String, userTypeSelected: UserTypeSelected, userModel: UserModel, currentDate: Long) : Result<MutableList<PriceModel>>
}