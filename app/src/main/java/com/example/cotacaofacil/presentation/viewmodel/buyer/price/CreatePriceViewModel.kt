package com.example.cotacaofacil.presentation.viewmodel.buyer.price

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cotacaofacil.R
import com.example.cotacaofacil.data.helper.UserHelper
import com.example.cotacaofacil.domain.exception.*
import com.example.cotacaofacil.domain.model.PartnerModel
import com.example.cotacaofacil.domain.model.PriorityPrice
import com.example.cotacaofacil.domain.model.StatusIsMyPartner
import com.example.cotacaofacil.domain.model.UserModel
import com.example.cotacaofacil.domain.usecase.date.contract.CalculationDateFinishPriceUseCase
import com.example.cotacaofacil.domain.usecase.date.contract.DateCurrentUseCase
import com.example.cotacaofacil.domain.usecase.date.contract.ValidationNextCreatePriceUseCase
import com.example.cotacaofacil.domain.usecase.partner.contract.GetAllPartnerModelUseCase
import com.example.cotacaofacil.presentation.ui.extension.toFormattedDateTime
import com.example.cotacaofacil.presentation.viewmodel.base.SingleLiveEvent
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractCreatePrice.CreatePriceEvent
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractCreatePrice.CreatePriceState
import com.example.cotacaofacil.presentation.viewmodel.register.model.UserTypeSelected
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CreatePriceViewModel(
    private val getAllPartnerModelUseCase: GetAllPartnerModelUseCase,
    private val userHelper: UserHelper,
    private val currentDateUseCase: DateCurrentUseCase,
    private val calculationDateFinishPriceUseCase: CalculationDateFinishPriceUseCase,
    private val validationNextCreatePriceUseCase: ValidationNextCreatePriceUseCase,
    private val context: Context
) : ViewModel() {
    val createPriceEventLiveData = SingleLiveEvent<CreatePriceEvent>()
    val createPriceStateLiveData = MutableLiveData(CreatePriceState())

    val user: UserModel? = userHelper.user
    var currentDate: String = System.currentTimeMillis().toFormattedDateTime()
    private var showEffect: Boolean = true
    var listAllPartners: MutableList<PartnerModel> = mutableListOf()
    var date: String = ""

    init {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.DAY_OF_MONTH, 1)

        date = currentDate
        createPriceStateLiveData.postValue(
            createPriceStateLiveData.value?.copy(
                showProgressBar = true, dateFinishPrice = currentDate, dateDelivery = calendar.timeInMillis.toFormattedDateTime()
            )
        )
        viewModelScope.launch(Dispatchers.IO) {
            currentDateUseCase.invoke().onSuccess { date ->
                    currentDate = calculationDateFinishPriceUseCase.invoke(date)
                    user?.id?.let { userId ->
                        user.cnpj.let { userCnpj ->
                            getAllPartnerModelUseCase.invoke(userTypeSelected = UserTypeSelected(userBuyerSelected = true), userId, userCnpj)
                                .onSuccess {
                                    val result = it.filter { it.isMyPartner == StatusIsMyPartner.TRUE }.toMutableList()
                                    if (result.isNotEmpty()) {
                                        listAllPartners = result
                                        createPriceStateLiveData.postValue(
                                            createPriceStateLiveData.value?.copy(
                                                listPartnersSelect = result, showProgressBar = false, messageErrorPartners = ""
                                            )
                                        )
                                    } else {
                                        createPriceStateLiveData.postValue(
                                            createPriceStateLiveData.value?.copy(
                                                showProgressBar = false,
                                                messageErrorPartners = context.getString(R.string.not_find_partner),
                                                listPartnersSelect = mutableListOf()
                                            )
                                        )
                                    }
                                }.onFailure {
                                    createPriceStateLiveData.postValue(createPriceStateLiveData.value?.copy(showProgressBar = false))
                                }
                        }
                    }
                }.onFailure {

                }
        }
    }

    fun tapOnSwitchAutoClose(checked: Boolean) {
        if (checked) createPriceEventLiveData.postValue(CreatePriceEvent.AutoChecked)
        else createPriceEventLiveData.postValue(CreatePriceEvent.NotAutoChecked)
    }

    fun tapOnSwitchAllPartners(checked: Boolean, listPartner: MutableList<PartnerModel>) {
        if (showEffect) {
            if (checked) {
                createPriceEventLiveData.postValue(CreatePriceEvent.AllPartnersWithEffect)
            } else {
                createPriceEventLiveData.postValue(CreatePriceEvent.NotAllPartnersWithEffect)
            }
        } else {
            if (checked) {
                createPriceEventLiveData.postValue(CreatePriceEvent.AllPartners)
            }

        }
        showEffect = true
    }

    fun filterSearch(text: CharSequence?) {
        val listMyPartners = listAllPartners.filter { it.isMyPartner == StatusIsMyPartner.TRUE }.toMutableList()
        if (text?.isEmpty() == true) {
            createPriceStateLiveData.postValue(createPriceStateLiveData.value?.copy(listPartnersSelect = listMyPartners, messageErrorPartners = ""))
        } else {
            val resultFilter: MutableList<PartnerModel> = mutableListOf()
            listMyPartners.forEach {
                if (text?.let { it1 -> it.cnpjCorporation.contains(it1, true) } == true) {
                    resultFilter.add(it)
                }
            }
            if (resultFilter.isNotEmpty()) {
                createPriceStateLiveData.postValue(createPriceStateLiveData.value?.copy(listPartnersSelect = resultFilter, messageErrorPartners = ""))
            } else {
                createPriceStateLiveData.postValue(
                    createPriceStateLiveData.value?.copy(
                        messageErrorPartners = context.getString(R.string.not_find_partner), listPartnersSelect = mutableListOf()
                    )
                )
            }
        }

    }

    fun tapOnButtonNext(
        autoClose: Boolean,
        allowAllPartners: Boolean,
        date: Long,
        dateDelivery: Long,
        partners: MutableList<PartnerModel>,
        description: String,
        priority: PriorityPrice?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            currentDateUseCase.invoke().onSuccess { currentDate ->
                    user?.nameCorporation?.let {
                        validationNextCreatePriceUseCase.invoke(
                            autoClose = autoClose,
                            allowAllPartners = allowAllPartners,
                            date = date,
                            dateDelivery = dateDelivery,
                            partners = partners,
                            description = description,
                            priority = priority,
                            currentDate = currentDate,
                            nameCompanyCreator = it
                        ).onSuccess { priceModel ->
                                val priorityPrice: PriorityPrice? = priority
                                priorityPrice?.let { priority ->
                                    priceModel.cnpjBuyerCreator = user.cnpj
                                    priceModel.priority = priority
                                    priceModel.dateFinishPrice = date
                                    priceModel.partnersAuthorized = listAllPartners
                                    priceModel.description = description
                                    priceModel.nameCompanyCreator = user.nameCorporation
                                }
                                createPriceEventLiveData.postValue(CreatePriceEvent.SuccessNext(priceModel))

                            }.onFailure { exception ->
                                when (exception) {
                                    is ScheduleDateForPastException -> {
                                        createPriceStateLiveData.postValue(createPriceStateLiveData.value?.copy(messageError = context.getString(R.string.not_possible_adjust_date)))
                                    }
                                    is StLeastOneHourException -> {
                                        createPriceStateLiveData.postValue(
                                            createPriceStateLiveData.value?.copy(
                                                messageError = context.getString(
                                                    R.string.not_possible_adjust_date_on_hour_later, "1"
                                                )
                                            )
                                        )
                                    }
                                    is MinTwoProvidersInPriceException -> {
                                        createPriceStateLiveData.postValue(createPriceStateLiveData.value?.copy(messageError = context.getString(R.string.min_two_partners)))
                                    }
                                    is PriorityIsEmptyException -> {
                                        createPriceStateLiveData.postValue(createPriceStateLiveData.value?.copy(messageError = context.getString(R.string.priority_is_empty)))
                                    }
                                    is StLeastOneDayException -> {
                                        createPriceStateLiveData.postValue(
                                            createPriceStateLiveData.value?.copy(
                                                messageError = context.getString(
                                                    R.string.not_possible_adjust_date_on_hour_later, "24"
                                                )
                                            )
                                        )
                                    }
                                    is DateDeliveryBeforeDateFinishPriceException -> {
                                        createPriceStateLiveData.postValue(
                                            createPriceStateLiveData.value?.copy(
                                                messageError = context.getString(R.string.error_date_finish_before_date_delivery)
                                            )
                                        )
                                    }
                                }
                            }
                    }
                }.onFailure {

                }

        }
    }

    fun tapOnSelectAll(checked: Boolean) {
        showEffect = false
        createPriceEventLiveData.postValue(CreatePriceEvent.AllPartners)
        createPriceEventLiveData.postValue(CreatePriceEvent.ChangeSwitch(true))
    }

    fun checkAllPartners(listPartner: MutableList<PartnerModel>, notShowEffect: Boolean) {
        val partnersChecked = listPartner.filter { it.isChecked }
        if (partnersChecked.size == listPartner.size) {
            createPriceEventLiveData.postValue(CreatePriceEvent.ChangeSwitch(true))
        } else {
            createPriceEventLiveData.postValue(CreatePriceEvent.ChangeSwitch(false))
        }
        showEffect = false

    }

    fun updateHourFinishPrice(hour: Long, isFinishPrice: Boolean) {
        if (isFinishPrice) {
            createPriceStateLiveData.postValue(createPriceStateLiveData.value?.copy(dateFinishPrice = hour.toFormattedDateTime()))
        } else {
            createPriceStateLiveData.postValue(createPriceStateLiveData.value?.copy(dateDelivery = hour.toFormattedDateTime()))
        }
    }

    fun selectPriority(checkHigh: Boolean, checkAverage: Boolean, checkLow: Boolean): PriorityPrice? {
        return if (checkHigh) {
            PriorityPrice.HIGH
        } else if (checkAverage) {
            PriorityPrice.AVERAGE
        } else if (checkLow) {
            PriorityPrice.LOW
        } else {
            null
        }
    }
}