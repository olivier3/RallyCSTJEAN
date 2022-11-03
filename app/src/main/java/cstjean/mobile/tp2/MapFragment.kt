package cstjean.mobile.tp2

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.concurrent.TimeUnit

private const val REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES_KEY"

class MapFragment : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    /**
     * Propriété global pour Google map.
     */
    private lateinit var map: GoogleMap

    /**
     * Array des différentes permission.
     */
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    /**
     * Propriété de la request de location.
     */
    private lateinit var locationRequest: LocationRequest

    /**
     * Propriété du callback de la localisation.
     */
    private lateinit var locationCallback: LocationCallback

    /**
     * Propriété de l'état du location request.
     */
    private var requestLocationUpdates = false

    /**
     * Propriété de l'état si il est en requesting.
     */
    private var requestingLocationUpdates = false

    /**
     * Propriété de la permission request.
     */
    private val locationPermissionRequest =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    startLocationUpdates()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    startLocationUpdates()
                }
                else -> {
                    // TODO
                    // Expliquer à l'usager que la fonctionnalité n'est pas disponible car elle
                    // nécessite une permission qui a été refusée.
                    findNavController().navigate(R.id.loginFragment)
                }
            }
        }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        val cstjean = LatLng(45.2974, -73.2687)
        val pizzeria = LatLng(45.29385261757588, -73.2562410613452)
        val bistro = LatLng(45.310058584648196, -73.25235153065181)
        val garnison = LatLng(45.30101075522801, -73.28109380181546)
        map = googleMap
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(cstjean, 14.0f))
        map.addMarker(
            MarkerOptions()
                .position(pizzeria)
                .title("Checkpoint")
        )
        map.addCircle(
            CircleOptions()
                .center(pizzeria)
                .radius(100.0)
                .strokeColor(Color.RED)
        )
        map.addMarker(
            MarkerOptions()
                .position(bistro)
                .title("Checkpoint")
        )
        map.addCircle(
            CircleOptions()
                .center(bistro)
                .radius(100.0)
                .strokeColor(Color.RED)
        )
        map.addMarker(
            MarkerOptions()
                .position(garnison)
                .title("Checkpoint")
        )
        map.addCircle(
            CircleOptions()
                .center(garnison)
                .radius(100.0)
                .strokeColor(Color.RED)
        )

        if(isPermissionGranted()) {
            map.isMyLocationEnabled = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        if (savedInstanceState != null) {
            requestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY, false)
        }

        fusedLocationClient = context?.let { LocationServices.getFusedLocationProviderClient(it) }!!

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            TimeUnit.SECONDS.toMillis(2)
        ).setMaxUpdateDelayMillis(TimeUnit.SECONDS.toMillis(3))
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { showLocation(it) }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates)
        super.onSaveInstanceState(outState)
    }

    /**
     * Méthode onStart du cycle de vie.
     */
    override fun onStart() {
        super.onStart()

        when {
            context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } == PackageManager.PERMISSION_GRANTED -> {
                startLocationUpdates()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                // TODO Expliquer pourquoi la permission est nécessaire pour la fonctionnalité
            }
            else -> {
                locationPermissionRequest.launch(permissions)
            }
        }
    }

    /**
     * Méthode onResume du cycle de vie.
     */
    override fun onResume() {
        super.onResume()
        // Get a handle to the fragment and register the callback.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        if (requestingLocationUpdates) startLocationUpdates()
    }

    /**
     * Méthode qui retourne l'état de la permission.
     */
    private fun isPermissionGranted(): Boolean {
        for (permission in permissions) {
            if (context?.let {
                    ContextCompat.checkSelfPermission(
                        it,
                        permission
                    )
                } == PackageManager.PERMISSION_GRANTED
            ) return true
        }
        return false
    }

    /**
     * Méthode qui affiche la nouvelle position.
     */
    private fun showLocation(location: Location) {
        val newPosition = LatLng(location.latitude, location.longitude)
        map.moveCamera(CameraUpdateFactory.newLatLng(newPosition))
    }

    /**
     * Méthode pour partir l'update de localisation.
     */
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!isPermissionGranted() || requestLocationUpdates) return
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        requestLocationUpdates = true
    }

    /**
     * Méthode onPause du cycle de vie.
     */
    override fun onPause() {
        super.onPause()
        requestingLocationUpdates = requestLocationUpdates
        stopLocationUpdates()
    }

    /**
     * Méthode pour arrêter l'update de la localisation.
     */
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        requestLocationUpdates = false
    }
}
