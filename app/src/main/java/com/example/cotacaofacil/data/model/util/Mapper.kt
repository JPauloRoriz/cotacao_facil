package com.example.cotacaofacil.data.model.util

import com.example.cotacaofacil.data.model.*
import com.example.cotacaofacil.domain.Extensions.Companion.toProductModel
import com.example.cotacaofacil.domain.Extensions.Companion.toProductResponse
import com.example.cotacaofacil.domain.model.*

fun BodyCompanyResponse.toBodyCompanyModel(): BodyCompanyModel {
    return BodyCompanyModel(
        nome, uf, situacao, municipio, fantasia, abertura, status, telefone, email
    )
}

fun BodyCompanyResponse.toPartnerModel(idUser: String, cnpj: String, date: Long): PartnerModel {
    return PartnerModel(
        id = "",
        idUser = idUser,
        nameCorporation = this.nome,
        nameFantasy = fantasia,
        cnpjCorporation = cnpj,
        isMyPartner = StatusIsMyPartner.TRUE,
        date = date,
        imageProfile = null
    )
}

fun Result<MutableList<HistoricResponse>>.toHistoricModel(): Result<Any> {
    val historicModelList = mutableListOf<HistoryModel>()

    return this.fold(
        onSuccess = {
            it.forEach { historicResponse ->
                historicModelList.add(
                    HistoryModel(
                        historicResponse.date,
                        historicResponse.type,
                        historicResponse.nameAssistant
                    )
                )
            }
            Result.success(historicModelList)
        },
        onFailure = {
            Result.failure<Throwable>(it)
        }

    )
}


fun PriceResponse.toPriceModel(currentDate: Long): PriceModel {
    return PriceModel(
        code = code,
        productsPrice = productsPrice.toListProductsPriceModel(),
        partnersAuthorized = partnersAuthorized,
        nameCompanyCreator = nameCompanyCreator,
        dateStartPrice = dateStartPrice,
        dateFinishPrice = dateFinishPrice,
        priority = priority,
        cnpjBuyerCreator = cnpjBuyerCreator,
        closeAutomatic = closeAutomatic,
        allowAllProvider = allowAllProvider,
        deliveryDate = deliveryDate,
        description = description,
        status = status,
        orderProvider = orderProvider.toOrderProviderModelList(currentDate = currentDate, dateFinishDelivery = deliveryDate)
    )
}

fun PriceModel.toPriceResponse(): PriceResponse {
    return PriceResponse(
        code = code,
        productsPrice = productsPrice.toListProductsPriceResponse(),
        partnersAuthorized = partnersAuthorized,
        nameCompanyCreator = nameCompanyCreator,
        dateStartPrice = dateStartPrice,
        dateFinishPrice = dateFinishPrice,
        priority = priority,
        cnpjBuyerCreator = cnpjBuyerCreator,
        closeAutomatic = closeAutomatic,
        allowAllProvider = allowAllProvider,
        deliveryDate = deliveryDate,
        description = description,
        status = status,
        orderProvider = orderProvider.toOrderProviderResponseList()
    )
}

fun MutableList<ProductPriceModel>.toListProductsPriceResponse(): MutableList<ProductPriceResponse> {
    return map {
        it.toProductPriceResponse()
    }.toMutableList()
}

fun MutableList<ProductPriceResponse>.toListProductsPriceModel(): MutableList<ProductPriceModel> {
    return map {
        it.toProductPriceModel()
    }.toMutableList()
}

fun ProductPriceModel.toProductPriceResponse(): ProductPriceResponse {
    return ProductPriceResponse(
        productModel = productModel.toProductResponse(),
        usersPrice = usersPrice,
        quantityProducts = quantityProducts,
        userWinner = userWinner,
    )
}

fun ProductPriceResponse.toProductPriceModel(): ProductPriceModel {
    return ProductPriceModel(
        productModel = productModel.toProductModel(),
        usersPrice = usersPrice,
        quantityProducts = quantityProducts,
        userWinner = userWinner,
    )
}

fun MutableList<OrderProviderModel>?.toOrderProviderResponseList(): MutableList<OrderProviderResponse>? {
    return this?.map { it.toOrderProviderResponse() }?.toMutableList()
}

fun MutableList<OrderProviderResponse>?.toOrderProviderModelList(currentDate: Long, dateFinishDelivery: Long): MutableList<OrderProviderModel>? {
    return this?.map { it.toOrderProviderModel(currentDate = currentDate, finishDeliveryPrice = dateFinishDelivery) }?.toMutableList()
}

fun OrderProviderModel.toOrderProviderResponse(): OrderProviderResponse {
    return OrderProviderResponse(
        priceCode = priceCode,
        orderCode = orderCode,
        cnpjProvider = cnpjProvider,
        productsPrice = productsPrice
    )

}

fun OrderProviderResponse.toOrderProviderModel(currentDate: Long, finishDeliveryPrice: Long): OrderProviderModel {
    return OrderProviderModel(
        priceCode = priceCode,
        orderCode = orderCode,
        cnpjProvider = cnpjProvider,
        productsPrice = productsPrice,
        isDelayInDelivery = currentDate > finishDeliveryPrice
    )

}


