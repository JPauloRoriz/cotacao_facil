package com.example.cotacaofacil.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.cotacaofacil.data.helper.BodyCompanyHelper
import com.example.cotacaofacil.data.helper.SpinnerListHelper
import com.example.cotacaofacil.data.helper.UserHelper
import com.example.cotacaofacil.data.repository.bodyCompany.BodyCompanyRepositoryImpl
import com.example.cotacaofacil.data.repository.bodyCompany.contract.BodyCompanyRepository
import com.example.cotacaofacil.data.repository.dete.DateCurrentRepositoryImpl
import com.example.cotacaofacil.data.repository.dete.contract.DateCurrentRepository
import com.example.cotacaofacil.data.repository.history.HistoryRepositoryImpl
import com.example.cotacaofacil.data.repository.history.contract.HistoryRepository
import com.example.cotacaofacil.data.repository.partner.PartnerRepositoryImpl
import com.example.cotacaofacil.data.repository.partner.contract.PartnerRepository
import com.example.cotacaofacil.data.repository.price.PriceRepository
import com.example.cotacaofacil.data.repository.price.PriceRepositoryImpl
import com.example.cotacaofacil.data.repository.product.ProductRepositoryImpl
import com.example.cotacaofacil.data.repository.product.contract.ProductRepository
import com.example.cotacaofacil.data.repository.storage.StorageRepositoryImpl
import com.example.cotacaofacil.data.repository.storage.contract.StorageRepository
import com.example.cotacaofacil.data.repository.user.UserRepositoryImpl
import com.example.cotacaofacil.data.repository.user.contract.UserRepository
import com.example.cotacaofacil.data.service.cnpj.BodyCompanyServiceImpl
import com.example.cotacaofacil.data.service.cnpj.CnpjServiceImpl
import com.example.cotacaofacil.data.service.cnpj.contract.BodyCompanyService
import com.example.cotacaofacil.data.service.date.DateCurrentServiceServiceImpl
import com.example.cotacaofacil.data.service.date.contract.DateCurrentService
import com.example.cotacaofacil.data.service.history.HistoryServiceImpl
import com.example.cotacaofacil.data.service.history.contract.HistoryService
import com.example.cotacaofacil.data.service.partner.PartnerServiceImpl
import com.example.cotacaofacil.data.service.partner.contract.PartnerService
import com.example.cotacaofacil.data.service.price.PriceServiceImpl
import com.example.cotacaofacil.data.service.price.contract.PriceService
import com.example.cotacaofacil.data.service.product.ProductServiceImpl
import com.example.cotacaofacil.data.service.product.contract.ProductService
import com.example.cotacaofacil.data.service.settings.retrofitConfig
import com.example.cotacaofacil.data.service.storage.StorageServiceImpl
import com.example.cotacaofacil.data.service.storage.contract.StorageService
import com.example.cotacaofacil.data.service.user.UserFirebaseService
import com.example.cotacaofacil.data.service.user.contract.UserService
import com.example.cotacaofacil.data.sharedPreferences.SharedPreferencesHelper
import com.example.cotacaofacil.domain.model.PriceEditModel
import com.example.cotacaofacil.domain.model.PriceModel
import com.example.cotacaofacil.domain.usecase.date.CalculationDateFinishPriceUseCaseImpl
import com.example.cotacaofacil.domain.usecase.date.DateCurrentUseCaseImpl
import com.example.cotacaofacil.domain.usecase.date.ValidationNextCreatePriceUseCaseImpl
import com.example.cotacaofacil.domain.usecase.date.contract.CalculationDateFinishPriceUseCase
import com.example.cotacaofacil.domain.usecase.date.contract.DateCurrentUseCase
import com.example.cotacaofacil.domain.usecase.date.contract.ValidationNextCreatePriceUseCase
import com.example.cotacaofacil.domain.usecase.historic.AddHistoricUseCaseImpl
import com.example.cotacaofacil.domain.usecase.historic.DeleteHistoricUseCaseImpl
import com.example.cotacaofacil.domain.usecase.historic.GetAllItemHistoricUseCaseImpl
import com.example.cotacaofacil.domain.usecase.historic.contract.AddHistoricUseCase
import com.example.cotacaofacil.domain.usecase.historic.contract.DeleteHistoricUseCase
import com.example.cotacaofacil.domain.usecase.historic.contract.GetAllItemHistoricUseCase
import com.example.cotacaofacil.domain.usecase.home.EditImageProfileUseCaseImpl
import com.example.cotacaofacil.domain.usecase.home.GetAllImageProfileUseCaseImpl
import com.example.cotacaofacil.domain.usecase.home.GetBodyCompanyModelUseCaseImpl
import com.example.cotacaofacil.domain.usecase.home.GetImageProfileUseCaseImpl
import com.example.cotacaofacil.domain.usecase.home.contract.EditImageProfileUseCase
import com.example.cotacaofacil.domain.usecase.home.contract.GetAllImageProfileUseCase
import com.example.cotacaofacil.domain.usecase.home.contract.GetBodyCompanyModelUseCase
import com.example.cotacaofacil.domain.usecase.home.contract.GetImageProfileUseCase
import com.example.cotacaofacil.domain.usecase.login.LoginUseCaseImpl
import com.example.cotacaofacil.domain.usecase.login.contract.LoginUseCase
import com.example.cotacaofacil.domain.usecase.partner.*
import com.example.cotacaofacil.domain.usecase.partner.contract.*
import com.example.cotacaofacil.domain.usecase.price.*
import com.example.cotacaofacil.domain.usecase.price.contract.*
import com.example.cotacaofacil.domain.usecase.product.*
import com.example.cotacaofacil.domain.usecase.product.contract.*
import com.example.cotacaofacil.domain.usecase.register.RegisterUseCaseImpl
import com.example.cotacaofacil.domain.usecase.register.contract.RegisterUseCase
import com.example.cotacaofacil.presentation.viewmodel.buyer.home.HomeBuyerViewModel
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.*
import com.example.cotacaofacil.presentation.viewmodel.history.HistoryViewModel
import com.example.cotacaofacil.presentation.viewmodel.login.LoginViewModel
import com.example.cotacaofacil.presentation.viewmodel.partner.PartnerViewModel
import com.example.cotacaofacil.presentation.viewmodel.product.AddProductViewModel
import com.example.cotacaofacil.presentation.viewmodel.product.StockBuyerViewModel
import com.example.cotacaofacil.presentation.viewmodel.provider.home.HomeProviderViewModel
import com.example.cotacaofacil.presentation.viewmodel.provider.price.ParticipatePriceProviderViewModel
import com.example.cotacaofacil.presentation.viewmodel.provider.price.PriceProviderViewModel
import com.example.cotacaofacil.presentation.viewmodel.register.RegisterViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit


