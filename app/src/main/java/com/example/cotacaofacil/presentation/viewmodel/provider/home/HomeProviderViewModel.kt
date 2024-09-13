package com.example.cotacaofacil.presentation.viewmodel.provider.home

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cotacaofacil.data.helper.BodyCompanyHelper
import com.example.cotacaofacil.data.helper.UserHelper
import com.example.cotacaofacil.data.sharedPreferences.SharedPreferencesHelper
import com.example.cotacaofacil.domain.Extensions.Companion.convertCnpj
import com.example.cotacaofacil.domain.Extensions.Companion.emailNotIsEmpty
import com.example.cotacaofacil.domain.Extensions.Companion.foneNotIsEmpty
import com.example.cotacaofacil.domain.Extensions.Companion.formatCnpj
import com.example.cotacaofacil.domain.Extensions.Companion.nameCorporationNotIsEmpty
import com.example.cotacaofacil.domain.Extensions.Companion.nomeFantasyNotIsEmpty
import com.example.cotacaofacil.domain.getQuantityOrdersOpen
import com.example.cotacaofacil.domain.model.BodyCompanyModel
import com.example.cotacaofacil.domain.model.StatusPrice
import com.example.cotacaofacil.domain.model.UserModel
import com.example.cotacaofacil.domain.usecase.date.contract.DateCurrentUseCase
import com.example.cotacaofacil.domain.usecase.home.contract.EditImageProfileUseCase
import com.example.cotacaofacil.domain.usecase.home.contract.GetBodyCompanyModelUseCase
import com.example.cotacaofacil.domain.usecase.home.contract.GetImageProfileUseCase
import com.example.cotacaofacil.domain.usecase.partner.contract.GetAllPartnerModelUseCase
import com.example.cotacaofacil.domain.usecase.price.contract.GetPricesProviderUseCase
import com.example.cotacaofacil.presentation.ui.dialog.OptionPhoto
import com.example.cotacaofacil.presentation.viewmodel.base.SingleLiveEvent
import com.example.cotacaofacil.presentation.viewmodel.provider.home.contract.HomeProviderEvent
import com.example.cotacaofacil.presentation.viewmodel.provider.home.contract.HomeProviderState
import kotlinx.coroutines.launch
import retrofit2.HttpException

