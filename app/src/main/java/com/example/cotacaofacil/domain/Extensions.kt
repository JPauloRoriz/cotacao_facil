package com.example.cotacaofacil.domain

import android.content.Context
import android.net.ConnectivityManager
import com.example.cotacaofacil.R
import com.example.cotacaofacil.data.model.ProductResponse
import com.example.cotacaofacil.domain.exception.DefaultException
import com.example.cotacaofacil.domain.model.*
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await


class Extensions {
    companion object {
        fun String.convertCnpj(): String {
            return this.replace(Regex("[/.-]"), "")
        }

        fun String.formatCnpj(): String {
            return "${this.substring(0, 2)}.${this.substring(2, 5)}.${this.substring(5, 8)}/${this.substring(8, 12)}-${this.substring(12)}"
        }

        fun String?.foneNotIsEmpty(context: Context): String {
            return if (this.isNullOrEmpty()) context.getString(R.string.fone_null) else this
        }

        fun String?.emailNotIsEmpty(context: Context): String {
            return if (this.isNullOrEmpty()) context.getString(R.string.email_null) else this
        }

        fun String?.nomeFantasyNotIsEmpty(context: Context): String {
            return if (this.isNullOrEmpty()) context.getString(R.string.fantasy_null) else this
        }

        fun String?.nameCorporationNotIsEmpty(context: Context): String {
            return if (this.isNullOrEmpty()) context.getString(R.string.name_corporation_null) else this
        }

        suspend fun Task<QuerySnapshot>.toResult(): Result<Unit?> {
            return try {
                if (this.isSuccessful) {
                    this.await()
                    Result.success(null)
                } else {
                    Result.failure(DefaultException())
                }
            } catch (e: Exception) {
                Result.failure(DefaultException())
            }
        }

        fun String?.ifNotEmpty(): String {
            return if (this.isNullOrEmpty()) {
                "Nome não registrado"
            } else {
                this
            }
        }

        fun Context.isNetworkConnected(): Boolean {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            return cm?.activeNetworkInfo != null && cm.activeNetworkInfo?.isConnected == true
        }

        fun MutableList<ProductResponse>.toProductModel(): MutableList<ProductModel> {
            return this.map {
                it.toProductModel()
            }.toMutableList()
        }

        fun ProductResponse.toProductModel(): ProductModel {
            return ProductModel(
                name,
                description,
                brand,
                typeMeasurement,
                cnpjBuyer,
                code,
                quantity,
                favorite,
                date
            )
        }

        fun ProductModel.toProductResponse(): ProductResponse {
            return ProductResponse(
                name,
                description,
                brand,
                typeMeasurement,
                cnpjBuyer,
                code,
                quantity,
                date,
                isFavorite

            )
        }

        fun MutableList<ProductModel>.toProductPriceModel(products: MutableList<ProductPriceModel>): MutableList<ProductPriceModel> {
            val listProductPriceModel = mutableListOf<ProductPriceModel>()
            this.forEach { productModel ->
                val product = products.find { productModel.code == it.productModel.code } ?: ProductPriceModel()
                listProductPriceModel.add(ProductPriceModel(productModel, product.usersPrice, product.isSelected))
            }
            return listProductPriceModel
        }

        fun Long.verifyAutoClose(autoClose: Boolean): Long {
            return if (autoClose) this else -1L
        }

        fun PriceModel.getCnpjProviders(): MutableList<String> {
            val providers = mutableListOf<String>()
            productsPrice.forEach {
                it.usersPrice.forEach {
                    if (!providers.contains(it.cnpjProvider)) {
                        providers.add(it.cnpjProvider)
                    }
                }
            }
            return providers
        }
    }
}

fun findSmallerPrice(productPrice: ProductPriceModel): ArrayList<UserPrice> {
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

fun PriceModel.getUsersConflicts(): ArrayList<UserPrice>? {
    productsPrice.forEach {
        if (it.userWinner == null && it.usersPrice.size > 1) {
            val usersWinners = findSmallerPrice(productPrice = it)
            if (usersWinners.size > 1) {
                return usersWinners
            }
        }
    }
    return null
}

fun MutableList<UserPrice?>.containsWithoutPrice(userPrice : UserPrice?) : Boolean{
    val cnpjs = this.map { it?.cnpjProvider }
    return cnpjs.contains(userPrice?.cnpjProvider)
}

 fun MutableList<PriceModel>.getQuantityOrdersOpen(cnpjProvider: String): MutableList<OrderProviderModel> {
    val orderByCnpj: MutableList<OrderProviderModel> = mutableListOf()
    this.forEach { priceModel ->
        priceModel.orderProvider?.forEach { orderProvider ->
            if (orderProvider.cnpjProvider == cnpjProvider) {
                orderByCnpj.add(orderProvider)
            }
        }
    }
    return orderByCnpj
}