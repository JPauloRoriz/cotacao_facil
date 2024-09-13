package com.example.cotacaofacil.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserPrice(
    val cnpjProvider: String = "",
    var price: Double = 0.00,
    var nameUser: String = "",
    var imageUser : String? = null
) : Parcelable
