package com.example.cotacaofacil.presentation.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.cotacaofacil.R
import com.example.cotacaofacil.data.helper.UserHelper
import com.example.cotacaofacil.databinding.ActivityParticipatePriceProviderBinding
import com.example.cotacaofacil.domain.model.PriceEditModel
import com.example.cotacaofacil.domain.model.ProductPriceEditPriceModel
import com.example.cotacaofacil.presentation.ui.adapter.ItemTableProductAdapter
import com.example.cotacaofacil.presentation.viewmodel.provider.price.ParticipatePriceProviderViewModel
import com.example.cotacaofacil.presentation.viewmodel.provider.price.contract.participatePricesProviderContract.ParticipatePriceEvent
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ParticipatePriceProviderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityParticipatePriceProviderBinding
    private val userHelper by inject<UserHelper>()
    private val priceEditModel by lazy { intent.extras?.getParcelable(PRICE_MODEL) ?: PriceEditModel() }
    val user by lazy { userHelper.user }
    private val viewModel: ParticipatePriceProviderViewModel by viewModel { parametersOf(priceEditModel) }
    private val allProductsPriceAdapter by lazy { ItemTableProductAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParticipatePriceProviderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupListeners()
        setupObservers()
    }

    private fun setupView() {
        binding.recyclerViewAllProducts.adapter = allProductsPriceAdapter
    }

    private fun setupListeners() {
        binding.arrow.setOnClickListener {
            finish()
        }

        binding.includedProduct.editTextValueProduct.addTextChangedListener { text ->
            viewModel.editProduct(text, allProductsPriceAdapter.listTableProduct, allProductsPriceAdapter.positionSelect)
        }

        binding.btnSavePrice.setOnClickListener {
            viewModel.tapOnRegisterPrices()
        }

        allProductsPriceAdapter.clickItem = { productPriceModel ->
            viewModel.tapOnProductPriceModel(productPriceModel)
        }

        binding.imageButtonArrowLeft.setOnClickListener {
            viewModel.tapOnArrowLeft(allProductsPriceAdapter.positionSelect, allProductsPriceAdapter.listTableProduct)
        }
        binding.imageButtonArrowRight.setOnClickListener {
            viewModel.tapOnArrowRight(allProductsPriceAdapter.positionSelect, allProductsPriceAdapter.listTableProduct)
        }
    }

    private fun setupObservers() {
        viewModel.stateLiveData.observe(this) { state ->
            binding.textViewToolbar.text = state.textViewCodePrice
            allProductsPriceAdapter.updateList(state.productsPricesModel)
            allProductsPriceAdapter.updateProduct(allProductsPriceAdapter.listTableProduct.indexOf(state.productEditSelect))
            allProductsPriceAdapter.isEditable = false
            setProductSelect(state.productEditSelect)
            binding.imageButtonArrowLeft.isVisible = allProductsPriceAdapter.positionSelect != 0
            binding.imageButtonArrowRight.isVisible = allProductsPriceAdapter.positionSelect != allProductsPriceAdapter.listTableProduct.size.minus(1)
            binding.includedProduct.editTextValueProduct.setText(state.productEditSelect.price.toString())
            binding.progressBar.isVisible = state.isLoading
        }

        viewModel.eventLiveData.observe(this) { event ->
            when (event) {
                is ParticipatePriceEvent.EditPriceSuccess ->{
                    Toast.makeText(this, event.codePrice, Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is ParticipatePriceEvent.ErrorSetPrice -> Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setProductSelect(productPriceModel: ProductPriceEditPriceModel) {
        binding.includedProduct.apply {
            textViewCodeProduct.text = getString(R.string.ccd_product, productPriceModel.productModel.code)
            textViewNameProduct.text = productPriceModel.productModel.name
            textViewTextDescription.text = productPriceModel.productModel.description
            textViewQuantityProduct.text =
                getString(R.string.quantity_products_editable, productPriceModel.quantityProducts)
            imageViewIsSelected.isVisible = productPriceModel.price > 0.0
        }
    }

    override fun onResume() {
        if(user == null){
            //criar uma viewmodel e se for nulo buscar o user da mesma forma que o login buscou para passar para cá
            finish()
        }
        super.onResume()
    }

    companion object {
        const val PRICE_MODEL = "price_model"
    }
}
