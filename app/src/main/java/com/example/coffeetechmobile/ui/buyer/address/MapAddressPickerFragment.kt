package com.example.coffeetechmobile.ui.buyer.address

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.data.location.DeliveryLocation
import com.example.coffeetechmobile.data.location.DeliveryLocationProvider
import com.example.coffeetechmobile.ui.buyer.location.hasLocationPermission
import com.example.coffeetechmobile.ui.buyer.location.locationPermissions
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.api.IGeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Selector de dirección sobre OpenStreetMap
 *
 * El pin permanece en el centro de la pantalla y el usuario mueve el mapa.
 * La dirección se resuelve con el Geocoder del sistema y se puede buscar
 * por texto o centrar en la ubicación actual.
 */
class MapAddressPickerFragment :
    Fragment(R.layout.fragment_map_address_picker) {

    private val viewModel: AddressBookViewModel by activityViewModels()

    private lateinit var map: MapView
    private lateinit var addressText: TextView
    private lateinit var progress: ProgressBar
    private lateinit var confirmButton: View

    private var selectedLocation: DeliveryLocation? = null
    private var resolvedCenter: GeoPoint? = null
    private var lookupJob: Job? = null
    private var initialCenterApplied = false

    private val locationProvider by lazy {
        DeliveryLocationProvider(requireContext())
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted =
            permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            useCurrentLocation()
        } else {
            Toast.makeText(
                requireContext(),
                R.string.delivery_location_permission_denied,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupMap(view)
        setupSearch(view)
        setupActions(view)
        observeAddressBook()

        viewModel.loadAddresses()

        viewLifecycleOwner.lifecycleScope.launch {
            delay(INITIAL_LOOKUP_DELAY_MS)

            if (!initialCenterApplied) {
                initialCenterApplied = true
                lookupAddressAtCenter()
            }
        }
    }

    private fun setupMap(root: View) {
        Configuration.getInstance().userAgentValue =
            "CoffeeTechMobile/1.0 (contact: soporte@coffeetech.com)"

        map = root.findViewById(R.id.address_map)

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(DEFAULT_ZOOM)
        map.controller.setCenter(
            GeoPoint(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
        )

        map.addMapListener(
            object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    scheduleAddressLookup()
                    return false
                }

                override fun onZoom(event: ZoomEvent?): Boolean {
                    scheduleAddressLookup()
                    return false
                }
            }
        )
    }

    private fun setupSearch(root: View) {
        val input = root.findViewById<EditText>(
            R.id.map_search_input
        )

        root.findViewById<View>(R.id.map_back_button)
            .setOnClickListener {
                findNavController().navigateUp()
            }

        root.findViewById<View>(R.id.map_search_button)
            .setOnClickListener {
                searchAddress(input)
            }

        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchAddress(input)
                true
            } else {
                false
            }
        }
    }

    private fun setupActions(root: View) {
        addressText = root.findViewById(R.id.map_selected_address)
        progress = root.findViewById(R.id.map_progress)
        confirmButton = root.findViewById(R.id.map_confirm_button)

        confirmButton.isEnabled = false
        confirmButton.alpha = 0.5f

        root.findViewById<View>(R.id.map_use_current_button)
            .setOnClickListener {
                requestCurrentLocation()
            }

        confirmButton.setOnClickListener {
            val location = selectedLocation
            val center = map.mapCenter
            val resolved = resolvedCenter

            if (
                location == null ||
                resolved == null ||
                distanceMeters(center, resolved) > MAX_CONFIRM_DISTANCE_METERS
            ) {
                Toast.makeText(
                    requireContext(),
                    R.string.address_map_resolving,
                    Toast.LENGTH_SHORT
                ).show()
                lookupAddressAtCenter()
                return@setOnClickListener
            }

            showAddressLabelDialog(location.address) { label ->
                viewModel.saveAddress(label, location)
            }
        }
    }

    private fun observeAddressBook() {
        viewModel.addresses.observe(viewLifecycleOwner) { addresses ->
            if (initialCenterApplied) {
                return@observe
            }

            val selected = addresses.firstOrNull {
                it.id == viewModel.selectedAddressId.value
            } ?: addresses.firstOrNull()

            if (selected != null) {
                initialCenterApplied = true

                map.controller.setCenter(
                    GeoPoint(selected.latitude, selected.longitude)
                )

                setResolvedLocation(
                    DeliveryLocation(
                        selected.address,
                        selected.latitude,
                        selected.longitude
                    )
                )
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                AddressBookState.Saved -> {
                    viewModel.consumeState()
                    Toast.makeText(
                        requireContext(),
                        R.string.address_selected,
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                }

                is AddressBookState.Error -> {
                    viewModel.consumeState()
                    Toast.makeText(
                        requireContext(),
                        state.messageRes,
                        Toast.LENGTH_LONG
                    ).show()
                }

                else -> Unit
            }
        }
    }

    private fun scheduleAddressLookup() {
        lookupJob?.cancel()

        lookupJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(LOOKUP_DEBOUNCE_MS)
            lookupAddressAtCenter()
        }
    }

    private fun lookupAddressAtCenter() {
        lookupJob?.cancel()

        lookupJob = viewLifecycleOwner.lifecycleScope.launch {
            val point = map.mapCenter

            setBusy(true)

            val address = locationProvider.resolveAddress(
                point.latitude,
                point.longitude
            )

            if (!isAdded) {
                return@launch
            }

            setResolvedLocation(
                DeliveryLocation(
                    address,
                    point.latitude,
                    point.longitude
                )
            )

            setBusy(false)
        }
    }

    private fun searchAddress(input: EditText) {
        val query = input.text?.toString()?.trim().orEmpty()

        if (query.isBlank()) {
            return
        }

        hideKeyboard(input)

        viewLifecycleOwner.lifecycleScope.launch {
            setBusy(true)

            val result = locationProvider.findByAddress(query)

            if (!isAdded) {
                return@launch
            }

            if (result == null) {
                setBusy(false)
                Toast.makeText(
                    requireContext(),
                    R.string.address_map_search_error,
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            map.controller.setCenter(
                GeoPoint(result.latitude, result.longitude)
            )
            map.controller.setZoom(SEARCH_ZOOM)

            setResolvedLocation(result)
            setBusy(false)
        }
    }

    private fun requestCurrentLocation() {
        if (requireContext().hasLocationPermission()) {
            useCurrentLocation()
        } else {
            permissionLauncher.launch(locationPermissions())
        }
    }

    private fun useCurrentLocation() {
        viewLifecycleOwner.lifecycleScope.launch {
            setBusy(true)

            val location = locationProvider.findCurrentLocation()

            if (!isAdded) {
                return@launch
            }

            if (location == null) {
                setBusy(false)
                Toast.makeText(
                    requireContext(),
                    R.string.delivery_location_unavailable,
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            map.controller.setCenter(
                GeoPoint(location.latitude, location.longitude)
            )
            map.controller.setZoom(CURRENT_LOCATION_ZOOM)

            setResolvedLocation(location)
            setBusy(false)
        }
    }

    private fun setResolvedLocation(location: DeliveryLocation) {
        selectedLocation = location
        resolvedCenter = GeoPoint(
            location.latitude,
            location.longitude
        )
        addressText.text = location.address
        setBusy(false)
    }

    private fun setBusy(busy: Boolean) {
        progress.visibility = if (busy) View.VISIBLE else View.GONE

        confirmButton.isEnabled = !busy && selectedLocation != null
        confirmButton.alpha = if (confirmButton.isEnabled) 1f else 0.5f
    }

    private fun hideKeyboard(view: View) {
        val manager = requireContext().getSystemService(
            android.content.Context.INPUT_METHOD_SERVICE
        ) as? InputMethodManager

        manager?.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    private fun distanceMeters(
        first: IGeoPoint,
        second: IGeoPoint
    ): Double {
        val earthRadius = 6_371_000.0

        val lat1 = Math.toRadians(first.latitude)
        val lat2 = Math.toRadians(second.latitude)
        val deltaLat = Math.toRadians(second.latitude - first.latitude)
        val deltaLon = Math.toRadians(second.longitude - first.longitude)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
            cos(lat1) * cos(lat2) *
            sin(deltaLon / 2) * sin(deltaLon / 2)

        return earthRadius * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        map.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        lookupJob?.cancel()
        map.onDetach()
        super.onDestroyView()
    }

    private companion object {
        const val DEFAULT_LATITUDE = 4.60971
        const val DEFAULT_LONGITUDE = -74.08175
        const val DEFAULT_ZOOM = 16.0
        const val SEARCH_ZOOM = 17.0
        const val CURRENT_LOCATION_ZOOM = 17.0
        const val LOOKUP_DEBOUNCE_MS = 600L
        const val INITIAL_LOOKUP_DELAY_MS = 700L
        const val MAX_CONFIRM_DISTANCE_METERS = 15.0
    }
}
