package com.example.aabalalonso;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.example.aabalalonso.mapasfinal.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap; //mapa de google con el que se trabaja
    private GoogleApiClient mGoogleApiClient; //API
    private static final int LOCATION_REQUEST_CODE = 1; //peticion de la ubicacion
    private static final int REQUEST_LOCATION = 2; // latitud y longitud

    private Marker Marca; //marca para indicar un lugar determinado
    private Circle Zona; //circulo limitador de zona cambiante
    private Location myLocation; // variable para guardar posicion del usuario
    private LatLng myposition; //variable donde se guarda Latitud y Longitud

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // construccion del cliente google asignando los metodos de las interfaces que lo complementan
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .build();

        //relacionado boton con boton del xml
        //Button distancia = (Button) findViewById(R.id.distancia);
        //distancia.setOnClickListener(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Pulsa el botón para saber a que distancia estas del tesoro.\n" +
                "Cuando aparezca la marca, pulsala y escanea el codigo QR.")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //información del juego leida
                    }
                });
        builder.create();
        builder.show();

        //comprobación del permiso para poder utilizar el GPS
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //si se ha concedido permiso la localizacion se hace visible
            mMap.setMyLocationEnabled(true);
        } else {
            //al no cumplirse el if significa que el permiso no esta concedido por lo que se pide
            ActivityCompat.requestPermissions(this, new String[]
                    {android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        //configuracion del mapa
        mMap.getUiSettings().setZoomControlsEnabled(true); //botones de zoom activados
        mMap.getUiSettings().setMapToolbarEnabled(false); //toolbar innecesario desactivado

        //adición de marca invisible en el mapa del lugar del tesoro

        LatLng treasure = new LatLng(42.236905, -8.712710); //lugar de la marca
        Marca = mMap.addMarker(new MarkerOptions().position(treasure).title("tesoro").visible(false));
        mMap.setOnMarkerClickListener(this);
        //adición del circulo donde se encuentra la marca
        LatLng center = new LatLng(42.237024, -8.713554); //centro del circulo
        Zona = mMap.addCircle(new CircleOptions().center(center).radius(150).strokeColor(Color.parseColor("#084B8A")));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //comprueba si el permiso ACCESS_FINE_LOCATION ha sido concedido
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //comprueba si la petición es la de hacer la ubicación disponible
            if (requestCode == LOCATION_REQUEST_CODE) {
                //realiza el mismo codigo que en el caso de que ya estuvieran concedidos
                mMap.setMyLocationEnabled(true);
            }
            //comprueba si la petición es la de recoger la ubicación del usuario
            if (requestCode == REQUEST_LOCATION) {
                //realiza el mismo codigo que en el caso de que ya estuvieran concedidos
                myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (myLocation != null) {
                    myposition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    //una vez cargada mi localización se anima el mapa hasta el circulo donde se esconde el tesoro
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myposition, 17));
                }
            }
        }
    }

    /**
     * cuando el cliente de google se conecta llama a este método para asignar la ubicación del usuario
     * con el atributo de clase myLocation
     *
     * @param bundle recoge el bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //se comprueba si el permiso del GPS esta autorizado
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //en caso de ya tener los permisos ejecuta el codigo correspondiente
            myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (myLocation != null) {
                myposition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                //una vez cargada mi localización se anima el mapa hasta el circulo donde se esconde el tesoro
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myposition, 17));
            }
        } else {
            //al no cumplirse el if significa que el permiso no esta concedido por lo que se pide
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }

    @Override
    public void onClick(View v) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //en caso de ya tener los permisos ejecuta el codigo correspondiente
            myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (myLocation != null) {
                myposition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            }
        }
        int distancia = (int) SphericalUtil.computeDistanceBetween(myposition, Marca.getPosition());
        if (distancia > 200) {
            Toast.makeText(this, "Muy frio'\nEstas a " + distancia + "m del punto", Toast.LENGTH_LONG).show();
        } else if (distancia > 150) {
            Toast.makeText(this, "Frio\nEstas a " + distancia + "m del punto", Toast.LENGTH_LONG).show();

        } else if (distancia > 20) {
            Toast.makeText(this, "Caliente\nEstas a " + distancia + "m del punto", Toast.LENGTH_LONG).show();
        } else if (distancia <= 20) {
            Toast.makeText(this, "¡Lo encontraste!", Toast.LENGTH_LONG).show();
            Marca.setVisible(true);
        }
    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * cuando se lanza la aplicación la API del cliente de google se conecta al servidor
     */
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    /**
     * en caso de que se pare la aplicacion el cliente de google se desconecta
     */
    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }


/

@Override
public boolean onMarkerClick(Marker marker) {
    //Se instancia un objeto de la clase IntentIntegrator
    IntentIntegrator scanIntegrator = new IntentIntegrator(this);
    //Se llama al escaner
    scanIntegrator.initiateScan();
    return false;
}

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //Se obtiene el resultado del proceso de scaneo y se parsea
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            //se obtiene el contenido ya que el escaner no ha sido nulo
            String scanContent = scanningResult.getContents();
            if (scanContent.equalsIgnoreCase("PREMIO!")){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("HAS ENCONTRADO EL TESORO!\n"+"Saca una foto al codigo QR y vuelve al punto de partida.")
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(MapsActivity.this, "abre la cámara!", Toast.LENGTH_SHORT).show();
                            }
                        });
                builder.create();
                builder.show();
            }
        }else{
            Toast.makeText(this, "No has escaneado nada", Toast.LENGTH_SHORT).show();
        }
        //http://www.hablemosdeandroid.com/p/como-hacer-un-lector-de-codigos-con.html
    }
}




