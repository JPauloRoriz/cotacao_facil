package com.example.cotacaofacil.presentation.ui.fragment.provider

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.cotacaofacil.R
import com.example.cotacaofacil.databinding.FragmentPriceProviderBinding
import com.example.cotacaofacil.domain.mapper.toPriceEditModel
import com.example.cotacaofacil.presentation.ui.activity.ParticipatePriceProviderActivity
import com.example.cotacaofacil.presentation.ui.activity.ParticipatePriceProviderActivity.Companion.PRICE_MODEL
import com.example.cotacaofacil.presentation.ui.adapter.PriceProviderAdapter
import com.example.cotacaofacil.presentation.viewmodel.provider.price.PriceProviderViewModel
import com.example.cotacaofacil.presentation.viewmodel.provider.price.contract.pricesProviderContract.PricePartnerEvent
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class PricesProviderFragment : Fragment() {
    private val viewModel by viewModel<PriceProviderViewModel>()
    private lateinit var binding: FragmentPriceProviderBinding
    private val adapter by lazy { PriceProviderAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPriceProviderBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            binding.recycleViewPartner.adapter = adapter
            adapter.updateList(state.pricesModel)
            binding.progressBar.isVisible = state.showProgressBar
            binding.textViewMessageError.text = state.messageError
        }

        viewModel.eventLiveData.observe(viewLifecycleOwner) { event ->
            when (event) {
                is PricePartnerEvent.TapOnPriceFinishedOrCanceled -> Toast.makeText(
                    requireContext(),
                    "cotação finalizada ou cancelada",
                    Toast.LENGTH_SHORT
                ).show()
                is PricePartnerEvent.TapOnPriceOpen -> {
                    val intent = Intent(requireContext(), ParticipatePriceProviderActivity::class.java)
                    val bundle = Bundle()
                    bundle.putParcelable(PRICE_MODEL, event.priceModel.toPriceEditModel(cnpjPartner = event.cnpjUser))
                    intent.putExtras(bundle)
                    startActivityForResult(intent, UPDATE_PRICES)
                }
                is PricePartnerEvent.SendCnpjToAdapter -> adapter.cnpjProvider = event.cnpjProvider
                is PricePartnerEvent.TapOnPriceFinishedPendency -> {
                    Toast.makeText(
                        requireContext(),
                        requireContext().getString(R.string.price_finished_message_finished_conflict, event.priceModel.cnpjBuyerCreator),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { viewModel.tapOnTab(it) }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        adapter.clickPrice =
            {
                viewModel.tapOnPrice(it)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_PRICES && resultCode == RESULT_OK) {
            viewModel.updateListPrices(binding.tabLayout.selectedTabPosition)
        }
    }


    companion object {
        private const val UPDATE_PRICES = 545
    }
}