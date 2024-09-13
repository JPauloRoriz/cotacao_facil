package com.example.cotacaofacil.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.cotacaofacil.R
import com.example.cotacaofacil.databinding.ItemUserPriceBinding
import com.example.cotacaofacil.domain.model.UserPriceConflict

class UserPriceAdapter : ListAdapter<UserPriceConflict, UserPriceAdapter.UserPriceViewHolder>(UserPriceDiffCallback) {

    var selectItem: ((List<UserPriceConflict>) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPriceViewHolder {
        val binding = ItemUserPriceBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return UserPriceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserPriceViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    fun updateList(UserPriceConflictList: MutableList<UserPriceConflict>) {
        submitList(UserPriceConflictList)
        notifyDataSetChanged()
    }

    inner class UserPriceViewHolder(val binding: ItemUserPriceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(userPrice: UserPriceConflict) {
            val context = binding.root.context
            binding.textViewNamePartner.text = userPrice.nameCorporation
            binding.textViewCnpj.text = userPrice.cnpjCorporation
            Glide.with(context)
                .load(userPrice.image)
                .apply(RequestOptions().transform(CircleCrop()))
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.imageViewUser)
            if (userPrice.isSelect) {
                binding.imageViewCrower.isVisible
                binding.root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                binding.textViewNamePartner.setTextColor(ContextCompat.getColor(context, R.color.white))
                binding.textViewCnpj.setTextColor(ContextCompat.getColor(context, R.color.white))
                binding.imageViewCrower.visibility = View.VISIBLE
            } else {
                binding.imageViewCrower.isInvisible
                binding.root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white_bottom_navigation))
                binding.textViewNamePartner.setTextColor(ContextCompat.getColor(context, R.color.black))
                binding.textViewCnpj.setTextColor(ContextCompat.getColor(context, R.color.black))
                binding.imageViewCrower.visibility = View.GONE
            }
            binding.root.setOnClickListener {
                currentList.forEach { it.isSelect = false }
                userPrice.isSelect = !userPrice.isSelect
                selectItem?.invoke(currentList)
                notifyDataSetChanged()
            }

        }
    }

    private companion object UserPriceDiffCallback : DiffUtil.ItemCallback<UserPriceConflict>() {
        override fun areItemsTheSame(oldItem: UserPriceConflict, newItem: UserPriceConflict): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: UserPriceConflict, newItem: UserPriceConflict): Boolean {
            return oldItem.userPrice.cnpjProvider == newItem.userPrice.cnpjProvider
        }
    }
}