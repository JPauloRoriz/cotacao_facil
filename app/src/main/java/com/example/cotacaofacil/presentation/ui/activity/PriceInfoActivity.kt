package com.example.cotacaofacil.presentation.ui.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.cotacaofacil.R
import com.example.cotacaofacil.data.helper.UserHelper
import com.example.cotacaofacil.databinding.ActivityPriceInfoBinding
import com.example.cotacaofacil.domain.model.PriceModel
import com.example.cotacaofacil.domain.model.StatusPrice
import com.example.cotacaofacil.presentation.ui.activity.ResolveConflictActivity.Companion.PRICE_MODEL_CONFLICT
import com.example.cotacaofacil.presentation.ui.activity.ResolveConflictActivity.Companion.RESOLVE_CONFLICT
import com.example.cotacaofacil.presentation.ui.adapter.ItemTableProductAdapter
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.PriceInfoViewModel
import com.example.cotacaofacil.presentation.viewmodel.buyer.price.contractPriceInfo.PriceEvent
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PriceInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPriceInfoBinding
    private val userHelper by inject<UserHelper>()
    val user by lazy { userHelper.user }
    private val codePrice by lazy { intent.getStringExtra(CODE_PRICE_SHOW) ?: "" }
    private val cnpjBuyerCreator by lazy { intent.getStringExtra(CNPJ_USER) ?: "" }

    private val viewModel: PriceInfoViewModel by viewModel { parametersOf(codePrice, cnpjBuyerCreator) }
    private val allProductsPriceAdapter by lazy { ItemTableProductAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPriceInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupListeners()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.stateLiveData.observe(this) { state ->
            with(binding) {
                btnCancelPrice.isVisible = state.showBtnCancelPrice
                btnFinishPrice.isVisible = state.showBtnFinishPrice
                progressBar.isVisible = state.isLoading
                allProductsPriceAdapter.updateList(state.productsPrice)
                tvProductQuantity.text = state.quantityProducts
                tvCreationDateLabel.text = state.dateInit
                tvClosingDateLabel.text = state.dateFinish
                tvQuantityProvider.text = state.quantityProviders
            }
        }

        viewModel.eventLiveData.observe(this) { event ->
            when (event) {
                is PriceEvent.FinishActivity -> {
                    val intent = Intent()
                    intent.putExtra(MESSAGE_ERROR, event.message)
                    setResult(RESULT_OK, intent)
                    finish()
                }
                is PriceEvent.ShowScreenConflicts -> {
                    val alert = AlertDialog.Builder(this, R.style.MyDialogTheme)
                    alert.setMessage(
                        getString(
                            R.string.find_conflict_price
                        )
                    ).setTitle(getString(R.string.attention)).setNegativeButton(getString(R.string.resolve_later)) { dialog, int -> }
                        .setPositiveButton(R.string.resolve_now) { dialog, int ->

                        }.show()
                }
                PriceEvent.ErrorConflictNotResolved -> errorResolveConflict()
                is PriceEvent.ShowScreenConflict -> showConflicts(priceModel = event.priceModel)
                is PriceEvent.ShowDialogWarning -> showDialogCancelOrFinish(event.messageDialog, event.statusPrice)
            }
        }
    }

    private fun showDialogCancelOrFinish(messageDialog: String, status: StatusPrice) {
        val alert = AlertDialog.Builder(this, R.style.MyDialogTheme)
        alert.setMessage(messageDialog).setTitle(getString(R.string.attention)).setNegativeButton(getString(R.string.not)) { dialog, int -> }
            .setPositiveButton(R.string.yes) { dialog, int ->
                viewModel.cancelOrFinish(statusPrice = status)
            }.show()

    }

    private fun showConflicts(priceModel: PriceModel) {
        val intent = Intent(this, ResolveConflictActivity::class.java)
        intent.putExtra(PRICE_MODEL_CONFLICT, priceModel)
        startActivityForResult(intent, RESOLVE_CONFLICT)
    }

    private fun errorResolveConflict() {
        Toast.makeText(this, getString(R.string.error_save_winner), Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESOLVE_CONFLICT && resultCode == RESULT_OK) {
            data?.let {
                val priceModel = it.getParcelableExtra<PriceModel>(PRICE_MODEL_CONFLICT)
                viewModel.cancelOrFinishPrice(statusPrice = StatusPrice.FINISHED, priceModelEdit = priceModel)
            } ?: errorResolveConflict()
        }
    }

    private fun setupListeners() {
        binding.arrow.setOnClickListener { finish() }
        binding.btnCancelPrice.setOnClickListener { viewModel.tapOnCancelOrFinishPrice(StatusPrice.CANCELED) }
        binding.btnFinishPrice.setOnClickListener { viewModel.tapOnCancelOrFinishPrice(StatusPrice.FINISHED) }
    }

    private fun setupView() {
        binding.recyclerViewAllProducts.adapter = allProductsPriceAdapter
        allProductsPriceAdapter.isEditable = false
        binding.textViewToolbar.text = getString(R.string.price_number, codePrice)
    }

    override fun onResume() {
        if (user == null) {
            //criar uma viewmodel e se for nulo buscar o user da mesma forma que o login buscou para passar para cá
            finish()
        }
        super.onResume()
    }

    companion object {
        const val CODE_PRICE_SHOW = "CODE_PRICE_SHOW"
        const val CNPJ_USER = "CNPJ_USER"
        const val UPDATE_PRICES = 557
        const val MESSAGE_ERROR = "MESSAGE_ERROR"
    }
}