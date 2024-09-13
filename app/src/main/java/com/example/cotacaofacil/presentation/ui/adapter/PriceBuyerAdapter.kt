package com.example.cotacaofacil.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.cotacaofacil.R
import com.example.cotacaofacil.databinding.ItemPriceBuyerBinding
import com.example.cotacaofacil.domain.Extensions.Companion.getCnpjProviders
import com.example.cotacaofacil.domain.model.PriceModel
import com.example.cotacaofacil.domain.model.StatusPrice
import com.example.cotacaofacil.presentation.ui.extension.dateEmpty
import com.example.cotacaofacil.presentation.ui.extension.formatDateHistoric
import com.example.cotacaofacil.presentation.ui.extension.toFormattedDateTime
import com.example.cotacaofacil.presentation.ui.extension.toTextStatus

class PriceBuyerAdapter : RecyclerView.Adapter<PriceBuyerAdapter.ItemPriceBuyerViewHolder>() {
    var listPriceModel: MutableList<PriceModel> = mutableListOf()
    var clickPrice: ((PriceModel) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemPriceBuyerViewHolder {
        val binding = ItemPriceBuyerBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemPriceBuyerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemPriceBuyerViewHolder, position: Int) {
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


    inner class ItemPriceBuyerViewHolder(val binding: ItemPriceBuyerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, priceModel: PriceModel) {
            binding.tvCodePrice.text = binding.root.context.getString(R.string.code_adapter_price, priceModel.code)
            binding.tvCreationDateLabel.text = binding.root.context.getString(R.string.date_init_price_adapter_price, priceModel.dateStartPrice.toFormattedDateTime())
            binding.tvStatus.text = priceModel.status.toTextStatus(binding.root.context)

            when(priceModel.status){
                StatusPrice.OPEN -> {
                    binding.constraintLayoutRoot.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.green_price))
                    binding.tvClosingDateLabel.text = priceModel.dateFinishPrice.dateEmpty(binding.root.context, priceModel.closeAutomatic)
                }
                StatusPrice.CANCELED -> {
                    binding.constraintLayoutRoot.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.gray_price))
                    binding.tvClosingDateLabel.text = binding.root.context.getString(R.string.price_canceled_date, priceModel.dateFinishPrice?.formatDateHistoric())
                }
                StatusPrice.FINISHED -> {
                    binding.constraintLayoutRoot.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.red_price))
                    binding.tvClosingDateLabel.text = binding.root.context.getString(R.string.price_finished_date, priceModel.dateFinishPrice.formatDateHistoric())
                }
                StatusPrice.PENDENCY -> {
                    binding.constraintLayoutRoot.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.teal_200))
                    binding.tvClosingDateLabel.text = binding.root.context.getString(R.string.price_finished_message_finished_conflict)
                }
            }

            binding.tvClosureType.text = priceModel.getCnpjProviders().size.toString()
            binding.tvProductQuantity.text = priceModel.productsPrice.size.toString()

            binding.cardViewRoot.setOnClickListener {
                clickPrice?.invoke(priceModel)
            }

        }
    }
}