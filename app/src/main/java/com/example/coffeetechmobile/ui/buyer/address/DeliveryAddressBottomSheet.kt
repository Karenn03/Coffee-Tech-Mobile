package com.example.coffeetechmobile.ui.buyer.address

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeetechmobile.R
import com.example.coffeetechmobile.data.model.DeliveryAddress
import com.example.coffeetechmobile.ui.buyer.location.hasLocationPermission
import com.example.coffeetechmobile.ui.buyer.location.locationPermissions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Hoja inferior con las direcciones guardadas del comprador
 *
 * Permite seleccionar una dirección, eliminarla o agregar una nueva
 * usando la ubicación actual o el selector del mapa
 */
class DeliveryAddressBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: AddressBookViewModel by activityViewModels()

    private lateinit var addressAdapter: DeliveryAddressAdapter

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted =
            permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            viewModel.captureCurrentLocation()
        } else {
            Toast.makeText(
                requireContext(),
                R.string.delivery_location_permission_denied,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.bottom_sheet_delivery_address,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        addressAdapter = DeliveryAddressAdapter(
            onSelect = { address ->
                viewModel.selectAddress(address.id)
            },
            onDelete = { address ->
                confirmDelete(address)
            }
        )

        val recycler = view.findViewById<RecyclerView>(
            R.id.address_list
        )

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = addressAdapter
        recycler.isNestedScrollingEnabled = false

        val emptyText = view.findViewById<TextView>(
            R.id.address_empty_text
        )

        view.findViewById<View>(R.id.address_close_button)
            .setOnClickListener {
                dismiss()
            }

        view.findViewById<View>(R.id.address_use_current_button)
            .setOnClickListener {
                requestCurrentLocation()
            }

        view.findViewById<View>(R.id.address_pick_map_button)
            .setOnClickListener {
                val navController = parentFragment?.findNavController()
                dismiss()
                navController?.navigate(R.id.mapAddressPickerFragment)
            }

        viewModel.addresses.observe(viewLifecycleOwner) { addresses ->
            addressAdapter.submitList(addresses)
            addressAdapter.selectedAddressId =
                viewModel.selectedAddressId.value

            emptyText.visibility = if (addresses.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        viewModel.selectedAddressId.observe(viewLifecycleOwner) { id ->
            addressAdapter.selectedAddressId = id
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                AddressBookState.Idle -> Unit

                AddressBookState.Locating -> Toast.makeText(
                    requireContext(),
                    R.string.delivery_location_locating,
                    Toast.LENGTH_SHORT
                ).show()

                is AddressBookState.Candidate -> {
                    val location = state.location
                    viewModel.consumeState()

                    showAddressLabelDialog(location.address) { label ->
                        viewModel.saveAddress(label, location)
                    }
                }

                AddressBookState.Saved -> {
                    viewModel.consumeState()
                    Toast.makeText(
                        requireContext(),
                        R.string.address_selected,
                        Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                }

                AddressBookState.Deleted -> {
                    viewModel.consumeState()
                    Toast.makeText(
                        requireContext(),
                        R.string.address_deleted,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is AddressBookState.Error -> {
                    viewModel.consumeState()
                    Toast.makeText(
                        requireContext(),
                        state.messageRes,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        viewModel.loadAddresses()
    }

    override fun onStart() {
        super.onStart()

        (dialog as? BottomSheetDialog)?.behavior?.state =
            BottomSheetBehavior.STATE_EXPANDED
    }

    private fun requestCurrentLocation() {
        if (requireContext().hasLocationPermission()) {
            viewModel.captureCurrentLocation()
        } else {
            permissionLauncher.launch(locationPermissions())
        }
    }

    private fun confirmDelete(address: DeliveryAddress) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.address_delete_title)
            .setMessage(
                getString(
                    R.string.address_delete_message,
                    address.label
                )
            )
            .setNegativeButton(
                R.string.address_cancel,
                null
            )
            .setPositiveButton(
                R.string.address_delete_confirm
            ) { _, _ ->
                viewModel.deleteAddress(address.id)
            }
            .show()
    }

    companion object {
        const val TAG = "DeliveryAddressBottomSheet"
    }
}
