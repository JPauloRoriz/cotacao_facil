package com.example.cotacaofacil.presentation.ui.fragment.buyer

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.cotacaofacil.R
import com.example.cotacaofacil.databinding.FragmentPriceBuyerBinding
import com.example.cotacaofacil.domain.model.PriceModel
import com.example.cotacaofacil.domain.model.StatusPrice
import com.example.cotacaofacil.presentation.ui.activity.CreatePriceActivity
import com.example.cotacaofacil.presentation.ui.activity.CreatePriceActivity.Companion.SUCCESS_CREATE_PRICE
import com.example.cotacaofacil.presentation.ui.activity.PriceInfoActivity
import com.example.cotacaofacil.presentation.ui.activity.PriceInfoActivity.Companion.CNPJ_USER
import com.example.cotacaofacil.presentation.ui.activity.PriceInfoActivity.Companion.CODE_PRICE_SHOW
import com.example.cotacaofacil.presentation.ui.activity.PriceInfoActivity.Companion.UPDATE_PRICES
import com.example.cotacaofacil.presentation.ui.activity.ResolveConflictActivity
import com.example.cotacaofacil.presentation.ui.adapter.PriceBuyerAdapter
import com.example.cotacaofacil.presentation.ui.dialog.ConfirmationCreatePriceDialog
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.PriceBuyerViewModel
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractPrice.PriceEvent
import org.koin.androidx.viewmodel.ext.android.viewModel

class PriceBuyerFragment : Fragment() {
    private lateinit var binding: FragmentPriceBuyerBinding
    private val viewModel by viewModel<PriceBuyerViewModel>()
    private val adapter = PriceBuyerAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPriceBuyerBinding.inflate(inflater, container, false)
        binding.recycleViewPartner.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.priceEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                PriceEvent.CreatePrice -> {
                    startActivityForResult(Intent(requireActivity(), CreatePriceActivity::class.java), SUCCESS_CREATE_PRICE)

                }
                is PriceEvent.ShowDialogSuccess -> ConfirmationCreatePriceDialog.newInstance(event.code).show(childFragmentManager, "")
                is PriceEvent.TapOnPriceCanceled -> {
                    showInfoPrice(codePrice = event.priceModel.code, cnpjBuyerCreator = event.priceModel.cnpjBuyerCreator)
                }
                is PriceEvent.TapOnPriceOpen -> {
                    showInfoPrice(codePrice = event.priceModel.code, cnpjBuyerCreator = event.priceModel.cnpjBuyerCreator)
                }
                is PriceEvent.TapOnPricePendency -> {
                    val alert = AlertDialog.Builder(requireContext(), R.style.MyDialogTheme)
                    alert.setMessage(
                        getString(
                            R.string.find_conflict_price
                        )
                    ).setTitle(getString(R.string.attention)).setNegativeButton(getString(R.string.resolve_later)) { dialog, int -> }
                        .setPositiveButton(R.string.resolve_now) { dialog, int ->
                            showConflicts(event.priceModel)
                        }.show()
                }
                is PriceEvent.ReolveConflictSuccess -> Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                is PriceEvent.TapOnPriceFinished -> {
                    showFinishPrice(priceModel = event.priceModel)
                }
            }
        }
        viewModel.priceState.observe(viewLifecycleOwner) { state ->
            binding.tvMessageError.text = state.messageError
            adapter.updateList(state.pricesModel)
            binding.progressBar.isVisible = state.showProgressBar
        }
    }

    private fun showFinishPrice(priceModel: PriceModel) {
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_out_right)
            .setPopExitAnim(R.anim.slide_in_left)
            .build()
        val bundle = Bundle()
        bundle.putParcelable(CreatePriceSelectProductsFragment.PRICE_MODEL, priceModel)
        findNavController().navigate(R.id.winnerPriceFragment, bundle, navOptions)
    }

    private fun showConflicts(priceModel: PriceModel) {
        val intent = Intent(requireContext(), ResolveConflictActivity::class.java)
        intent.putExtra(ResolveConflictActivity.PRICE_MODEL_CONFLICT, priceModel)
        startActivityForResult(intent, ResolveConflictActivity.RESOLVE_CONFLICT)
    }

    private fun showInfoPrice(codePrice: String, cnpjBuyerCreator: String) {
        showScreenConflict(codePrice = codePrice, cnpjBuyerCreator = cnpjBuyerCreator)
    }

    private fun showScreenConflict(codePrice: String, cnpjBuyerCreator: String) {
        val intent = Intent(requireContext(), PriceInfoActivity::class.java)
        val bundle = Bundle()
        bundle.putString(CODE_PRICE_SHOW, codePrice)
        bundle.putString(CNPJ_USER, cnpjBuyerCreator)
        intent.putExtras(bundle)
        startActivityForResult(intent, UPDATE_PRICES)
    }

    private fun setupListeners() {
        binding.arrow.setOnClickListener {
            activity?.onBackPressed()
        }
        adapter.clickPrice = {
            viewModel.tapOnPrice(priceModel = it)
        }
        binding.buttonAddPrice.setOnClickListener {
            viewModel.tapOnCreatePrice()
        }
    }

    override fun onResume() {
//        viewModel.updateListPrices()
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SUCCESS_CREATE_PRICE && resultCode == RESULT_OK) {
            data?.getStringExtra(CODE_PRICE)?.let {
                viewModel.updateListPrices()
                viewModel.showDialogSuccess(it)
            }
        }
        if (requestCode == UPDATE_PRICES && resultCode == RESULT_OK) {
            val messageError = data?.getStringExtra(PriceInfoActivity.MESSAGE_ERROR)
            viewModel.updateListPrices()
            Toast.makeText(requireContext(), getString(R.string.price_finished_or_canceled_success, messageError), Toast.LENGTH_LONG).show()
        }
        if (requestCode == ResolveConflictActivity.RESOLVE_CONFLICT && resultCode == RESULT_OK) {
            data?.let {
                val priceModel = it.getParcelableExtra<PriceModel>(ResolveConflictActivity.PRICE_MODEL_CONFLICT)
                viewModel.cancelOrFinishPrice(statusPrice = StatusPrice.FINISHED, priceModelEdit = priceModel)
            } ?: errorResolveConflict()
        }
    }

    private fun errorResolveConflict() {
        Toast.makeText(requireContext(), getString(R.string.error_save_winner), Toast.LENGTH_SHORT).show()
    }


    companion object {
        const val CODE_PRICE = "CODE_PRICE"
    }
}