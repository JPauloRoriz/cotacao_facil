package com.example.cotacaofacil.presentation.viewmodel.buyer.home

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cotacaofacil.data.helper.BodyCompanyHelper
import com.example.cotacaofacil.data.helper.UserHelper
import com.example.cotacaofacil.data.sharedPreferences.SharedPreferencesHelper
import com.example.cotacaofacil.domain.Extensions.Companion.emailNotIsEmpty
import com.example.cotacaofacil.domain.Extensions.Companion.foneNotIsEmpty
import com.example.cotacaofacil.domain.Extensions.Companion.formatCnpj
import com.example.cotacaofacil.domain.Extensions.Companion.nameCorporationNotIsEmpty
import com.example.cotacaofacil.domain.Extensions.Companion.nomeFantasyNotIsEmpty
import com.example.cotacaofacil.domain.exception.DefaultException
import com.example.cotacaofacil.domain.exception.ListEmptyException
import com.example.cotacaofacil.domain.model.BodyCompanyModel
import com.example.cotacaofacil.domain.model.ProductModel
import com.example.cotacaofacil.domain.model.StatusPrice
import com.example.cotacaofacil.domain.model.UserModel
import com.example.cotacaofacil.domain.usecase.date.contract.DateCurrentUseCase
import com.example.cotacaofacil.domain.usecase.home.contract.EditImageProfileUseCase
import com.example.cotacaofacil.domain.usecase.home.contract.GetBodyCompanyModelUseCase
import com.example.cotacaofacil.domain.usecase.home.contract.GetImageProfileUseCase
import com.example.cotacaofacil.domain.usecase.price.contract.GetPricesBuyerUserCase
import com.example.cotacaofacil.domain.usecase.product.contract.GetAllByCnpjProductsUseCase
import com.example.cotacaofacil.presentation.ui.dialog.OptionPhoto
import com.example.cotacaofacil.presentation.viewmodel.base.SingleLiveEvent
import com.example.cotacaofacil.presentation.viewmodel.buyer.home.contract.HomeBuyerEvent
import com.example.cotacaofacil.presentation.viewmodel.buyer.home.contract.HomeBuyerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class HomeBuyerViewModel(
    private val getAllProductsUseCase: GetAllByCnpjProductsUseCase,
    private val userHelper: UserHelper,
    private val getBodyCompanyModelUseCase: GetBodyCompanyModelUseCase,
    private val sharedPreferences: SharedPreferencesHelper,
    private val context: Context,
    private val bodyCompanyHelper: BodyCompanyHelper,
    private val getPricesBuyerUserCase: GetPricesBuyerUserCase,
    private val editImageProfileUseCase: EditImageProfileUseCase,
    private val getImageProfileUseCase: GetImageProfileUseCase,
    private val currentUseCase: DateCurrentUseCase,
) : ViewModel() {

    val event = SingleLiveEvent<HomeBuyerEvent>()
    val state = MutableLiveData(HomeBuyerState())

    init {
        state.postValue(state.value?.copy(isLoading = true, loadingImageProfile = true))
        userHelper.user?.let { user ->
            loadDataUser()
            loadDataPrice(user)
        }
    }

    private fun loadDataPrice(user: UserModel) {
        viewModelScope.launch(Dispatchers.IO) {
            currentUseCase.invoke().onSuccess { currentDate ->
                getPricesBuyerUserCase.invoke(cnpjUser = user.cnpj, userTypeSelected = user.userTypeSelected, userModel = user, currentDate = currentDate)
                    .onSuccess { prices ->
                        val pricesStatusOpen = prices.filter { it.status == StatusPrice.OPEN }
                        state.postValue(
                            state.value?.copy(
                                quantityPrice = pricesStatusOpen.size.toString()
                            )
                        )

                    }
                    .onFailure {

                    }
            }
        }
    }

    fun loadDataUser() {
        val user = userHelper.user
        viewModelScope.launch(Dispatchers.IO) {
            user?.cnpj?.let {
                getBodyCompanyModelUseCase.invoke(it)
                    .onSuccess { bodyCompanyModel ->
                        handleSuccessBodyCompany(bodyCompanyModel, user)
                        getImageUser(cnpj = user.cnpj)
                    }
                    .onFailure { exception ->
                        when (exception) {
                            is HttpException -> {
                                handlerErrorHttp(user)
                            }
                        }
                    }
            }
        }
    }

    private fun handlerErrorHttp(user: UserModel) {
        state.postValue(
            state.value?.copy(
                isLoading = false,
                fone = bodyCompanyHelper.bodyCompany?.telefone.foneNotIsEmpty(context),
                email = bodyCompanyHelper.bodyCompany?.email.emailNotIsEmpty(context),
                nameFantasy = bodyCompanyHelper.bodyCompany?.fantasia.nomeFantasyNotIsEmpty(context),
                nameCorporation = bodyCompanyHelper.bodyCompany?.nome.nameCorporationNotIsEmpty(context),
                cnpj = user.cnpj.formatCnpj()
            )
        )
    }

    private fun handleSuccessBodyCompany(bodyCompanyModel: BodyCompanyModel, user: UserModel) {
        viewModelScope.launch {
            getAllProductsUseCase.invoke(user.cnpj)
                .onSuccess { productsModelList ->
                    successGetProducts(bodyCompanyModel, user, productsModelList)
                }.onFailure {
                    //todo tratamento de erros
                }
        }
    }

    private fun successGetProducts(bodyCompanyModel: BodyCompanyModel, user: UserModel, productsModelList: MutableList<ProductModel>) {
        bodyCompanyModel.apply {
            state.postValue(
                state.value?.copy(
                    isLoading = false,
                    fone = telefone.foneNotIsEmpty(context),
                    email = email.emailNotIsEmpty(context),
                    nameFantasy = fantasia.nomeFantasyNotIsEmpty(context),
                    nameCorporation = nome.nameCorporationNotIsEmpty(context),
                    cnpj = user.cnpj.formatCnpj(),
                    quantityProducts = productsModelList.size.toString()
                )
            )
        }
    }

    fun tapOnCardStock() {
        viewModelScope.launch(Dispatchers.IO) {
            userHelper.user?.cnpj?.let {
                getAllProductsUseCase.invoke(it)
                    .onSuccess {
                        event.postValue(HomeBuyerEvent.SuccessListProducts(userHelper.user))
                    }
                    .onFailure { exception ->
                        when (exception) {
                            is ListEmptyException -> {
                                event.postValue(HomeBuyerEvent.ListEmptyProducts(userHelper.user))
                            }
                            is DefaultException -> {
                                event.postValue(HomeBuyerEvent.ErrorLoadListProducts)
                            }

                        }
                    }
            }

        }
    }

    fun tapOnArrowBack(backPressedOnce: Boolean) {
        if (backPressedOnce) {
            event.postValue(HomeBuyerEvent.FinishApp)
        } else {
            event.postValue(HomeBuyerEvent.AskAgain)
        }
    }

    fun tapOnPartner() {
        event.postValue(HomeBuyerEvent.ClickPartner)
    }

    fun tapOnLogout() {
        sharedPreferences.setStringSecret(sharedPreferences.KEY_USER_LOGIN, null, context)
        event.postValue(HomeBuyerEvent.Logout)
    }

    fun tapOnCardPrices() {
        event.postValue(HomeBuyerEvent.ClickCardPrices)
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

    private suspend fun getImageUser(cnpj: String) {
        getImageProfileUseCase.invoke(cnpj)
            .onSuccess {
                state.postValue(state.value?.copy(imageProfile = it, loadingImageProfile = false))
            }.onFailure {
                state.postValue(state.value?.copy(imageProfile = null, loadingImageProfile = false))
            }
    }

    fun tapOnSelectTypePhoto(optionImage: OptionPhoto) {
        when (optionImage) {
            OptionPhoto.CAMERA -> event.postValue(HomeBuyerEvent.ShowCamera)
            OptionPhoto.GALLERY -> event.postValue(HomeBuyerEvent.ShowGallery)
        }
    }

    fun tapOnImageProfile() {
        event.postValue(HomeBuyerEvent.EditImage)
    }
}
