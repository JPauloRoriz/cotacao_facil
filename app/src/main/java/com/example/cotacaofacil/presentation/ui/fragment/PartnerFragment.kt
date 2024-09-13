package com.example.cotacaofacil.presentation.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.cotacaofacil.R
import com.example.cotacaofacil.databinding.FragmentPartnerBinding
import com.example.cotacaofacil.domain.usecase.partner.util.TypeDeletePartner
import com.example.cotacaofacil.presentation.ui.adapter.PartnerAdapter
import com.example.cotacaofacil.presentation.util.Maks
import com.example.cotacaofacil.presentation.viewmodel.partner.PartnerViewModel
import com.example.cotacaofacil.presentation.viewmodel.partner.model.PartnerEvent
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class PartnerFragment : Fragment() {
    private val viewModel: PartnerViewModel by viewModel()
    private lateinit var binding: FragmentPartnerBinding
    private val adapter by lazy { PartnerAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPartnerBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.imageViewNotification.setOnClickListener {
            viewModel.loadListPartnerModel(false)
        }
        binding.buttonAddNewPartner.setOnClickListener {
            lifecycleScope.launch {
                viewModel.tapOnAddNewPartner(
                    cnpj = binding.edtSearch.text.toString()
                )
            }

        }

        adapter.clickPartner = { partner ->
            viewModel.tapOnIconPartner(partner)

        }

        adapter.clickAcceptPartner = { partner ->
            viewModel.tapOnAcceptPartner(partner)

        }

        adapter.clickRejectPartner = { partner ->
            viewModel.tapOnRejectPartner(partner)
        }

        binding.arrow.setOnClickListener {
            viewModel.tapOnArrow()
        }

        viewModel.enterCnpj()
    }

    private fun setupObservers() {
        viewModel.eventLiveData.observe(viewLifecycleOwner) { event ->
            when (event) {
                is PartnerEvent.GoToAddNewPartnerSuccess -> {
                    binding.imageViewErrorEmpty.visibility = View.GONE
                    binding.tvMessageError.visibility = View.GONE
                    binding.recycleViewPartner.visibility = View.VISIBLE
                }
                is PartnerEvent.GoToAddNewPartnerError -> {
                    binding.imageViewErrorEmpty.visibility = View.VISIBLE
                    binding.tvMessageError.visibility = View.VISIBLE
                }
                is PartnerEvent.EnterCnpj -> {
                    binding.edtSearch.addTextChangedListener(Maks.insert(binding.edtSearch))
                    Maks.insert(binding.edtSearch)
                }
                is PartnerEvent.RequestAddPartner -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.request_send),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is PartnerEvent.DeletePartner -> {
                    val alert = AlertDialog.Builder(requireContext(), R.style.MyDialogTheme)
                    alert.setMessage(
                        getString(
                            R.string.text_delete_partner,
                            event.partner.nameFantasy?.lowercase()
                        )
                    ).setTitle(getString(R.string.attention)).setNegativeButton(
                        getString(R.string.not)
                    ) { dialog, int -> }.setPositiveButton(
                        R.string.yes
                    ) { dialog, int ->
                        lifecycleScope.launch {
                            viewModel.tapOnConfirmDeletePartner(event.partner)

                        }
                    }.show()
                }
                is PartnerEvent.RejectPartner -> {
                    val alert = AlertDialog.Builder(requireContext(), R.style.MyDialogTheme)
                    alert.setMessage(
                        getString(
                            R.string.text_reject_partner,
                            event.partner.nameFantasy?.lowercase()
                        )
                    ).setTitle(getString(R.string.attention)).setNegativeButton(
                        getString(R.string.not)
                    ) { dialog, int -> }.setPositiveButton(
                        R.string.yes
                    ) { dialog, int ->
                        lifecycleScope.launch {
                            viewModel.tapOnConfirmRejectPartner(event.partner, TypeDeletePartner.REJECT_PARTNER)
                        }
                    }.show()
                }

                is PartnerEvent.SuccessRejectPartner -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.request_reject_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is PartnerEvent.SuccessDeletePartner -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.request_delete_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is PartnerEvent.SuccessAcceptPartner -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.request_accept_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is PartnerEvent.FindEmpty -> {
                    viewModel.loadListPartnerModel(true)
                }
                is PartnerEvent.CancelRequestPartner -> {
                    val alert = AlertDialog.Builder(requireContext(), R.style.MyDialogTheme)
                    alert.setMessage(
                        getString(
                            R.string.text_to_give_up_partner,
                            event.partner.nameFantasy?.lowercase()
                        )
                    ).setTitle(getString(R.string.attention)).setNegativeButton(
                        getString(R.string.not)
                    ) { dialog, int -> }.setPositiveButton(
                        R.string.yes
                    ) { dialog, int ->
                        lifecycleScope.launch {
                            viewModel.tapOnConfirmRejectPartner(event.partner, TypeDeletePartner.CANCEL_REQUEST_PARTNER)
                        }
                    }.show()
                }
                is PartnerEvent.ErrorInternetConnection -> {
                    Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                }
                PartnerEvent.TapOnBack -> activity?.onBackPressed()
            }
        }

        viewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            adapter.listPartner = state.listPartnerModel
            binding.recycleViewPartner.adapter = adapter
            binding.tvMessageError.text = state.messageError
            binding.imageViewErrorEmpty.isVisible = state.showImageError
            binding.tvMessageError.isVisible = state.showImageError
            binding.progressBar.isVisible = state.isLoading
            binding.textViewNumberNotifications.text = state.numberNotifications
            binding.textViewNameList.text = state.textTitleList
            binding.editTextSearchView.isVisible = state.showInputLayout
            binding.buttonAddNewPartner.text = state.textButtonPartner
        }
    }


}