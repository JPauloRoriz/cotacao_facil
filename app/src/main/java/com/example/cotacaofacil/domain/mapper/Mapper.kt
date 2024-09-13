package com.example.cotacaofacil.domain.mapper

import com.example.cotacaofacil.data.model.BodyCompanyResponse
import com.example.cotacaofacil.data.model.HistoricResponse
import com.example.cotacaofacil.data.model.PartnerResponse
import com.example.cotacaofacil.data.model.UserResponse
import com.example.cotacaofacil.domain.model.*

fun Result<UserResponse>.mapperUser(): Result<UserModel> {
    return this.map {
        UserModel(
            id = it.id,
            cnpj = it.cnpj,
            email = it.email,
            userTypeSelected = it.userTypeSelected,
            nameCorporation = it.nameCorporation
        )
    }
}

fun BodyCompanyResponse.mapperPartner(cnpj: String?, idUser: String): PartnerModel {
    return PartnerModel(
        "",
        idUser,
        this.fantasia,
        nameCorporation = this.nome,
        cnpjCorporation = cnpj ?: "",
        isMyPartner = StatusIsMyPartner.FALSE
    )
}

fun BodyCompanyModel.mapper(): BodyCompanyResponse {
    this.apply {
        if (!this.nome.isNullOrEmpty() &&
            !this.status.isNullOrEmpty() &&
            !this.uf.isNullOrEmpty() &&
            !this.fantasia.isNullOrEmpty() &&
            !this.abertura.isNullOrEmpty() &&
            !this.municipio.isNullOrEmpty() &&
            !this.situacao.isNullOrEmpty()
        ) {
            return BodyCompanyResponse(
                nome = this.nome,
                uf = this.uf,
                situacao = this.situacao,
                municipio = this.municipio,
                fantasia = this.fantasia,
                abertura = this.abertura,
                status = this.status
            )
        } else {
            return BodyCompanyResponse("", "", "", "", "", "", "")
        }

    }
}

fun PartnerModel.mapperPartner(cnpj: String): PartnerResponse {
    return PartnerResponse(
        this.cnpjCorporation,
        false,
        this.date,
        cnpj
    )
}

fun HistoryModel.toHistoricResponse(): HistoricResponse {
    return HistoricResponse(
        this.typeHistory,
        this.date,
        this.nameAssistant
    )
}

fun PriceModel.toPriceEditModel(cnpjPartner : String): PriceEditModel {
    val dateFinish = if(closeAutomatic) dateFinishPrice else -1L
    return PriceEditModel(
        code = code,
        dateFinishPrice = dateFinish,
        productsEdit = productsPrice.toProductEditPrice(cnpjPartner),
        cnpjBuyer = cnpjBuyerCreator
    )
}

fun MutableList<ProductPriceModel>.toProductEditPrice(cnpj: String): MutableList<ProductPriceEditPriceModel> {
    return map { productPriceModel ->
        productPriceModel.toProductEditPriceModel(cnpj = cnpj)
    }.toMutableList()
}

fun ProductPriceModel.toProductEditPriceModel(cnpj: String): ProductPriceEditPriceModel {
    return ProductPriceEditPriceModel(
        productModel = productModel,
        price = usersPrice.toPriceByUser(cnpj = cnpj),
        quantityProducts = quantityProducts
    )
}

fun MutableList<UserPrice>.toPriceByUser(cnpj: String): Double {
    return find { it.cnpjProvider == cnpj }?.price ?: 0.0
}
