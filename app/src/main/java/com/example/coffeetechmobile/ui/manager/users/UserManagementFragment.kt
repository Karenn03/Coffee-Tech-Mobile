package com.example.coffeetechmobile.ui.manager.users

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.data.model.User
import com.example.coffeetechmobile.data.model.UserRole
import com.example.coffeetechmobile.data.session.SessionManager
import com.example.coffeetechmobile.ui.manager.navigation.ManagerTab
import com.example.coffeetechmobile.ui.manager.navigation.setupManagerBottomNavigation
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Gestión de usuarios del administrador
 *
 * Lista las cuentas reales de SQLite y permite crear, editar y eliminar. Los
 * indicadores superiores se calculan con esos mismos datos
 */
class UserManagementFragment :
    Fragment(R.layout.fragment_user_management) {

    private val viewModel: UserManagementViewModel by viewModels()

    private lateinit var userAdapter: UserAdapter

    private var allUsers: List<User> = emptyList()
    private var selectedFilter = UserFilter.ALL
    private var searchQuery = ""
    private var sortAscending = true
    private var currentPage = 1

    private companion object {
        const val PAGE_SIZE = 5
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupManagerBottomNavigation(
            root = view.findViewById(
                R.id.manager_bottom_navigation_container
            ),
            navController = findNavController(),
            selectedTab = ManagerTab.USERS
        )

        // Se muestran ceros mientras llega la primera consulta a SQLite.
        renderStats(emptyList())

        val usersRecyclerView = view.findViewById<RecyclerView>(
            R.id.users_recycler_view
        )

        usersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        usersRecyclerView.isNestedScrollingEnabled = false

        userAdapter = UserAdapter(
            users = mutableListOf(),

            // Acción: editar usuario
            onEdit = { user ->
                findNavController().navigate(
                    R.id.action_userManagementFragment_to_createUserFragment,
                    bundleOf("user_to_edit" to user)
                )
            },

            // Acción: eliminar usuario
            onDelete = { user ->
                showDeleteConfirmation(user)
            }
        )

        usersRecyclerView.adapter = userAdapter

        setupSearch(view)
        setupFilters(view)
        setupSort(view)
        setupPagination(view)

        // Navegar al formulario de creación
        view.findViewById<View>(R.id.create_user_button)
            .setOnClickListener {
                findNavController().navigate(
                    R.id.action_userManagementFragment_to_createUserFragment
                )
            }

        applyFilters()
        observeUsers()
    }

    override fun onResume() {
        super.onResume()

        // Al volver del formulario se recarga la lista desde SQLite
        viewModel.loadUsers()
    }

    private fun observeUsers() {
        viewModel.users.observe(viewLifecycleOwner) { users ->
            allUsers = users
            renderStats(users)
            updateFilterCounts()
            applyFilters()
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(
                    requireContext(),
                    it,
                    Toast.LENGTH_SHORT
                ).show()

                viewModel.consumeMessage()
            }
        }
    }

    // Conecta el buscador con el filtrado local de la lista
    private fun setupSearch(root: View) {
        root.findViewById<EditText>(R.id.user_search_input)
            .doAfterTextChanged { editable ->
                searchQuery = editable?.toString().orEmpty().trim()
                currentPage = 1
                applyFilters()
            }
    }

    // Conecta los filtros: Todos, Empleados y Clientes
    private fun setupFilters(root: View) {
        root.findViewById<View>(R.id.filter_all)
            .setOnClickListener {
                selectedFilter = UserFilter.ALL
                currentPage = 1
                applyFilters()
            }

        root.findViewById<View>(R.id.filter_employees)
            .setOnClickListener {
                selectedFilter = UserFilter.EMPLOYEES
                currentPage = 1
                applyFilters()
            }

        root.findViewById<View>(R.id.filter_clients)
            .setOnClickListener {
                selectedFilter = UserFilter.CLIENTS
                currentPage = 1
                applyFilters()
            }
    }

    // Conecta los botones de página anterior y siguiente
    private fun setupPagination(root: View) {
        root.findViewById<View>(R.id.previous_page_button)
            .setOnClickListener {
                if (currentPage > 1) {
                    currentPage--
                    applyFilters()
                }
            }

        root.findViewById<View>(R.id.next_page_button)
            .setOnClickListener {
                currentPage++
                applyFilters()
            }
    }

    // Alterna entre orden alfabético y orden por creación
    private fun setupSort(root: View) {
        root.findViewById<View>(R.id.sort_users_button)
            .setOnClickListener {
                sortAscending = !sortAscending
                currentPage = 1
                applyFilters()
            }
    }

    // Aplica búsqueda, filtro y orden sobre la lista que vino de SQLite
    private fun applyFilters() {
        val query = searchQuery.lowercase()

        val filtered = allUsers.filter { user ->
            val matchesFilter = when (selectedFilter) {
                UserFilter.ALL -> true
                UserFilter.EMPLOYEES -> user.role != UserRole.BUYER
                UserFilter.CLIENTS -> user.role == UserRole.BUYER
            }

            val matchesSearch = query.isEmpty() ||
                user.fullName.lowercase().contains(query) ||
                user.email.lowercase().contains(query) ||
                user.document.lowercase().contains(query) ||
                user.jobTitle.lowercase().contains(query)

            matchesFilter && matchesSearch
        }

        val sorted = if (sortAscending) {
            filtered.sortedBy { it.fullName.lowercase() }
        } else {
            filtered.sortedByDescending { it.id }
        }

        val totalPages = if (sorted.isEmpty()) {
            1
        } else {
            (sorted.size + PAGE_SIZE - 1) / PAGE_SIZE
        }

        currentPage = currentPage.coerceIn(1, totalPages)

        val start = (currentPage - 1) * PAGE_SIZE
        val end = minOf(start + PAGE_SIZE, sorted.size)

        val pageUsers = if (sorted.isEmpty()) {
            emptyList()
        } else {
            sorted.subList(start, end)
        }

        userAdapter.submitList(
            pageUsers.map { UserItem.fromUser(it) }
        )

        updateFilterSelection()
        updateSortButton()
        updatePagination(
            currentPage = currentPage,
            totalPages = totalPages,
            totalCount = sorted.size
        )
    }

    // Pinta el filtro activo con el estilo ya definido en el layout
    private fun updateFilterSelection() {
        bindFilterChip(R.id.filter_all, selectedFilter == UserFilter.ALL)
        bindFilterChip(
            R.id.filter_employees,
            selectedFilter == UserFilter.EMPLOYEES
        )
        bindFilterChip(
            R.id.filter_clients,
            selectedFilter == UserFilter.CLIENTS
        )
    }

    private fun bindFilterChip(containerId: Int, selected: Boolean) {
        val container = requireView().findViewById<View>(containerId)

        container.setBackgroundResource(
            if (selected) {
                R.drawable.bg_filter_selected
            } else {
                R.drawable.bg_filter_unselected
            }
        )

        val icon = (container as? ViewGroup)?.getChildAt(0) as? ImageView
        val label = (container as? ViewGroup)?.getChildAt(1) as? TextView

        val iconColor = ContextCompat.getColor(
            requireContext(),
            if (selected) R.color.text_light else R.color.coffee_secondary
        )

        val textColor = ContextCompat.getColor(
            requireContext(),
            if (selected) R.color.text_light else R.color.coffee_primary
        )

        icon?.imageTintList = ColorStateList.valueOf(iconColor)
        label?.setTextColor(textColor)
    }

    // Actualiza los contadores de los chips con los datos reales
    private fun updateFilterCounts() {
        val total = allUsers.size

        val employees = allUsers.count {
            it.role != UserRole.BUYER
        }

        val clients = allUsers.count {
            it.role == UserRole.BUYER
        }

        setFilterLabel(R.id.filter_all, "Todos ($total)")
        setFilterLabel(R.id.filter_employees, "Empleados ($employees)")
        setFilterLabel(R.id.filter_clients, "Clientes ($clients)")
    }

    private fun setFilterLabel(containerId: Int, text: String) {
        val container = requireView().findViewById<View>(containerId)
        ((container as? ViewGroup)?.getChildAt(1) as? TextView)?.text = text
    }

    private fun updateSortButton() {
        val container = requireView()
            .findViewById<View>(R.id.sort_users_button)

        val label = (container as? ViewGroup)?.getChildAt(1) as? TextView
        label?.text = if (sortAscending) {
            "Ordenar A-Z"
        } else {
            "Ordenar recientes"
        }
    }

    // Actualiza el contador total y los controles de paginación
    private fun updatePagination(
        currentPage: Int,
        totalPages: Int,
        totalCount: Int
    ) {
        val records = requireView().findViewById<TextView>(
            R.id.user_records_count
        )

        records.text = if (totalCount == 1) {
            "1 Registro"
        } else {
            "$totalCount Registros"
        }

        requireView().findViewById<TextView>(
            R.id.current_page_text
        ).text = "Página $currentPage de $totalPages"

        requireView().findViewById<View>(
            R.id.previous_page_button
        ).apply {
            isEnabled = currentPage > 1
            alpha = if (currentPage > 1) 1f else 0.4f
        }

        requireView().findViewById<View>(
            R.id.next_page_button
        ).apply {
            isEnabled = currentPage < totalPages
            alpha = if (currentPage < totalPages) 1f else 0.4f
        }
    }

    // Actualiza las stats con los datos reales de la base
    private fun renderStats(users: List<User>) {
        val total = users.size

        val clients = users.count {
            it.role == UserRole.BUYER
        }

        val employees = users.count {
            it.role != UserRole.BUYER
        }

        val currentMonth = LocalDate.now()

        val newThisMonth = users.count { user ->
            val date = Instant.ofEpochMilli(user.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            date.year == currentMonth.year &&
                date.monthValue == currentMonth.monthValue
        }

        bindStat(
            root = requireView(),
            cardId = R.id.stat_total,
            iconRes = R.drawable.ic_users,
            badge = "Total",
            value = total.toString(),
            label = "Usuarios globales",
            backgroundRes = R.drawable.bg_stat_total,
            badgeBackgroundRes = R.drawable.bg_stat_badge,
            foregroundColor = "#916A45",
            badgeTextColor = "#8D6849"
        )

        bindStat(
            root = requireView(),
            cardId = R.id.stat_clients,
            iconRes = R.drawable.ic_shopping_bag,
            badge = "Activos",
            value = clients.toString(),
            label = "Clientes activos",
            backgroundRes = R.drawable.bg_stat_clients,
            badgeBackgroundRes = R.drawable.bg_stat_badge,
            foregroundColor = "#8A5E35",
            badgeTextColor = "#8D6849"
        )

        bindStat(
            root = requireView(),
            cardId = R.id.stat_sales,
            iconRes = R.drawable.ic_user_plus,
            badge = "Este mes",
            value = newThisMonth.toString(),
            label = "Nuevos usuarios",
            backgroundRes = R.drawable.bg_stat_sales,
            badgeBackgroundRes = R.drawable.bg_stat_badge_dark,
            foregroundColor = "#FCE6DC",
            badgeTextColor = "#FCE6DC"
        )

        bindStat(
            root = requireView(),
            cardId = R.id.stat_employees,
            iconRes = R.drawable.ic_coffee_maker,
            badge = "Barra activa",
            value = employees.toString(),
            label = "Empleados",
            backgroundRes = R.drawable.bg_stat_employees,
            badgeBackgroundRes = R.drawable.bg_stat_badge,
            foregroundColor = "#B77737",
            badgeTextColor = "#A9682E"
        )
    }

    private fun bindStat(
        root: View,
        cardId: Int,
        iconRes: Int,
        badge: String,
        value: String,
        label: String,
        backgroundRes: Int,
        badgeBackgroundRes: Int,
        foregroundColor: String,
        badgeTextColor: String
    ) {
        val card = root.findViewById<View>(cardId)

        card.setBackgroundResource(backgroundRes)

        val icon = card.findViewById<ImageView>(R.id.stat_icon)
        icon.setImageResource(iconRes)
        icon.imageTintList =
            ColorStateList.valueOf(Color.parseColor(foregroundColor))

        val badgeView = card.findViewById<TextView>(R.id.stat_badge)
        badgeView.text = badge
        badgeView.setBackgroundResource(badgeBackgroundRes)
        badgeView.setTextColor(Color.parseColor(badgeTextColor))

        val valueView = card.findViewById<TextView>(R.id.stat_value)
        valueView.text = value
        valueView.setTextColor(Color.parseColor(foregroundColor))

        val labelView = card.findViewById<TextView>(R.id.stat_label)
        labelView.text = label
        labelView.setTextColor(Color.parseColor(foregroundColor))
    }

    /**
     * Pide confirmación antes de eliminar y evita que el administrador borre
     * su propia cuenta mientras la está usando.
     */
    private fun showDeleteConfirmation(user: UserItem) {
        val currentUserId = SessionManager(requireContext())
            .current()
            ?.id

        if (user.userId == currentUserId) {
            Toast.makeText(
                requireContext(),
                "No puedes eliminar tu propia cuenta.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar usuario")
            .setMessage(
                "¿Estás seguro de que deseas eliminar a ${user.name}?"
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteUser(user.userId)
            }
            .show()
    }

    private enum class UserFilter {
        ALL,
        EMPLOYEES,
        CLIENTS
    }
}
