package com.example.cotacaofacil.presentation.viewmodel.partner

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cotacaofacil.R
import com.example.cotacaofacil.data.helper.UserHelper
import com.example.cotacaofacil.domain.exception.*
import com.example.cotacaofacil.domain.model.PartnerModel
import com.example.cotacaofacil.domain.model.StatusIsMyPartner
import com.example.cotacaofacil.domain.usecase.date.contract.DateCurrentUseCase
import com.example.cotacaofacil.domain.usecase.home.contract.GetImageProfileUseCase
import com.example.cotacaofacil.domain.usecase.partner.contract.*
import com.example.cotacaofacil.domain.usecase.partner.util.TypeDeletePartner
import com.example.cotacaofacil.presentation.viewmodel.base.SingleLiveEvent
import com.example.cotacaofacil.presentation.viewmodel.partner.model.PartnerEvent
import com.example.cotacaofacil.presentation.viewmodel.partner.model.PartnerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PartnerViewModel(
    private val context: Context,
    private val validationCnpjUseCase: ValidationCnpjUseCase,
    private val getAllPartnerModelUseCase: GetAllPartnerModelUseCase,
    private val addRequestPartnerUseCase: AddRequestPartnerUseCase,
    private val rejectRequestPartnerUseCase: RejectRequestPartnerUseCase,
    private val acceptRequestPartnerUseCase: AcceptRequestPartnerUseCase,
    private val dateCurrentUseCase: DateCurrentUseCase,
    private val getImageProfileUseCase: GetImageProfileUseCase,
    private val userHelper: UserHelper
) : ViewModel() {
    val stateLiveData = MutableLiveData(PartnerState(textButtonPartner =  context.getString(R.string.add_new_partner)))
    val eventLiveData = SingleLiveEvent<PartnerEvent>()
    val user = userHelper.user

    init {
        loadListPartnerModel(true)
    }

    fun loadListPartnerModel(isAll: Boolean) {
        stateLiveData.postValue(stateLiveData.value?.copy(isLoading = true))
        viewModelScope.launch(Dispatchers.IO) {
            user?.id?.let {
                getAllPartnerModelUseCase.invoke(user.userTypeSelected, it, user.cnpj)
                    .onSuccess { listPartnerModel ->
                        val listFilter = listPartnersFilter(isAll, listPartnerModel)
                        if (listFilter.isEmpty()) {
                            stateLiveData.postValue(
                                stateLiveData.value?.copy(
                                    textTitleList = setTitleList(isAll),
                                    showImageError = true,
                                    isLoading = false,
                                    listPartnerModel = mutableListOf(),
                                    messageError = setMessageError(isAll),
                                    numberNotifications = listPartnersFilter(false, listPartnerModel).size.toString(),
                                )
                            )

                        } else {
                            stateLiveData.postValue(
                                stateLiveData.value?.copy(
                                    textTitleList = setTitleList(isAll),
                                    isLoading = false,
                                    showImageError = false,
                                    listPartnerModel = listFilter,
                                    numberNotifications = listPartnersFilter(
                                        isAllPartners = false,
                                        listAllPartners = listPartnerModel
                                    ).size.toString(),
                                    textButtonPartner = setTextButton(isAll),
                                    showInputLayout = isAll
                                )
                            )
                            getImageProfile(listFilter = listFilter)
                        }
                    }.onFailure { it.message }
            }
        }
    }

    private suspend fun getImageProfile(listFilter: MutableList<PartnerModel>) {
        listFilter.forEach { partnerModel ->
            getImageProfileUseCase.invoke(partnerModel.cnpjCorporation)
                .onSuccess { imageUrl ->
                    partnerModel.imageProfile = imageUrl
                    stateLiveData.postValue(stateLiveData.value?.copy(listPartnerModel = listFilter))
                }
        }
    }

    private fun listPartnersFilter(
        isAllPartners: Boolean,
        listAllPartners: MutableList<PartnerModel>
    ): MutableList<PartnerModel> {
        return if (isAllPartners) {
            listAllPartners.filter { partnerModel ->
                partnerModel.isMyPartner != StatusIsMyPartner.TO_RESPOND && partnerModel.isMyPartner != StatusIsMyPartner.TO_RESPOND
            }.toMutableList()
        } else {
            listAllPartners.filter { partnerModel ->
                partnerModel.isMyPartner == StatusIsMyPartner.WAIT_ANSWER || partnerModel.isMyPartner == StatusIsMyPartner.TO_RESPOND
            }.toMutableList()
        }
    }


    suspend fun tapOnAddNewPartner(cnpj: String) {
        stateLiveData.value = stateLiveData.value?.copy(textTitleList = setTitleList(true), isLoading = true)
        viewModelScope.launch(Dispatchers.IO) {
            user.let { user ->
                user?.userTypeSelected?.let { userType ->
                    validationCnpjUseCase.invoke(userType, user, cnpj, context)
                        .onSuccess { partnerModel ->
                            val listPartner = mutableListOf<PartnerModel>()
                            partnerModel?.let { listPartner.add(partnerModel) }
                            eventLiveData.postValue(PartnerEvent.GoToAddNewPartnerSuccess)
                            stateLiveData.postValue(
                                stateLiveData.value?.copy(
                                    textTitleList = setTitleList(true),
                                    listPartnerModel = listPartner,
                                    isLoading = false,
                                    showImageError = false
                                )
                            )
                        }.onFailure { error ->
                            eventLiveData.postValue(PartnerEvent.GoToAddNewPartnerError)
                            when (error) {
                                is EmptyFildException -> {
                                    eventLiveData.postValue(PartnerEvent.FindEmpty)
                                }
                                is CnpjOwnException -> {
                                    stateLiveData.postValue(
                                        stateLiveData.value?.copy(
                                            listPartnerModel = mutableListOf(),
                                            isLoading = false,
                                            messageError = context.getString(R.string.owner_erro_message),
                                            showImageError = true
                                        )
                                    )

                                }
                                is CnpjIncompleteException -> {
                                    stateLiveData.postValue(
                                        stateLiveData.value?.copy(
                                            textTitleList = setTitleList(true),
                                            listPartnerModel = mutableListOf(),
                                            isLoading = false,
                                            messageError = context.getString(R.string.cnpj_invalid_exception),
                                            showImageError = true
                                        )
                                    )
                                }
                                is NoConnectionInternetException -> {
                                    eventLiveData.postValue(PartnerEvent.ErrorInternetConnection(context.getString(R.string.not_internet)))
                                }
                                is UserNotFindException -> {
                                    stateLiveData.postValue(
                                        stateLiveData.value?.copy(
                                            textTitleList = setTitleList(true),
                                            listPartnerModel = mutableListOf(),
                                            isLoading = false,
                                            messageError = context.getString(R.string.not_cnpj_find),
                                            showImageError = true
                                        )
                                    )
                                }
                                is DefaultException -> {
                                    stateLiveData.postValue(
                                        stateLiveData.value?.copy(
                                            textTitleList = setTitleList(true),
                                            listPartnerModel = mutableListOf(),
                                            isLoading = false,
                                            messageError = context.getString(R.string.default_exception_find),
                                            showImageError = true,
                                        )
                                    )
                                }
                            }
                        }
                }
            }
        }
    }

    fun enterCnpj() {
        eventLiveData.value = PartnerEvent.EnterCnpj
    }

    fun tapOnIconPartner(partner: PartnerModel) {
        viewModelScope.launch(Dispatchers.IO) {
            stateLiveData.postValue(
                stateLiveData.value?.copy(
                    isLoading = true
                )
            )
            dateCurrentUseCase.invoke()
                .onSuccess { currentDate ->
                    when (partner.isMyPartner) {
                        StatusIsMyPartner.FALSE -> {
                            user?.let {
                                addRequestPartnerUseCase.invoke(it, partner, currentDate, context)
                                    .onSuccess {
                                        loadListPartnerModel(true)
                                        eventLiveData.postValue(PartnerEvent.RequestAddPartner)
                                    }
                                    .onFailure {
                                        when (it) {
                                            is NoConnectionInternetException -> {
                                                stateLiveData.postValue(
                                                    stateLiveData.value?.copy(
                                                        textTitleList = setTitleList(true),
                                                        listPartnerModel = mutableListOf(),
                                                        showImageError = true,
                                                        messageError = context.getString(
                                                            R.string.not_internet
                                                        )
                                                    )
                                                )
                                            }
                                            else -> {
                                                stateLiveData.postValue(
                                                    stateLiveData.value?.copy(
                                                        textTitleList = setTitleList(true),
                                                        showImageError = true,
                                                        messageError = context.getString(
                                                            R.string.not_possibility_add_partner
                                                        )
                                                    )
                                                )
                                            }
                                        }
                                    }
                            }
                        }
                        StatusIsMyPartner.TRUE -> {
                            eventLiveData.postValue(PartnerEvent.DeletePartner(partner))
                        }
                        StatusIsMyPartner.WAIT_ANSWER -> {
                            eventLiveData.postValue(PartnerEvent.CancelRequestPartner(partner))
                        }
                        StatusIsMyPartner.TO_RESPOND -> {
                            //Onde muda o constraint para aceitar ou recusar a solicitação
                        }
                    }
                }
                .onFailure {
                    //todo tratamento para erro de conexão com internet
                }

        }
    }

    fun tapOnRejectPartner(partner: PartnerModel) {
        eventLiveData.postValue(PartnerEvent.RejectPartner(partner))
    }

    suspend fun tapOnConfirmRejectPartner(partner: PartnerModel, typeDeletePartner: TypeDeletePartner) {
        stateLiveData.postValue(stateLiveData.value?.copy(textTitleList = setTitleList(true), isLoading = true))
        dateCurrentUseCase.invoke()
            .onSuccess { currentDate ->
                user?.cnpj?.let {
                    rejectRequestPartnerUseCase.invoke(it, partner, context, typeDeletePartner, currentDate)
                        .onSuccess {
                            eventLiveData.postValue(PartnerEvent.SuccessRejectPartner)
                            loadListPartnerModel(true)
                        }.onFailure {
                            when (it) {
                                is NoConnectionInternetException -> {
                                    stateLiveData.postValue(
                                        stateLiveData.value?.copy(
                                            isLoading = false,
                                            textTitleList = setTitleList(true),
                                            listPartnerModel = mutableListOf(),
                                            showImageError = true,
                                            messageError = context.getString(
                                                R.string.not_internet
                                            )
                                        )
                                    )
                                }
                                else -> {
                                    stateLiveData.postValue(
                                        stateLiveData.value?.copy(
                                            isLoading = false,
                                            messageError = context.getString(
                                                R.string.inpossible_reject_request_partner
                                            ),
                                            showImageError = true
                                        )
                                    )
                                }
                            }
                        }
                }
            }
            .onFailure {

            }
    }

    suspend fun tapOnConfirmDeletePartner(partner: PartnerModel) {
        stateLiveData.postValue(
            stateLiveData.value?.copy(
                textTitleList = setTitleList(true), isLoading = true
            )
        )
        dateCurrentUseCase.invoke()
            .onSuccess { currentDate ->
                user?.cnpj?.let {
                    rejectRequestPartnerUseCase.invoke(it, partner, context, TypeDeletePartner.DELETE_PARTNER, currentDate)
                        .onSuccess {
                            stateLiveData.postValue(stateLiveData.value?.copy(isLoading = false))
                            eventLiveData.postValue(PartnerEvent.SuccessDeletePartner)
                            loadListPartnerModel(true)
                        }.onFailure {
                            stateLiveData.postValue(
                                stateLiveData.value?.copy(
                                    textTitleList = setTitleList(true), isLoading = false, showImageError = true, messageError = context.getString(
                                        R.string.inpossible_delete_partner, partner.nameFantasy
                                    )
                                )
                            )
                        }
                }
            }
            .onFailure {

            }

    }

    fun tapOnAcceptPartner(partner: PartnerModel) {
        stateLiveData.postValue(stateLiveData.value?.copy(showImageError = false, isShowListMyPartners = true, isLoading = true))
        viewModelScope.launch(Dispatchers.IO) {
            dateCurrentUseCase.invoke()
                .onSuccess { currentDate ->
                    user?.cnpj?.let {
                        acceptRequestPartnerUseCase.invoke(it, partner, currentDate)
                            .onSuccess {
                                eventLiveData.postValue(PartnerEvent.SuccessAcceptPartner)
                                loadListPartnerModel(false)
                            }.onFailure {
                                when (it) {
                                    is DefaultException -> {
                                        stateLiveData.postValue(
                                            stateLiveData.value?.copy(
                                                textTitleList = setTitleList(true),
                                                isLoading = false,
                                                showImageError = true,
                                                messageError = context.getString(
                                                    R.string.inpossible_accept_request_partner
                                                ),
                                                isShowListMyPartners = false
                                            )
                                        )
                                    }
                                    else -> {
                                        stateLiveData.postValue(
                                            stateLiveData.value?.copy(
                                                textTitleList = setTitleList(true),
                                                isLoading = false,
                                                showImageError = true,
                                                messageError = context.getString(
                                                    R.string.inpossible_accept_request_partner
                                                ),
                                                isShowListMyPartners = false
                                            )
                                        )
                                    }
                                }

                            }
                    }
                }
                .onFailure {
                    //todo tratamento para sem conexão
                }
        }
    }

    private fun listRequests(listAllPartners: MutableList<PartnerModel>): MutableList<PartnerModel> {
        return listAllPartners.filter { partnerModel ->
            partnerModel.isMyPartner == StatusIsMyPartner.TO_RESPOND
        }.toMutableList()
    }

    private fun setTitleList(isAll: Boolean): String {
        return if (isAll) {
            if (user?.userTypeSelected?.userBuyerSelected == true) {
                context.getString(R.string.providers)
            } else {
                context.getString(R.string.buyers)
            }
        } else {
            context.getString(R.string.request)
        }
    }

    private fun setTextButton(isAll: Boolean): String {
        return if (isAll) {
            context.getString(R.string.add_new_partner)
        } else {
            context.getString(R.string.your_partner)
        }
    }

    private fun setMessageError(isAll: Boolean): String {
        return if (isAll) {
            if (user?.userTypeSelected?.userBuyerSelected == true) {
                context.getString(
                    R.string.partner_empty_message_error_buyer
                )
            } else {
                context.getString(R.string.partner_empty_message_error_provider)
            }
        } else {
            context.getString(
                R.string.request_empty_message_error
            )
        }
    }

    fun tapOnArrow() {
        eventLiveData.postValue(PartnerEvent.TapOnBack)
    }
}