val appModule = module {

    //viewModel
    viewModel { LoginViewModel(get(), get(), get(), get()) }
    viewModel { RegisterViewModel(get(), get()) }
    viewModel { HistoryViewModel(get(), get(), get(), get()) }
    viewModel { PartnerViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { AddProductViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { StockBuyerViewModel(get(), get(), get(), get(), get()) }
    viewModel { HomeBuyerViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { HomeProviderViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { PriceBuyerViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { PriceProviderViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { CreatePriceViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { EditDateHourViewModel(get(), get()) }
    viewModel { (priceModel: PriceModel) -> SelectProductsViewModel(get(), get(), get(), get(), priceModel = priceModel) }
    viewModel { (priceModel: PriceModel) -> ReportInitPriceViewModel(priceModel = priceModel, get(), get(), get(), get()) }
    viewModel { (codePrice: String, cnpjBuyerCreator: String) ->
        PriceInfoViewModel(
            codePrice = codePrice,
            cnpjBuyerCreator = cnpjBuyerCreator,
            getPriceByCodeUseCase = get(),
            context = get(),
            currentDateUseCase = get(),
            editPriceUseCase = get()
        )
    }
    viewModel { (priceEditModel: PriceEditModel) ->
        ParticipatePriceProviderViewModel(
            userHelper = get(),
            priceEditModel = priceEditModel,
            context = get(),
            dateCurrentUseCase = get(),
            setPricePartnerUseCase = get(),
        )
    }

    viewModel { (priceModel : PriceModel) ->
        ResolveConflictViewModel(
            context = get(),
            getImageProfileUseCase = get(),
            priceModel = priceModel
        )
    }
    viewModel { (priceModel : PriceModel) ->
        WinnerPriceViewModel( price = priceModel, get(), get())
    }


    //useCase
    factory<RegisterUseCase> { RegisterUseCaseImpl(get(), get(), get()) }
    factory<LoginUseCase> { LoginUseCaseImpl(get(), get()) }
    factory<ValidationCnpjUseCase> { ValidationCnpjUseCaseImpl(get()) }
    factory<GetAllPartnerModelUseCase> { GetAllPartnerModelUseCaseImpl(get()) }
    factory<AddRequestPartnerUseCase> { AddRequestRequestPartnerUseCaseImpl(get(), get(), get()) }
    factory<RejectRequestPartnerUseCase> { RejectRequestPartnerUseCaseImpl(get(), get(), get()) }
    factory<AcceptRequestPartnerUseCase> { AcceptRequestPartnerUseCaseImpl(get(), get(), get()) }
    factory<GetAllItemHistoricUseCase> { GetAllItemHistoricUseCaseImpl(get()) }
    factory<GetAllListSpinnerOptionsUseCase> { GetAllListSpinnerOptionsUseCaseImpl(get()) }
    factory<SaveProductionUseCase> { SaveProductionUseCaseImpl(get()) }
    factory<GetAllByCnpjProductsUseCase> { GetAllByCnpjProductsUseCaseImpl(get()) }
    factory<GetBodyCompanyModelUseCase> { GetBodyCompanyModelUseCaseImpl(get()) }
    factory<DeleteHistoricUseCase> { DeleteHistoricUseCaseImpl(get()) }
    factory<ChangeFavoriteProductUseCase> { ChangeFavoriteProductUseCaseImpl(get()) }
    factory<EditProductUseCase> { EditProductUseCaseImpl(get()) }
    factory<DeleteProductUseCase> { DeleteProductUseCaseImpl(get()) }
    factory<DateCurrentUseCase> { DateCurrentUseCaseImpl(get()) }
    factory<CalculationDateFinishPriceUseCase> { CalculationDateFinishPriceUseCaseImpl() }
    factory<ValidationNextCreatePriceUseCase> { ValidationNextCreatePriceUseCaseImpl() }
    factory<CreatePriceUseCase> { CreatePriceUseCaseImpl(get()) }
    factory<AddHistoricUseCase> { AddHistoricUseCaseImpl(get()) }
    factory<GetPricesBuyerUserCase> { GetPricesBuyerUserCaseImpl(get()) }
    factory<GetPricesProviderUseCase> { GetPricesProviderUseCaseImpl(get()) }
    factory<ValidationStatusPriceUseCase> { ValidationStatusPriceUseCaseImpl(get(), get()) }
    factory<ValidationPricesProviderUseCase> { ValidationPricesProviderUseCaseImpl(get()) }
    factory<UpdateHourPricesUseCase> { UpdateHourPricesUseCaseImpl(get(), get()) }
    factory<EditPriceUseCase> { EditPriceUseCaseImpl(get()) }
    factory<SetPricePartnerUseCase> { SetPricePartnerUseCaseUseCaseImpl(get(), get()) }
    factory<GetPriceByCodeUseCase> { GetPriceByCodeUseCaseImpl(get()) }
    factory<EditImageProfileUseCase> { EditImageProfileUseCaseImpl(get()) }
    factory<GetImageProfileUseCase> { GetImageProfileUseCaseImpl(get()) }
    factory<GetAllImageProfileUseCase> { GetAllImageProfileUseCaseImpl(get()) }


    //repository
    factory<UserRepository> { UserRepositoryImpl(get()) }
    factory<BodyCompanyRepository> { BodyCompanyRepositoryImpl(get(), get()) }
    factory<PartnerRepository> { PartnerRepositoryImpl(get()) }
    factory<HistoryRepository> { HistoryRepositoryImpl(get()) }
    factory<ProductRepository> { ProductRepositoryImpl(get(), get(), get()) }
    factory<DateCurrentRepository> { DateCurrentRepositoryImpl(get()) }
    factory<PriceRepository> { PriceRepositoryImpl(get()) }
    factory<StorageRepository> { StorageRepositoryImpl(get()) }


    //service
    single<UserService> { UserFirebaseService(get(), get()) }
    single<PartnerService> { PartnerServiceImpl(get()) }
    single<BodyCompanyService> { BodyCompanyServiceImpl(get()) }
    single<HistoryService> { HistoryServiceImpl(get(), get()) }
    single<ProductService> { ProductServiceImpl(get()) }
    single<DateCurrentService> { DateCurrentServiceServiceImpl() }
    single<PriceService> { PriceServiceImpl(get(), get()) }
    single<StorageService> { StorageServiceImpl(get()) }

    //helper
    single { SpinnerListHelper() }
    single { UserHelper() }
    single { BodyCompanyHelper() }
    single { SharedPreferencesHelper(get(), get()) }

    single { retrofitConfig }
    single { get<Retrofit>().create(CnpjServiceImpl::class.java) }
    single { Firebase.firestore }
    single { Firebase.auth }
    single { FirebaseStorage.getInstance() }
    single<SharedPreferences> { androidContext().getSharedPreferences("my_prefs", Context.MODE_PRIVATE) }
    single {
        val context: Context = androidContext()
        val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        EncryptedSharedPreferences.create(
            context,
            context.packageName + "_preferences",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }
}