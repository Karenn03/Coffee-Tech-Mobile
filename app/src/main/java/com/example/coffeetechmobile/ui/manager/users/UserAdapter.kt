package com.example.coffeetechmobile.ui.manager.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeetechmobile.R

data class UserItem(
    val name: String,
    val initials: String,
    val role: String,
    val document: String,
    val primaryIcon: Int,
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryIcon: Int? = null,
    val secondaryLabel: String? = null,
    val secondaryValue: String? = null
)

class UserAdapter(
    private val users: List<UserItem>
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)

        return UserViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int
    ) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    class UserViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(user: UserItem) {

            itemView.findViewById<TextView>(
                R.id.user_initials
            ).text = user.initials

            itemView.findViewById<TextView>(
                R.id.user_name
            ).text = user.name

            itemView.findViewById<TextView>(
                R.id.user_role
            ).text = user.role

            itemView.findViewById<TextView>(
                R.id.user_document
            ).text = user.document

            bindDetail(
                R.id.user_detail_primary,
                R.id.user_detail_primary_icon,
                R.id.user_detail_primary_label,
                R.id.user_detail_primary_value,
                user.primaryIcon,
                user.primaryLabel,
                user.primaryValue
            )

            val secondary = itemView.findViewById<View>(
                R.id.user_detail_secondary
            )

            if (
                user.secondaryIcon != null &&
                user.secondaryLabel != null &&
                user.secondaryValue != null
            ) {
                secondary.visibility = View.VISIBLE

                bindDetail(
                    R.id.user_detail_secondary,
                    R.id.user_detail_secondary_icon,
                    R.id.user_detail_secondary_label,
                    R.id.user_detail_secondary_value,
                    user.secondaryIcon,
                    user.secondaryLabel,
                    user.secondaryValue
                )
            } else {
                secondary.visibility = View.GONE
            }
        }

        private fun bindDetail(
            containerId: Int,
            iconId: Int,
            labelId: Int,
            valueId: Int,
            iconRes: Int,
            label: String,
            value: String
        ) {
            val container = itemView.findViewById<View>(containerId)

            container.findViewById<ImageView>(iconId)
                .setImageResource(iconRes)

            container.findViewById<TextView>(labelId).text = label

            container.findViewById<TextView>(valueId).text = value
        }
    }
}
