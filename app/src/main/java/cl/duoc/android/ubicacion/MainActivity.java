package cl.duoc.android.ubicacion;

import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    protected Location mLastLocation;
    // el Handler sirve para ejecutar onReceiveResult desde el Thread especificado
    // , en el caso de ser NULL se usa uno arbitrario
    private AddressResultReceiver mResultReceiver = new AddressResultReceiver(new Handler());
    private GoogleApiClient mGoogleApiClient;
    private boolean mAddressRequested;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng lugar = new LatLng(-33.598511, -70.578956);

        String lugarTitle = "Duoc UC";
        mMap.addMarker(new MarkerOptions().position(lugar).title(lugarTitle));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(lugar));

        // zoom
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public void fetchAddressButtonHandler(View view) {
        // Only start the service to fetch the address if GoogleApiClient is
        // connected.
        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
            mostrarToast("Starting IntenterService");
            startIntentService();
        } else {
            mostrarToast( "Google API is not connected OR mLastLocation is NULL gapi.isConnected="+mGoogleApiClient.isConnected() );
        }
        // If GoogleApiClient isn't connected, process the user's request by
        // setting mAddressRequested to true. Later, when GoogleApiClient connects,
        // launch the service to fetch the address. As far as the user is
        // concerned, pressing the Fetch Address button
        // immediately kicks off the process of getting the address.
        mAddressRequested = true;
        updateUIWidgets();
    }

    private void updateUIWidgets() {
    }

    protected void startIntentService(){
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    public void actualizarUbicacion(View view) {
        ubicacionListener();
    }

    private void setTVText(int vid, String texto) {
        TextView tv = (TextView) findViewById(vid);
        tv.setText(texto);
    }

    private void actualizarEtiquetas(Double latitud, Double longitud) {
        setTVText(R.id.tvLatitud, latitud+"");
        setTVText(R.id.tvLongitud, longitud+"");
    }

    private void mostrarToast(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    private void usarLocalizacion(Location location) {
        this.mLastLocation = location;
        mostrarToast("Lat:"+location.getLatitude()+" Long:"+location.getLongitude());
        actualizarEtiquetas(location.getLatitude(), location.getLongitude());
    }

    public void ubicacionListener() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                usarLocalizacion(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Gets the best and most recent location currently available,
        // which may be null in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, R.string.no_geocoder_available,
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (mAddressRequested) {
                startIntentService();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mostrarToast(connectionResult.getErrorMessage());
    }

    // inner class
    class AddressResultReceiver extends ResultReceiver {

        String mAddressOutput = "";

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        public void showToast(String s) {
            Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
        }

        public void displayAddressOutput() {
            showToast(mAddressOutput);
            TextView tvDireccion = (TextView) MainActivity.this.findViewById(R.id.tvDireccion);
            tvDireccion.setText(mAddressOutput);
            showToast("Mostrando dirección en TextView ...");
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                showToast(getString(R.string.address_found));
            }

        }
    }

}
