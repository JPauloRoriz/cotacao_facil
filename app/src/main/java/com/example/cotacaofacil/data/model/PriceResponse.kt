package com.example.cotacaofacil.data.model

import android.os.Parcelable
import com.example.cotacaofacil.domain.model.PartnerModel
import com.example.cotacaofacil.domain.model.PriorityPrice
import com.example.cotacaofacil.domain.model.StatusPrice
import kotlinx.parcelize.Parcelize

@Parcelize
data class PriceResponse (
        var code : String = "",
        var productsPrice : MutableList<ProductPriceResponse> = mutableListOf(),
        val partnersAuthorized : MutableList<PartnerModel> = mutableListOf(),
        var nameCompanyCreator : String = "",
        val dateStartPrice : Long = 0,
        val dateFinishPrice : Long = -1L,
        val dateFinishPendency : Long? = null,
        var priority : PriorityPrice = PriorityPrice.AVERAGE,
        var cnpjBuyerCreator : String = "",
        var closeAutomatic : Boolean = true,
        var allowAllProvider : Boolean = false,
        val deliveryDate : Long = 0,
        val description : String = "",
        var status : StatusPrice = StatusPrice.OPEN,
        var orderProvider : MutableList<OrderProviderResponse>? = null,
) : Parcelable