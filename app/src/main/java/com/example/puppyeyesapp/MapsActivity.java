package com.example.puppyeyesapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.SearchView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.puppyeyesapp.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private LocationManager ubicacion;
    private String direccion1;
    SearchView searchView;
    String coordenada;
    String coordenada1;
    TTSManager ttsManager = null;
    // sensor
    SensorManager sensorManager;
    Sensor  sensor;
    SensorEventListener sensorEventListener;
    SensorManager msensorManager;
    Sensor msensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        try {
            getLocalizacion();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //Texto a voz
        ttsManager= new TTSManager();
        ttsManager.init(this);

        //sensor
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        final Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        msensorManager =(SensorManager) getSystemService(SENSOR_SERVICE);
        msensor  = msensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SensorEventListener proximitySensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if(sensorEvent.values[0]< proximitySensor.getMaximumRange())
                {
                    String texto = "Se encuentra en " + direccion1;
                    ttsManager.initQueue(texto);

                }
                /*movimiento
                float x= sensorEvent.values[0];
                if(x<(0) && whip ==0)
                {
                    ttsManager.initQueue("");
                    whip++;
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,  "es-MX");
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Comienza a hablar");
                    startActivityForResult(intent, 100);
                }*/

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        sensorManager.registerListener(proximitySensorListener,proximitySensor, 2*1000*1000);
        //sensorManager.registerListener(sensorEventListener, sensor1 ,SensorManager.SENSOR_DELAY_NORMAL);

        //busqueda de lugares


    }

    // parar el texto a voz
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        ttsManager.shutDown();
    }
    // direcciones maps
    public void getLocalizacion() throws IOException {
        int permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if(permiso == PackageManager.PERMISSION_DENIED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        //obtenre direccion actual
        ubicacion = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location loc = ubicacion.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> direccion= geocoder.getFromLocation(loc.getLatitude() , loc.getLongitude() ,1);
        direccion1= direccion.get(0).getAddressLine(0);

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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        LocationManager locationManager = (LocationManager) MapsActivity.this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // buscar lugares
                String location1 = "Cinecenter";
                if(location1!= null || location1 != (""))
                {
                    List<Address> addressesList = null;
                    Geocoder geo = new Geocoder(MapsActivity.this);
                    try {
                        addressesList = geo.getFromLocationName(location1,1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Address addres = addressesList.get(0);
                    LatLng latLng  = new LatLng(addres.getLatitude(), addres.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title("direccion"));
                    coordenada1= String.valueOf(addres.getLatitude()+ addres.getLongitude());
                }

                // mi ubicacion
                LatLng miUbicacion = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(miUbicacion).title("ubicacion actual"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(miUbicacion));
                coordenada = String.valueOf(location.getLatitude()+location.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(miUbicacion)
                        .zoom(40)
                        .bearing(90)
                        .tilt(45)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);


    }
    /*private void direccion(){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Uri.parse("https://googleapis.com/maps/api/directions/json").buildUpon()
                .appendQueryParameter("origin",coordenada)
                .appendQueryParameter("destino",coordenada1)
                .appendQueryParameter("node", "driving")
                .appendQueryParameter("key", "AIzaSyA1CIhVp0BxGMmRAX6i5YghAegq6D7LXTg")
                .toString();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
              try {
                  String status = response.getString("status");
                  if(status.equals("OK")){
                      JSONArray routes = response.getJSONArray("routes");
                      ArrayList<LatLng> points;
                      PolygonOptions polygonOptions = null;
                      for(int i=0; i< routes.length(); i++){
                          points = new ArrayList<>();
                          polygonOptions = new PolygonOptions();
                          JSONArray legs = routes.getJSONObject(i).getJSONArray("legs");
                          for(int j=0;j<legs.length(); j++){
                              JSONArray steps = legs.getJSONObject(j).getJSONArray("steps");
                              for(int k =0 ; k< steps.length(); k++){
                                  String polyline = steps.getJSONObject(k).getJSONObject("polyline").getString("points");
                                  List<LatLng> List= decodePoly(polyline);
                                  for(int l=0; l< List.size(); l++){
                                      LatLng positi = new LatLng((List.get(l)).latitude,(List.get(l).longitude));
                                  }
                              }
                          }
                          polygonOptions.addAll(points);
                          polygonOptions.strokeWidth(10);
                          polygonOptions.fillColor(ContextCompat.getColor((MapsActivity.this, R.color.purple_500)));
                          polygonOptions.geodesic(true);
                      }
                      mMap.addPolyline(polygonOptions);
                  }
              } catch (JSONException e) {
                  throw new RuntimeException(e);
              }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

    }
    private List<LatLng> decodePoly(String encoded){
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len= encoded.length();
        int lat=0, lng=0;
        while  (index < len){
            int b, shift=0, result=0;
            do{
                b=encoded.charAt(index++)-63;
                result |=(b & 0x1f)<< shift;
                shift +=5;
            }
            while(b>=0x28);
            int dlat = ((result & 1) != 0? ~(result >>1) : (result>>1));
            lat+= dlat;
            shift=0;
            result=0;
            do{
                b=encoded.charAt(index++)-63;
                result |=(b & 0x1f)<< shift;
                shift +=5;
            }while(b>=0x20);
            int dlng =((result &1)) !=0? ~(result>>1) : (result>>1);
            lng +=dlng;
            LatLng p = new LatLng(((double) lat / 1E5),((double) lng /1E5));
            poly.add(p);
        }
        return poly;
    }*/
}