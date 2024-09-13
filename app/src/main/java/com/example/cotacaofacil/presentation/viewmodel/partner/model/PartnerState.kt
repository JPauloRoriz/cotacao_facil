package com.example.cotacaofacil.presentation.viewmodel.partner.model

import com.example.cotacaofacil.domain.model.PartnerModel

data class PartnerState(
    val isShowListMyPartners: Boolean = true,
    val messageError: String = "",
    val imageError: String = "",
    val isLoading: Boolean = false,
    val listPartnerModel : MutableList<PartnerModel> = mutableListOf(),
    val showImageError : Boolean = false,
    val numberNotifications : String = "",
    val textTitleList : String = "",
    val textButtonPartner : String = "",
    val showInputLayout : Boolean = true,
)