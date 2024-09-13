package com.example.cotacaofacil.domain.usecase.historic

import com.example.cotacaofacil.data.repository.history.contract.HistoryRepository
import com.example.cotacaofacil.domain.model.HistoryModel
import com.example.cotacaofacil.domain.model.TypeHistory
import com.example.cotacaofacil.domain.usecase.historic.contract.AddHistoricUseCase

class AddHistoricUseCaseImpl(
    private val repository: HistoryRepository
) : AddHistoricUseCase {
    override suspend fun addHistoric(date: Long, cnpjUser: String, codePrice: String, typeHistory: TypeHistory) {
        addHistoryModel(typeHistory, date, codePrice, cnpjUser)
    }

    private suspend fun addHistoryModel(typeHistory: TypeHistory, date: Long, codePrice: String, cnpj: String) {
        repository.addHistory(
            HistoryModel(
                typeHistory = typeHistory,
                date = date,
                nameAssistant = codePrice
            ), cnpj = cnpj
        )
    }
}