package com.example.cotacaofacil.presentation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.cotacaofacil.R
import com.example.cotacaofacil.databinding.ItemPriceProviderBinding
import com.example.cotacaofacil.domain.model.PriceModel
import com.example.cotacaofacil.domain.model.StatusPrice
import com.example.cotacaofacil.presentation.ui.extension.dateEmpty
import com.example.cotacaofacil.presentation.ui.extension.formatDateHistoric
import com.example.cotacaofacil.presentation.ui.extension.toFormattedDateTime
import com.example.cotacaofacil.presentation.ui.extension.toTextStatus

class PriceProviderAdapter : RecyclerView.Adapter<PriceProviderAdapter.ItemPriceProviderViewHolder>() {
    private var listPriceModel: MutableList<PriceModel> = mutableListOf()
    var clickPrice: ((PriceModel) -> Unit)? = null
    var cnpjProvider: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemPriceProviderViewHolder {
        val binding = ItemPriceProviderBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemPriceProviderViewHolder(binding, cnpjProvider)
    }

    override fun onBindViewHolder(holder: ItemPriceProviderViewHolder, position: Int) {
        holder.bind(position, listPriceModel[position])
    }

    override fun getItemCount(): Int {
        return listPriceModel.size
    }

    fun updateList(productsModel: MutableList<PriceModel>) {
        listPriceModel.clear()
        listPriceModel.addAll(productsModel)
        notifyDataSetChanged()
    }


    inner class ItemPriceProviderViewHolder(val binding: ItemPriceProviderBinding, val cnpjProvider: String?) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, priceModel: PriceModel) {
            val context = binding.root.context

            binding.tvCodePrice.text = context.getString(R.string.code_adapter_price, priceModel.code)
            binding.tvCreationDateLabel.text =
                context.getString(R.string.date_init_price_adapter_price, priceModel.dateStartPrice.toFormattedDateTime())
            binding.tvStatus.text = priceModel.status.toTextStatus(context)
            cnpjProvider?.let { cnpj ->
                val productsModel = priceModel.productsPrice
                productsModel.forEach { productPriceModel ->
                    if (productPriceModel.usersPrice.find { it.cnpjProvider == cnpj } != null) {
                        binding.tvParticipating.isVisible = true
                        return@forEach
                    }
                }
            }
            when (priceModel.status) {
                StatusPrice.OPEN -> {
                    binding.constraintLayoutRoot.setBackgroundColor(ContextCompat.getColor(context, R.color.green_price))
                    binding.tvClosingDateLabel.text = priceModel.dateFinishPrice.dateEmpty(context, priceModel.closeAutomatic)
                        ?: context.getString(R.string.finish_price_not_auto)
                    binding.tvParticipating.text = context.getString(R.string.participating)
                    binding.tvParticipating.setBackgroundResource(R.drawable.shape_participate)
                }
                StatusPrice.CANCELED -> {
                    binding.tvParticipating.setBackgroundResource(R.drawable.shape_participate_closed)
                    binding.tvParticipating.text = context.getString(R.string.participate)
                    binding.constraintLayoutRoot.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_price))
                    binding.tvClosingDateLabel.visibility = View.GONE
                }
                StatusPrice.FINISHED -> {
                    binding.tvParticipating.setBackgroundResource(R.drawable.shape_participate_closed)
                    binding.tvParticipating.text = context.getString(R.string.participate)
                    binding.constraintLayoutRoot.setBackgroundColor(ContextCompat.getColor(context, R.color.red_price))
                    binding.tvClosingDateLabel.text =
                        context.getString(R.string.price_finished_date, priceModel.dateFinishPrice.formatDateHistoric())
                }
                StatusPrice.PENDENCY -> {
                    binding.tvParticipating.setBackgroundResource(R.drawable.shape_participate_pendency)
                    binding.tvParticipating.text = context.getString(R.string.pendency_status_text)
                    binding.constraintLayoutRoot.setBackgroundColor(ContextCompat.getColor(context, R.color.teal_200))
                    binding.tvClosingDateLabel.text =
                        context.getString(R.string.price_finished_date, priceModel.dateFinishPrice.formatDateHistoric())
                }
            }

            binding.tvCompanyName.text = priceModel.nameCompanyCreator
            binding.tvCnpj.text = "(${priceModel.cnpjBuyerCreator})"
            binding.tvClosingDateLabel.text = priceModel.dateFinishPrice?.dateEmpty(context, priceModel.closeAutomatic)
            binding.tvCreationDateLabel.text =
                context.getString(R.string.date_init_price_adapter_price, priceModel.dateStartPrice.toFormattedDateTime())
            binding.tvStatus.text = priceModel.status.toTextStatus(context)
            binding.tvClosureType.text = getTvClosureTypeText(context, priceModel.closeAutomatic)
            binding.tvProductQuantity.text = priceModel.productsPrice.size.toString()

            binding.cardViewRoot.setOnClickListener {
                clickPrice?.invoke(priceModel)
            }

            binding.ivExpand.setOnClickListener {
                binding.ivExpand.rotation = if (binding.ivExpand.rotation == 0F) 180F else 0F
                binding.tvClosureTypeLabel.isGone = !binding.tvClosureTypeLabel.isGone
                binding.tvClosureType.isGone = !binding.tvClosureType.isGone
                binding.tvObservations.isGone = !binding.tvObservations.isGone
                binding.tvObservationsLabel.isGone = !binding.tvObservationsLabel.isGone
                binding.tvProductQuantityLabel.isGone = !binding.tvProductQuantityLabel.isGone
                binding.tvProductQuantity.isGone = !binding.tvProductQuantity.isGone
                binding.tvCreationDateLabel.isGone = !binding.tvCreationDateLabel.isGone
                binding.tvCreationDate.isGone = !binding.tvCreationDate.isGone
            }
        }
    }

    private fun getTvClosureTypeText(context: Context, closeAutomatic: Boolean): String {
        return if (closeAutomatic) context.getString(R.string.finish_price_auto) else context.getString(R.string.finish_price_not_auto_description)
    }
}
