package com.example.cotacaofacil.presentation.viewmodel.provider.home.contract

sealed class HomeProviderEvent {
    data class ErrorLoadInformation(val message : String = "e") : HomeProviderEvent()
    object ClickPartner : HomeProviderEvent()
    object FinishApp : HomeProviderEvent()
    object Logout : HomeProviderEvent()
    object AskAgain : HomeProviderEvent()
    object EditImage : HomeProviderEvent()
    object ShowCamera : HomeProviderEvent()
    object ShowGallery : HomeProviderEvent()
    object ClickCardPrices : HomeProviderEvent()
}