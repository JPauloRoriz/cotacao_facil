package com.example.cotacaofacil.presentation.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import com.example.cotacaofacil.R
import com.example.cotacaofacil.databinding.ActivityStockBuyerBinding
import com.example.cotacaofacil.domain.model.UserModel
import com.example.cotacaofacil.presentation.ui.adapter.ProductAdapter
import com.example.cotacaofacil.presentation.ui.dialog.AddProductBottomSheetDialogFragment
import com.example.cotacaofacil.presentation.viewmodel.product.StockBuyerViewModel
import com.example.cotacaofacil.presentation.viewmodel.product.model.StockEvent
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class StockBuyerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStockBuyerBinding
    val user by lazy { intent?.extras?.getParcelable<UserModel>(USER) }
    private val viewModel: StockBuyerViewModel by viewModel { parametersOf(user) }
    private val productAdapter = ProductAdapter()
    private val fragmentManager = supportFragmentManager
    private var isShowDialog = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStockBuyerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recycleViewProducts.adapter = productAdapter


        setupListeners()
        setupObservers()
    }


    private fun setupObservers() {
        viewModel.stateLiveData.observe(this) { state ->
            binding.progressBar.isVisible = state.isLoading
            binding.recycleViewProducts.visibility = state.showListProducts
            binding.textViewMessageError.visibility = state.showError
            binding.imageButtonReload.visibility = state.showError
            binding.searchView.queryHint = state.hintTextFindProduct
            productAdapter.updateList(state.productsList)
        }

        viewModel.eventLiveData.observe(this) { event ->
            when (event) {
                is StockEvent.StockEmptyEvent -> {
                    Toast.makeText(this, getString(R.string.stock_empty_message), Toast.LENGTH_SHORT).show()
                    this.finish()
                }
                is StockEvent.UpdateList -> {
                    productAdapter.updateList(event.products)
                }
                StockEvent.SelectedFilter -> {
                    binding.searchView.setQuery("", true)
                }
                is StockEvent.EditProduct -> {
                    if (!isShowDialog){
                        AddProductBottomSheetDialogFragment.newInstance(
                            cnpjUser = user?.cnpj,
                            productModel = event.product,
                            updateListProducts = { viewModel.initViewModel(true) },
                            dismissDialog = { isShowDialog = false }
                        ).show(fragmentManager, EDIT_PRODUCT_BOTTOM_SHEET)
                    }
                    isShowDialog = true
                }
                is StockEvent.FeedbackMessage -> {
                    Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
                }

            }
        }

    }

    private fun setupListeners() {
        binding.arrow.setOnClickListener {
            onBackPressed()
        }
        productAdapter.clickFavorite = {
            viewModel.clickFavorite(it)
        }
        productAdapter.clickEditProduct = {
            viewModel.tapOnProduct(it)
        }
        productAdapter.clickDeleteProduct = {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.delete_product))
            builder.setPositiveButton(R.string.yes) { p0, p1 ->
                viewModel.tapOnTrash(it)
            }
            builder.setNegativeButton(R.string.not) { p0, p1 ->
            }
            builder.create().show()

        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        viewModel.initViewModel(true)
                    }
                    1 -> {
                        viewModel.initViewModel(false)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        binding.imageButtonReload.setOnClickListener {
            viewModel.initViewModel(true)
        }

        binding.floatingButtonAdd.setOnClickListener {
            if(!isShowDialog){
                AddProductBottomSheetDialogFragment.newInstance(
                    cnpjUser = user?.cnpj,
                    productModel = null,
                    updateListProducts = { viewModel.initViewModel(true) },
                    dismissDialog = { isShowDialog = false }
                ).show(fragmentManager, ADD_PRODUCT_BOTTOM_SHEET)
            }
            isShowDialog = true
        }

        binding.filterButton.setOnClickListener {
            val options = arrayOf(getString(R.string.code_product), getString(R.string.name_product), getString(R.string.brand_product))

            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.select_filter_find))
            builder.setItems(options) { _, which ->
                viewModel.tapOnSelectedFilter(which)
            }
            builder.create().show()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                viewModel.performSearch(p0, binding.tabLayout.selectedTabPosition)
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                viewModel.performSearch(p0, binding.tabLayout.selectedTabPosition)
                return true
            }

        })


    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        finish()
        super.onBackPressed()
    }

    override fun onResume() {
        if (user == null) {finish()}
        super.onResume()
    }

    companion object {
        const val USER = "user"
        const val QUANTITY_PRODUCTS = "QUANTITY_PRODUCTS"
        const val ADD_PRODUCT_BOTTOM_SHEET = "AddProductBottomSheetDialogFragment"
        const val EDIT_PRODUCT_BOTTOM_SHEET = "EditProductBottomSheetDialogFragment"
    }
}