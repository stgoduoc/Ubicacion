package cl.duoc.android.ubicacion;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
}
