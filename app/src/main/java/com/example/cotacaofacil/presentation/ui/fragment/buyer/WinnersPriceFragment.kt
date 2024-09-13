package com.example.cotacaofacil.presentation.ui.fragment.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.cotacaofacil.databinding.FragmentWinnersPriceBinding
import com.example.cotacaofacil.domain.model.PriceModel
import com.example.cotacaofacil.presentation.ui.adapter.PriceWinnersAdapter
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.WinnerPriceViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class WinnersPriceFragment : Fragment() {
    private lateinit var binding: FragmentWinnersPriceBinding
    private val priceModel by lazy { arguments?.getParcelable<PriceModel>(CreatePriceSelectProductsFragment.PRICE_MODEL) }
    val adapter = PriceWinnersAdapter()
    val viewModel: WinnerPriceViewModel by viewModel { parametersOf(priceModel) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWinnersPriceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycleViewWinners.adapter = adapter

        viewModel.startScreen()
        setupListeners()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.stateLiveData.observe(viewLifecycleOwner){ state ->
            adapter.updateList(state.pricesWinners)
            binding.textViewToolbar.text = state.textToolbar
            binding.progressBar.isVisible = state.isLoading
        }
    }

    private fun setupListeners() {
        binding.arrow.setOnClickListener {
            activity?.onBackPressed()
        }
    }

}