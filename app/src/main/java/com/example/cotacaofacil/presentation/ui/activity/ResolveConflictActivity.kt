package com.example.cotacaofacil.presentation.ui.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.example.cotacaofacil.R
import com.example.cotacaofacil.data.helper.UserHelper
import com.example.cotacaofacil.databinding.ActivityResolveConflictBinding
import com.example.cotacaofacil.domain.model.PriceModel
import com.example.cotacaofacil.presentation.ui.adapter.UserPriceAdapter
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.ResolveConflictViewModel
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractResolveConflict.ResolveConflictEventLiveData
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ResolveConflictActivity : AppCompatActivity() {

    private val binding by lazy { ActivityResolveConflictBinding.inflate(layoutInflater) }
    val adapter = UserPriceAdapter()
    private val userHelper by inject<UserHelper>()
    val user by lazy { userHelper.user }
    private val priceModel by lazy { intent.getParcelableExtra<PriceModel>(PRICE_MODEL_CONFLICT) }
    val viewModel: ResolveConflictViewModel by viewModel { parametersOf(priceModel) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel.verifyIfExistConflict()
        setupView()
        setupListeners()
        setupObservers()
    }

    private fun setupView() {
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.state.observe(this) { state ->
            binding.textViewNumberConflict.text = state.textNumberConflict
            binding.textViewCodeProduct.text = state.codeProduct
            binding.textViewNameProduct.text = state.productName
            binding.textViewTextDescription.text = state.productDescription
            binding.textViewQuantityProduct.text = state.productQuantity
            binding.textViewValueProduct.text = state.valueProduct
            binding.textViewValueTotal.text = state.valueTotal
            binding.buttonNextOrSave.text = state.textButton
            binding.buttonNextOrSave.backgroundTintList = AppCompatResources.getColorStateList(this, state.colorBackgroundButton)
            binding.buttonNextOrSave.setTextColor(ContextCompat.getColor(this, state.colorTextButton))
            binding.buttonNextOrSave.isClickable = state.buttonClickable
            adapter.updateList(state.usersPriceConflict)
        }

        viewModel.event.observe(this) { event ->
            when (event) {
                is ResolveConflictEventLiveData.FinishActivityError -> finishActivityError(message = event.message)
                is ResolveConflictEventLiveData.FinishScreenAndPrice -> {
                    val intent = Intent()
                    intent.putExtra(PRICE_MODEL_CONFLICT, event.priceModel)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.buttonNextOrSave.setOnClickListener {
            viewModel.tapOnNextOrSave(adapter.currentList)
        }
        adapter.selectItem = {
            viewModel.verifyStatusButton(it)
        }
    }

    private fun finishActivityError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }



    override fun onResume() {
        if (user == null) {
            //criar uma viewmodel e se for nulo buscar o user da mesma forma que o login buscou para passar para cá
            finish()
        }
        super.onResume()
    }

    override fun onBackPressed() {
        showExitConfirmationDialog()
    }

    private fun showExitConfirmationDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder.setTitle(R.string.attention)
        alertDialogBuilder.setMessage(getString(R.string.dialog_message_close_resolve_conflict))

        alertDialogBuilder.setPositiveButton(R.string.yes) { dialog: DialogInterface, _: Int ->
            finish()
            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton(R.string.not) { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    companion object {
        const val PRICE_MODEL_CONFLICT = "PRICE_MODEL_CONFLICT"
        const val RESOLVE_CONFLICT = 435
    }

}