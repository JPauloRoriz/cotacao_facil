package com.example.cotacaofacil.domain.model

import android.os.Parcelable
import com.example.cotacaofacil.data.model.OrderProviderResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class PriceModel(
    var code : String = "",
    var nameCompanyCreator : String = "",
    var productsPrice : MutableList<ProductPriceModel> = mutableListOf(),
    var partnersAuthorized : MutableList<PartnerModel> = mutableListOf(),
    val dateStartPrice : Long = 0,
    var dateFinishPrice : Long = 0,
    var dateFinishPendency : Long? = null,
    var priority : PriorityPrice = PriorityPrice.AVERAGE,
    var cnpjBuyerCreator : String = "",
    var closeAutomatic : Boolean = true,
    var allowAllProvider : Boolean = false,
    val deliveryDate : Long = 0,
    var description : String = "",
    var orderProvider : MutableList<OrderProviderModel>? = null,
    var status : StatusPrice = StatusPrice.OPEN
) : Parcelable

enum class PriorityPrice{
    HIGH,
    AVERAGE,
    LOW
}

enum class StatusPrice{
    OPEN,
    CANCELED,
    FINISHED,
    PENDENCY,
}

