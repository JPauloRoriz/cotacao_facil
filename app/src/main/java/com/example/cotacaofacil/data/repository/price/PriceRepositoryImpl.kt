package com.example.cotacaofacil.data.repository.price

import com.example.cotacaofacil.data.model.ProductPriceResponse
import com.example.cotacaofacil.data.model.util.toPriceModel
import com.example.cotacaofacil.data.model.util.toPriceResponse
import com.example.cotacaofacil.data.service.price.contract.PriceService
import com.example.cotacaofacil.domain.Extensions.Companion.toProductModel
import com.example.cotacaofacil.domain.exception.DefaultException
import com.example.cotacaofacil.domain.exception.PriceNotEdit
import com.example.cotacaofacil.domain.exception.PriceNotFindException
import com.example.cotacaofacil.domain.model.*
import com.example.cotacaofacil.presentation.viewmodel.register.model.UserTypeSelected

class PriceRepositoryImpl(
    private val priceService: PriceService
) : PriceRepository {
    override suspend fun savePrice(priceModel: PriceModel): Result<String> {
        return priceService.savePrice(priceModel.toPriceResponse())
    }

    override suspend fun editPrice(priceModel: PriceModel): Result<String> {
        return priceService.editPrice(priceModel.toPriceResponse())
    }

    override suspend fun getPricesByCnpj(
        cnpjUser: String,
        userTypeSelected: UserTypeSelected,
        userModel: UserModel,
        currentDate: Long
    ): Result<MutableList<PriceModel>> {
        val pricesModel: MutableList<PriceModel> = mutableListOf()
        val result = priceService.getPricesByCnpj(cnpjUser, currentDate)
        if (result.isSuccess) {
            result.map {
                it.forEach { priceResponse ->
                    pricesModel.add(
                        PriceModel(
                            code = priceResponse.code,
                            productsPrice = productResponseToProductModelList(priceResponse.productsPrice),
                            partnersAuthorized = priceResponse.partnersAuthorized,
                            dateFinishPrice = priceResponse.dateFinishPrice,
                            dateStartPrice = priceResponse.dateStartPrice,
                            priority = priceResponse.priority,
                            cnpjBuyerCreator = priceResponse.cnpjBuyerCreator,
                            closeAutomatic = priceResponse.closeAutomatic,
                            allowAllProvider = priceResponse.allowAllProvider,
                            deliveryDate = priceResponse.deliveryDate,
                            description = priceResponse.description,
                            status = priceResponse.status
                        )
                    )
                }
            }
            return Result.success(pricesModel)
        } else {
            return Result.failure(DefaultException())
        }
    }

    override suspend fun getPricesProvider(
        cnpjBuyers: MutableList<String>,
        cnpjProvider: String,
        userModel: UserModel,
        currentDate: Long
    ): Result<MutableList<PriceModel>> {
        return runCatching {
            cnpjBuyers.mapNotNull { cnpj ->
                priceService.getPricesByCnpj(cnpj, currentDate).map { prices ->
                    prices.filter {
                        val cnpjPartnersAuthorized = it.partnersAuthorized.map { it.cnpjCorporation }
                        cnpjPartnersAuthorized.contains(cnpjProvider)
                    }.map { priceResponse ->
                        priceResponse.toPriceModel(currentDate = currentDate)
                    }
                }.getOrNull()
            }.flatten().toMutableList()
        }
    }

    override suspend fun setPricesPartner(cnpjPartner: String, productsEditPrice: PriceEditModel, userName : String,currentDate: Long): Result<Any> {
        val partnerModel = priceService.getPriceByCnpj(code = productsEditPrice.code, cnpjBuyer = productsEditPrice.cnpjBuyer,currentDate )
        return partnerModel.map { priceResponse ->
            productsEditPrice.productsEdit.forEach { productEdit ->
                val productPrice = priceResponse.productsPrice.find { it.productModel.code == productEdit.productModel.code }

                if (productEdit.price > 0.0) {
                    val userPrice = UserPrice(cnpjProvider = cnpjPartner, price = productEdit.price, nameUser = userName)
                    removePrice(productPrice, cnpjPartner)
                    productPrice?.usersPrice?.add(userPrice)
                } else {
                    removePrice(productPrice, cnpjPartner)
                }
            }

            if (priceService.editPrice(priceResponse).isSuccess) {
                Result.success(Any())
            } else {
                Result.failure(PriceNotEdit())
            }
        }.getOrElse { Result.failure(PriceNotFindException()) }
    }

    override suspend fun getPriceByCode(priceCode: String, cnpjBuyerCreator: String, currentDate: Long): Result<PriceModel> {
        return priceService.getPriceByCnpj(code = priceCode, cnpjBuyer = cnpjBuyerCreator, currentDate).map { it.toPriceModel(currentDate = currentDate) }
    }

    private fun removePrice(productPrice: ProductPriceResponse?, cnpjPartner: String) {
        val userToRemove = productPrice?.usersPrice?.find { it.cnpjProvider == cnpjPartner }
        productPrice?.usersPrice?.remove(userToRemove)
    }

    private fun productResponseToProductModelList(
        listCodes: MutableList<ProductPriceResponse>
    ): MutableList<ProductPriceModel> {
        val listProductsPriceModel: MutableList<ProductPriceModel> = mutableListOf()
        listCodes.map {
            val productPriceModel = ProductPriceModel()
            productPriceModel.productModel = it.productModel.toProductModel()
            productPriceModel.usersPrice = it.usersPrice
            productPriceModel.quantityProducts = it.quantityProducts
            productPriceModel.isSelected = false
            productPriceModel.userWinner = it.userWinner
            listProductsPriceModel.add(productPriceModel)
        }.toMutableList()
        return listProductsPriceModel
    }
}