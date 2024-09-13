package com.example.cotacaofacil.data.repository.price

import com.example.cotacaofacil.domain.model.PriceEditModel
import com.example.cotacaofacil.domain.model.PriceModel
import com.example.cotacaofacil.domain.model.UserModel
import com.example.cotacaofacil.presentation.viewmodel.register.model.UserTypeSelected

interface PriceRepository {
    suspend fun savePrice(priceModel: PriceModel) : Result<String>
    suspend fun editPrice(priceModel: PriceModel) : Result<String>
    suspend fun getPricesByCnpj(cnpjUser: String, userTypeSelected: UserTypeSelected, userModel: UserModel, currentDate: Long): Result<MutableList<PriceModel>>

    suspend fun getPricesProvider(cnpjBuyers: MutableList<String>, cnpjProvider : String, userModel: UserModel, currentDate: Long) : Result<MutableList<PriceModel>>
    suspend fun setPricesPartner(cnpjPartner: String, productsEditPrice: PriceEditModel, nameUser : String, currentDate: Long): Result<Any>
    suspend fun getPriceByCode(priceCode : String, cnpjBuyerCreator : String, currentDate: Long) : Result <PriceModel>
}