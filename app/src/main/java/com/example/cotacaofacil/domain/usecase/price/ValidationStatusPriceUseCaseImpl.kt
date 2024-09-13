package com.example.cotacaofacil.domain.usecase.price

import com.example.cotacaofacil.data.repository.dete.contract.DateCurrentRepository
import com.example.cotacaofacil.data.repository.price.PriceRepository
import com.example.cotacaofacil.domain.exception.DefaultException
import com.example.cotacaofacil.domain.model.PriceModel
import com.example.cotacaofacil.domain.model.StatusPrice
import com.example.cotacaofacil.domain.usecase.price.contract.ValidationStatusPriceUseCase

class ValidationStatusPriceUseCaseImpl(
    private val priceRepository: PriceRepository,
    private val dateCurrentRepository: DateCurrentRepository

) : ValidationStatusPriceUseCase {
    override suspend fun invoke(prices: MutableList<PriceModel>): Result<MutableList<PriceModel>> {
        try {
            dateCurrentRepository.invoke()
                .onSuccess { dateCurrent ->
                    val pricesStatusUpdate: MutableList<PriceModel> = mutableListOf()
                    prices.forEach { priceModel ->
                        if (priceModel.closeAutomatic && priceModel.status == StatusPrice.OPEN && (priceModel.dateFinishPrice) < dateCurrent
                        ) {
                            val result = priceRepository.editPrice(priceModel = priceModel)
                            if (result.isSuccess) {
                                pricesStatusUpdate.add(priceModel)
                            } else {
                                throw DefaultException()
                            }

                        } else {
                            pricesStatusUpdate.add(priceModel)
                        }
                    }

                    return Result.success(pricesStatusUpdate)

                }.onFailure {
                    return Result.failure(DefaultException())
                }
        } catch (e: Exception) {
            return Result.failure(DefaultException())
        }
        return Result.failure(DefaultException())
    }

}