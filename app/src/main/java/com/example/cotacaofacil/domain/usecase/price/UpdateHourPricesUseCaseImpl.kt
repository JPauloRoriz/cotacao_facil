package com.example.cotacaofacil.domain.usecase.price

import com.example.cotacaofacil.data.repository.price.PriceRepository
import com.example.cotacaofacil.domain.model.PriceModel
import com.example.cotacaofacil.domain.model.StatusPrice
import com.example.cotacaofacil.domain.usecase.date.contract.DateCurrentUseCase
import com.example.cotacaofacil.domain.usecase.price.contract.UpdateHourPricesUseCase

class UpdateHourPricesUseCaseImpl(
    private val currentDate: DateCurrentUseCase,
    private val priceRepository: PriceRepository
) : UpdateHourPricesUseCase {
    override suspend fun invoke(priceModel: PriceModel): PriceModel? {
        currentDate.invoke().onSuccess { date ->
            if (priceModel.closeAutomatic && priceModel.status == StatusPrice.OPEN && priceModel.dateFinishPrice < date
            ) {
                return null
            }
        }
        return priceModel
    }
}