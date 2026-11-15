package com.example.coffeetechmobile.ui.buyer.address

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.data.model.DeliveryAddress

// Lista de direcciones guardadas con selección y eliminación
class DeliveryAddressAdapter(
    private val onSelect: (DeliveryAddress) -> Unit,
    private val onDelete: (DeliveryAddress) -> Unit
) : ListAdapter<DeliveryAddress, DeliveryAddressAdapter.AddressViewHolder>(
    DiffCallback()
) {

    var selectedAddressId: Long? = null
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount)
        }

    inner class AddressViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val label: TextView =
            itemView.findViewById(R.id.address_label)

        private val address: TextView =
            itemView.findViewById(R.id.address_text)

        private val radio: RadioButton =
            itemView.findViewById(R.id.address_radio)

        private val deleteButton: ImageButton =
            itemView.findViewById(R.id.address_delete_button)

        fun bind(item: DeliveryAddress) {
            label.text = item.label
            address.text = item.address
            radio.isChecked = item.id == selectedAddressId

            itemView.setOnClickListener {
                onSelect(item)
            }

            deleteButton.setOnClickListener {
                onDelete(item)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AddressViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_delivery_address,
            parent,
            false
        )

        return AddressViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: AddressViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    private class DiffCallback :
        DiffUtil.ItemCallback<DeliveryAddress>() {

        override fun areItemsTheSame(
            oldItem: DeliveryAddress,
            newItem: DeliveryAddress
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: DeliveryAddress,
            newItem: DeliveryAddress
        ): Boolean {
            return oldItem == newItem
        }
    }
}