class HomeProviderViewModel(
    private val userHelper: UserHelper,
    private val getBodyCompanyModelUseCase: GetBodyCompanyModelUseCase,
    private val editImageProfileUseCase: EditImageProfileUseCase,
    private val getImageProfileUseCase: GetImageProfileUseCase,
    private val sharedPreferences: SharedPreferencesHelper,
    private val getAllPartnerModelUseCase: GetAllPartnerModelUseCase,
    private val getPricesProviderUseCase: GetPricesProviderUseCase,
    private val context: Context,
    private val currentDateUseCase: DateCurrentUseCase,
    private val bodyCompanyHelper: BodyCompanyHelper
) : ViewModel() {

    val event = SingleLiveEvent<HomeProviderEvent>()
    val state = MutableLiveData(HomeProviderState())

    init {
        viewModelScope.launch {
            userHelper.user?.let { user ->
                loadDataUser(user = user)
                getQuantityPricesOpen(user = user)
            }
        }
    }

    private suspend fun getQuantityPricesOpen(user: UserModel) {
        currentDateUseCase.invoke().onSuccess { currentDate ->
            getAllPartnerModelUseCase.invoke(userTypeSelected = user.userTypeSelected, idUser = user.id ?: "", cnpj = user.cnpj)
                .onSuccess { partnersProvider ->
                    val listCnpjs = partnersProvider.map { it.cnpjCorporation.convertCnpj() }.toMutableList()
                    getPricesProviderUseCase.invoke(cnpj = listCnpjs, cnpjProvider = user.cnpj, userModel = user, currentDate)
                        .onSuccess { prices ->
                            val quantityOrderOpen = prices.filter { it.status == StatusPrice.FINISHED }.toMutableList()
                                .getQuantityOrdersOpen(cnpjProvider = user.cnpj).size
                            state.postValue(
                                state.value?.copy(
                                    quantityPricesOpen = prices.filter { it.status == StatusPrice.OPEN }.size.toString(),
                                    quantityOrderOpen = quantityOrderOpen.toString()
                                )
                            )
                        }
                }
        }
    }

    private suspend fun loadDataUser(user: UserModel) {
        state.postValue(state.value?.copy(isLoading = true, loadingImageProfile = true))
        getBodyCompanyModelUseCase.invoke(user.cnpj)
            .onSuccess { bodyCompanyModel ->
                handlerSuccessLoadData(bodyCompanyModel, user.cnpj)
                getImageUser(cnpj = user.cnpj)
            }
            .onFailure {
                handlerFailure(throwable = it, cnpj = user.cnpj)
            }
    }

    private fun handlerFailure(throwable: Throwable, cnpj: String) {
        when (throwable) {
            is HttpException -> {
                state.postValue(
                    state.value?.copy(
                        isLoading = false,
                        fone = bodyCompanyHelper.bodyCompany?.telefone.foneNotIsEmpty(context),
                        email = bodyCompanyHelper.bodyCompany?.email.emailNotIsEmpty(context),
                        nameFantasy = bodyCompanyHelper.bodyCompany?.fantasia.nomeFantasyNotIsEmpty(context),
                        nameCorporation = bodyCompanyHelper.bodyCompany?.nome.nameCorporationNotIsEmpty(context),
                        cnpj = cnpj.formatCnpj()
                    )
                )
            }
        }
    }

    private fun handlerSuccessLoadData(bodyCompanyModel: BodyCompanyModel, cnpj: String) {
        state.postValue(
            HomeProviderState(
                isLoading = false,
                fone = bodyCompanyModel.telefone.foneNotIsEmpty(context),
                email = bodyCompanyModel.email.emailNotIsEmpty(context),
                nameFantasy = bodyCompanyModel.fantasia.nomeFantasyNotIsEmpty(context),
                nameCorporation = bodyCompanyModel.nome.nameCorporationNotIsEmpty(context),
                cnpj = cnpj.formatCnpj()
            )
        )
    }

    private suspend fun getImageUser(cnpj: String) {
        getImageProfileUseCase.invoke(cnpj)
            .onSuccess {
                state.postValue(state.value?.copy(imageProfile = it, loadingImageProfile = false))
            }.onFailure {
                state.postValue(state.value?.copy(imageProfile = null, loadingImageProfile = false))
            }
    }

    fun tapOnArrowBack(backPressedOnce: Boolean) {
        if (backPressedOnce) {
            event.postValue(HomeProviderEvent.FinishApp)
        } else {
            event.postValue(HomeProviderEvent.AskAgain)
        }
    }

    fun tapOnPartner() {
        event.postValue(HomeProviderEvent.ClickPartner)
    }

    fun tapOnLogout() {
        sharedPreferences.setStringSecret(sharedPreferences.KEY_USER_LOGIN, null, context)
        event.postValue(HomeProviderEvent.Logout)
    }

    fun tapOnImageProfile() {
        event.postValue(HomeProviderEvent.EditImage)
    }

    fun saveImage(image: Bitmap?) {
        state.postValue(state.value?.copy(loadingImageProfile = true))
        viewModelScope.launch {
            userHelper.user?.cnpj?.let { cnpj ->
                image?.let { bitmap ->
                    editImageProfileUseCase.invoke(imageBitmap = bitmap, cnpj = cnpj)
                        .onSuccess {
                            getImageUser(cnpj)
                        }
                        .onFailure {
                            //todo tratamento para erro ao carregar imagem
                        }
                }
            }
        }

    }

    fun tapOnSelectTypePhoto(optionImage: OptionPhoto) {
        when (optionImage) {
            OptionPhoto.CAMERA -> event.postValue(HomeProviderEvent.ShowCamera)
            OptionPhoto.GALLERY -> event.postValue(HomeProviderEvent.ShowGallery)
        }
    }

    fun tapOnCardPrices() {
        event.postValue(HomeProviderEvent.ClickCardPrices)
    }

}