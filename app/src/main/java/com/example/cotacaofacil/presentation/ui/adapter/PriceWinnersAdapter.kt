package com.example.cotacaofacil.presentation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.cotacaofacil.R
import com.example.cotacaofacil.databinding.ItemLineDividerBinding
import com.example.cotacaofacil.databinding.ItemProductPriceEmptyBinding
import com.example.cotacaofacil.databinding.ItemTableProductBinding
import com.example.cotacaofacil.databinding.ItemUserPriceBinding
import com.example.cotacaofacil.domain.model.*

class PriceWinnersAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var priceWinnersList: MutableList<PriceWinner> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            PriceWinnerType.PRODUCT_PRICE.ordinal -> {
                val binding = ItemTableProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ProductViewHolder(binding)
            }
            PriceWinnerType.USER_PRICE.ordinal -> {
                val binding = ItemUserPriceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                UserPriceViewHolder(binding)
            }
            PriceWinnerType.LINE_DIVIDER.ordinal -> {
                val binding = ItemLineDividerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                LineViewHolder(binding)
            }
            PriceWinnerType.TEXT_DESCRIPTION.ordinal -> {
                val binding = ItemProductPriceEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                EmptyPriceViewHolder(binding)
            }

            else -> {
                val binding = ItemLineDividerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                LineViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val priceWinner = priceWinnersList[position]
        when (holder) {
            is ProductViewHolder -> holder.bind(priceWinner.productPrice)
            is UserPriceViewHolder -> holder.bind(priceWinner.userPrice)
            is EmptyPriceViewHolder -> holder.bind(priceWinner.textDescription)
            is LineViewHolder -> holder.bind(priceWinner.textEndLine)
        }
    }

    override fun getItemCount(): Int {
        return priceWinnersList.size
    }

    override fun getItemViewType(position: Int): Int {
        return priceWinnersList[position].type.ordinal
    }

    fun updateList(priceWinnersList: MutableList<PriceWinner>?) {
        priceWinnersList?.let {
            this.priceWinnersList.clear()
            this.priceWinnersList = priceWinnersList
            notifyDataSetChanged()
        }
    }

    inner class ProductViewHolder(private val binding: ItemTableProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductPriceEditPriceModel?) {
            val context = binding.root.context
            product?.let {
                setNameProduct(itemTableProductModel = it)
                setTextQuantity(itemTableProductModel = it)
                binding.editTextQuantity.isEnabled = false
                binding.editTextQuantity.setText("R$ " + (product.price * product.quantityProducts).toString())
                colorClicked(context, binding, R.color.colorPrimary, R.color.white)
            }
        }

        private fun setNameProduct(itemTableProductModel: ProductPriceEditPriceModel) {
            if (itemTableProductModel.productModel.quantity.isEmpty() || itemTableProductModel.productModel.typeMeasurement == "Outros") {
                binding.tvNameProduct.text =
                    "${itemTableProductModel.productModel.name} ${itemTableProductModel.productModel.brand} - Qtd. ${itemTableProductModel.quantityProducts}"
            } else {
                binding.tvNameProduct.text =
                    "${itemTableProductModel.productModel.name} ${itemTableProductModel.productModel.brand} - ${itemTableProductModel.productModel.quantity} ${itemTableProductModel.productModel.typeMeasurement}  - Qtd. ${itemTableProductModel.quantityProducts}"
            }
            binding.editTextQuantity.setText(itemTableProductModel.quantityProducts.toString())
        }

        private fun setTextQuantity(itemTableProductModel: ProductPriceEditPriceModel) {
            if (binding.editTextQuantity.text.toString().isNotEmpty()) {
                itemTableProductModel.quantityProducts = binding.editTextQuantity.text.toString().toInt()
            } else {
                itemTableProductModel.quantityProducts = 1
            }
            binding.textViewIndex.text = itemTableProductModel.productModel.code
        }


        private fun colorClicked(context: Context, binding: ItemTableProductBinding, color: Int, colorTextCode: Int) {
            val colorCompat = ContextCompat.getColor(context, color)
            binding.viewEndLine.setBackgroundColor(colorCompat)
            binding.viewBottomLine.setBackgroundColor(colorCompat)
            binding.viewTopLine.setBackgroundColor(colorCompat)
            binding.viewStartLine.setBackgroundColor(colorCompat)
            binding.textViewIndex.setBackgroundColor(colorCompat)
            binding.editTextQuantity.setBackgroundColor(colorCompat)
            binding.textViewIndex.setTextColor(ContextCompat.getColor(context, colorTextCode))
        }
    }

    inner class UserPriceViewHolder(private val binding: ItemUserPriceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(userPrice: UserPrice?) {
            val context = binding.root.context
            userPrice?.let {
                binding.textViewNamePartner.text = userPrice.nameUser
                binding.textViewCnpj.text = userPrice.cnpjProvider
                binding.imageViewCrower.isVisible
                binding.root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                binding.textViewNamePartner.setTextColor(ContextCompat.getColor(context, R.color.white))
                binding.textViewCnpj.setTextColor(ContextCompat.getColor(context, R.color.white))
                binding.imageViewCrower.visibility = View.VISIBLE
                Glide.with(context)
                    .load(userPrice.imageUser)
                    .apply(RequestOptions().transform(CircleCrop()))
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(binding.imageViewUser)
            }
        }
    }

    inner class LineViewHolder(private val binding: ItemLineDividerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(textEnd: String) {
            binding.textViewEndMessage.text = textEnd
        }
    }

    inner class EmptyPriceViewHolder(private val binding: ItemProductPriceEmptyBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(description: TextDescription?) {
            description?.let {
                binding.textViewDescription.text = description.text
                binding.imageViewIcon.setImageDrawable(description.icon)
            }
        }
    }
}
