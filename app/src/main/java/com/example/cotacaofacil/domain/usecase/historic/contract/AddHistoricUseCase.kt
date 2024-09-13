package com.example.cotacaofacil.domain.usecase.historic.contract

import com.example.cotacaofacil.domain.model.TypeHistory

interface AddHistoricUseCase {
    suspend fun addHistoric(date: Long, cnpjUser: String, codePrice: String, typeHistory: TypeHistory)
}