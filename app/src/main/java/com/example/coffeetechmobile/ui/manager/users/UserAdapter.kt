package com.example.coffeetechmobile.ui.manager.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.data.model.User
import com.example.coffeetechmobile.data.model.UserRole
import java.io.Serializable
import java.util.UUID

data class UserItem(
    val id: String = UUID.randomUUID().toString(),
    val userId: Long = 0L,
    val authRole: String = "BUYER",
    val name: String,
    val initials: String,
    val role: String,
    val document: String,
    val primaryIcon: Int,
    val primaryLabel: String,
    val primaryValue: String,
    val secondaryIcon: Int? = null,
    val secondaryLabel: String? = null,
    val secondaryValue: String? = null,
    val documentType: String = "CC (Cédula)",
    val documentNumber: String = "",
    val email: String = "",
    val phone: String = "",
    val branch: String = "",
    val contractType: String = ""
) : Serializable {

    companion object {

        /**
         * Convierte una cuenta de SQLite en el modelo que pinta el RecyclerView
         *
         * Los compradores muestran correo y teléfono; los colaboradores muestran
         * sede y tipo de contrato
         */
        fun fromUser(user: User): UserItem {
            val isBuyer = user.role == UserRole.BUYER

            val displayRole = user.jobTitle.ifBlank {
                when (user.role) {
                    UserRole.ADMIN -> "Administrador"
                    UserRole.SELLER -> "Vendedor"
                    UserRole.BUYER -> "Cliente"
                }
            }

            val initials = user.fullName
                .trim()
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercase() }

            val document = if (user.document.isBlank()) {
                "Sin documento"
            } else {
                "${user.documentType} ${user.document}".trim()
            }

            return UserItem(
                id = user.id.toString(),
                userId = user.id,
                authRole = user.role.name,
                name = user.fullName,
                initials = initials,
                role = displayRole,
                document = document,
                primaryIcon = if (isBuyer) {
                    R.drawable.ic_mail
                } else {
                    R.drawable.ic_store
                },
                primaryLabel = if (isBuyer) "Correo:" else "Sede:",
                primaryValue = if (isBuyer) {
                    user.email
                } else {
                    user.branch.ifBlank { "Sin sede" }
                },
                secondaryIcon = if (isBuyer) {
                    R.drawable.ic_phone
                } else {
                    R.drawable.ic_clipboard_clock
                },
                secondaryLabel = if (isBuyer) "Teléfono:" else "Contrato:",
                secondaryValue = if (isBuyer) {
                    user.phone.ifBlank { "Sin teléfono" }
                } else {
                    user.contractType.ifBlank { "Sin contrato" }
                },
                documentType = user.documentType,
                documentNumber = user.document,
                email = user.email,
                phone = user.phone,
                branch = user.branch,
                contractType = user.contractType
            )
        }
    }
}

class UserAdapter(
    private val users: MutableList<UserItem>,
    private val onEdit: (UserItem) -> Unit,
    private val onDelete: (UserItem) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)

        return UserViewHolder(view, onEdit, onDelete)
    }

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int
    ) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    fun addUser(user: UserItem) {
        users.add(0, user)
        notifyItemInserted(0)
    }

    fun removeUser(user: UserItem) {
        val position = users.indexOfFirst { it.id == user.id }

        if (position != -1) {
            users.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateUser(updatedUser: UserItem) {
        val position = users.indexOfFirst { it.id == updatedUser.id }

        if (position != -1) {
            users[position] = updatedUser
            notifyItemChanged(position)
        }
    }

    /**
     * Reemplaza la lista completa. Se usa después de cada consulta a SQLite.
     */
    fun submitList(newUsers: List<UserItem>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }

    class UserViewHolder(
        itemView: View,
        private val onEdit: (UserItem) -> Unit,
        private val onDelete: (UserItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

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

            val optionsButton = itemView.findViewById<View>(
                R.id.user_options_button
            )

            optionsButton.setOnClickListener { anchor ->

                val popupMenu = PopupMenu(itemView.context, anchor)

                popupMenu.menu.add(0, 1, 0, "Editar")
                popupMenu.menu.add(0, 2, 1, "Eliminar")

                popupMenu.setOnMenuItemClickListener { menuItem ->

                    when (menuItem.itemId) {
                        1 -> {
                            onEdit(user)
                            true
                        }

                        2 -> {
                            onDelete(user)
                            true
                        }

                        else -> false
                    }
                }

                popupMenu.show()
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
