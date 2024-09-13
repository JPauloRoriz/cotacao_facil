package com.example.cotacaofacil.presentation.ui.fragment.buyer

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.cotacaofacil.databinding.FragmentReportInitPriceBinding
import com.example.cotacaofacil.domain.model.PriceModel
import com.example.cotacaofacil.presentation.ui.adapter.ItemTableProductAdapter
import com.example.cotacaofacil.presentation.ui.fragment.buyer.PriceBuyerFragment.Companion.CODE_PRICE
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.ReportInitPriceViewModel
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractReportInitPrice.EventReportInitPriceLiveData
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ReportInitPriceFragment : Fragment() {
    private lateinit var binding: FragmentReportInitPriceBinding
    private val priceModel by lazy { arguments?.getParcelable<PriceModel>(CreatePriceSelectProductsFragment.PRICE_MODEL) }
    val viewModel: ReportInitPriceViewModel by viewModel { parametersOf(priceModel) }
    val adapter = ItemTableProductAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReportInitPriceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycleViewProducts.adapter = adapter

        setupListeners()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            binding.textViewTotalProductsValue.text = state.textValueVariablesProducts
            binding.textViewQuantityProductsValue.text = state.textValueQuantityProducts
            binding.textViewDateInitPriceValue.text = state.textDateInitPrice
            binding.textViewDateDeliveryPriceValue.text = state.textDateDeliveryPrice
            binding.textViewDateFinishPriceValue.text = state.textDateFinishPrice
            binding.textViewPriorityPriceValue.setCompoundDrawablesRelativeWithIntrinsicBounds(state.colorPriorityPrice, null, null, null)
            binding.textViewPriorityPriceValue.text = state.textPriorityPrice
            binding.textViewDescrition.text = state.descriptionPrice
            binding.progressBar.isVisible = state.isLoading
        }

        viewModel.eventLiveData.observe(viewLifecycleOwner) { event ->
            when (event) {
                is EventReportInitPriceLiveData.UpdateListProducts -> adapter.updateList(event.productPriceModelList)
                is EventReportInitPriceLiveData.SuccessAddPrice -> {
                    val resultIntent = Intent()
                    resultIntent.putExtra(CODE_PRICE, event.codePrice)
                    activity?.setResult(RESULT_OK, resultIntent)
                    activity?.finish()
                }
            }
        }
    }

    private fun setupListeners() {
        adapter.updateQuantity = {
            viewModel.updateValuesTotal(it)
        }

        binding.buttonNext.setOnClickListener {
            viewModel.tapOnInitPrice()
        }
    }
}