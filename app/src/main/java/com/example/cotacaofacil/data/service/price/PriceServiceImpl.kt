package com.example.cotacaofacil.data.service.price

import com.example.cotacaofacil.data.helper.UserHelper
import com.example.cotacaofacil.data.model.OrderProviderResponse
import com.example.cotacaofacil.data.model.PriceResponse
import com.example.cotacaofacil.data.model.ProductPriceResponse
import com.example.cotacaofacil.data.service.price.contract.PriceService
import com.example.cotacaofacil.domain.containsWithoutPrice
import com.example.cotacaofacil.domain.exception.*
import com.example.cotacaofacil.domain.model.StatusPrice
import com.example.cotacaofacil.domain.model.UserPrice
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.IOException
import kotlin.random.Random

class PriceServiceImpl(
    private val firestore: FirebaseFirestore,
    private val userHelper: UserHelper
) : PriceService {
    override suspend fun savePrice(priceResponse: PriceResponse): Result<String> {
        val user = userHelper.user
        val cnpj = user?.cnpj
        if (cnpj != null) {
            priceResponse.code = createPriceCode(cnpj)
            try {
                val documentReference = firestore.collection(PRICE_TABLE)
                    .document(cnpj)
                    .collection(MY_PRICES)
                    .document(priceResponse.code)

                documentReference.addSnapshotListener { documentSnapshot, _ ->
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Result.success(priceResponse.code)
                    }
                }

                documentReference.set(priceResponse).await()
                return Result.success(priceResponse.code)
            } catch (e: Exception) {
                return Result.failure(NotCreatePriceException())
            }
        }
        return Result.failure(DefaultException())
    }

    override suspend fun editPrice(priceResponse: PriceResponse): Result<String> {
        return try {
            if (priceResponse.status == StatusPrice.FINISHED && priceResponse.orderProvider == null) {
                addOrderToPrice(priceResponse = priceResponse)
            }
            val result =
                firestore.collection(PRICE_TABLE).document(priceResponse.cnpjBuyerCreator).collection(MY_PRICES).document(priceResponse.code)
                    .set(priceResponse)
            result.await()
            if (result.isSuccessful) {
                Result.success(priceResponse.code)
            } else {
                Result.failure(NotCreatePriceException())
            }
        } catch (e: java.lang.Exception) {
            Result.failure(NotCreatePriceException())
        }
    }

    private fun addOrderToPrice(priceResponse: PriceResponse) {
        val orderProviderList = mutableListOf<OrderProviderResponse>()
        val userWinnersList: MutableList<UserPrice?> = mutableListOf()

        priceResponse.productsPrice.forEach { productPriceModel ->
            if (productPriceModel.userWinner != null) {
                if (userWinnersList.containsWithoutPrice(productPriceModel.userWinner).not()) {
                    userWinnersList.add(productPriceModel.userWinner)
                }
            }
        }
        userWinnersList.forEachIndexed { index, userPrice ->
            val orderProvider = OrderProviderResponse(
                cnpjProvider = userPrice?.cnpjProvider ?: "",
                priceCode = priceResponse.code,
                orderCode = "${priceResponse.code} $index"
            )
            orderProviderList.add(orderProvider)
            priceResponse.productsPrice.forEach { productPrice ->
                if (userPrice?.cnpjProvider == productPrice.userWinner?.cnpjProvider)
                    orderProvider.productsPrice.add(productPrice.productModel.code)
            }
        }
        priceResponse.orderProvider = orderProviderList
    }

    override suspend fun getPricesByCnpj(cnpjUser: String, currentDate: Long): Result<MutableList<PriceResponse>> {
        return try {
            val result = firestore.collection(PRICE_TABLE).document(cnpjUser)
                .collection(MY_PRICES).get().await()
            if (result.documents.isEmpty())
                Result.failure(ListEmptyException())
            else {
                val priceResponseList = result.toObjects(PriceResponse::class.java)
                updateStatusPricesList(priceResponseList, currentDate)
                priceResponseList.filterNotNull().sortedByDescending { it.dateStartPrice }
                Result.success(priceResponseList.toMutableList())
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> Result.failure(NoConnectionInternetException())
                is FirebaseNetworkException -> Result.failure(NoConnectionInternetException())
                else -> Result.failure(DefaultException())
            }
        }
    }

    override suspend fun getPriceByCnpj(code: String, cnpjBuyer: String, currentDate: Long): Result<PriceResponse> {
        return try {
            val result = firestore.collection(PRICE_TABLE).document(cnpjBuyer)
                .collection(MY_PRICES).document(code).get().await()
            if (!result.exists())
                Result.failure(PriceNotFindException())
            else {
                val priceResponse = result.toObject(PriceResponse::class.java)
                priceResponse?.let { priceResponse ->
                    updateStatusPrice(priceResponse = priceResponse, currentDate = currentDate)
                    Result.success(value = priceResponse)
                } ?: Result.failure(PriceNotFindException())
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> Result.failure(NoConnectionInternetException())
                is FirebaseNetworkException -> Result.failure(NoConnectionInternetException())
                else -> Result.failure(DefaultException())
            }
        }
    }

    private suspend fun updateStatusPricesList(pricesResponses: MutableList<PriceResponse>, currentDate: Long) {
        pricesResponses.forEach {
            updateStatusPrice(it, currentDate)
        }
    }

    private suspend fun updateStatusPrice(priceResponse: PriceResponse, currentDate: Long) {
        if (priceResponse.status == StatusPrice.OPEN &&
            priceResponse.closeAutomatic &&
            priceResponse.dateFinishPrice != -1L &&
            currentDate > priceResponse.dateFinishPrice
        ) {
            if (priceResponse.getUsersConflicts() == null) {
                priceResponse.status = StatusPrice.FINISHED
                editPrice(priceResponse = priceResponse)
            } else {
                priceResponse.status = StatusPrice.PENDENCY
                editPrice(priceResponse = priceResponse)
            }
        }
    }

    private fun PriceResponse.getUsersConflicts(): ArrayList<UserPrice>? {
        productsPrice.forEach {
            if (it.userWinner == null) {
                if (it.usersPrice.size > 1) {
                    val usersWinners = findSmallerPrice(productPrice = it)
                    if (usersWinners.size > 1) {
                        return usersWinners
                    } else if (usersWinners.size == 1) {
                        it.userWinner = usersWinners[0]
                    }
                }
                if (it.usersPrice.size == 1) {
                    it.userWinner = it.usersPrice[0]
                }
            }
        }
        return null
    }

    private fun findSmallerPrice(productPrice: ProductPriceResponse): ArrayList<UserPrice> {
        val userPrices = arrayListOf<UserPrice>()
        productPrice.usersPrice.forEach { userPrice ->
            if (userPrices.isEmpty()) {
                userPrices.add(userPrice)
                return@forEach
            }
            if (userPrices.isNotEmpty() && userPrice.price < userPrices[0].price) {
                userPrices.clear()
                userPrices.add(userPrice)
                return@forEach
            }
            if (userPrices.isNotEmpty() && userPrice.price == userPrices[0].price) {
                userPrices.add(userPrice)
                return@forEach
            }
        }
        return userPrices
    }

    private suspend fun createPriceCode(cnpjBuyer: String): String {
        var productCode = Random.nextInt(100000, 999999)
        val productsRef = firestore.collection(PRICE_TABLE).document(cnpjBuyer).collection(MY_PRICES)
        var codeExists = true

        while (codeExists) {
            val query = productsRef.whereEqualTo("code", productCode)
            val result = query.get().await()

            if (result.isEmpty) {
                codeExists = false
            } else {
                productCode = Random.nextInt(100000, 999999)
            }
        }

        return productCode.toString()
    }

    companion object {
        private const val PRICE_TABLE = "price"
        private const val MY_PRICES = "my_prices"
    }
}