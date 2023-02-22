package com.example.puppyeyesapp;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private LocationManager ubicacion;
    private String direccion1;
    JSONObject jso;

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

                    speak();


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

    }

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
        //obtener direccion actual
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
        //RequestQueue  queue = Volley.newRequestQueue(this);
        //direccion();
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
                String location1 = "cochabamba";
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
                //mMap.addMarker(new MarkerOptions().position(miUbicacion).title("ubicacion actual"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(miUbicacion));
                coordenada = String.valueOf(location.getLatitude()+location.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(miUbicacion)
                        .zoom(80)
                        .bearing(90)
                        .tilt(45)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                /*RequestQueue queue = Volley.newRequestQueue(getParent());
                String url = "https://maps.googleapis.com/maps/api/directions/json?origin=-17.789262,-60.102875&destination=-17.789262,-68.102875";
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                             jso = new JSONObject(response);
                            dibujarRuta(jso);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                queue.add(stringRequest);*/

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
        //direccion(jso);

    }
    /*private void direccion(JSONObject jso){
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Uri.parse("https://maps.googleapis.com/maps/api/directions/json")
                .buildUpon()
                .appendQueryParameter("destination","-17.789280, -63.102392")
                .appendQueryParameter("origin","-17.795368, -63.103808")
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
                      PolylineOptions polylineOptions = null;
                      for(int i=0; i< routes.length(); i++){
                          points = new ArrayList<>();
                          polylineOptions = new PolylineOptions();
                          JSONArray legs = routes.getJSONObject(i).getJSONArray("legs");
                          for(int j=0;j<legs.length(); j++){
                              JSONArray steps = legs.getJSONObject(j).getJSONArray("steps");
                              for(int k =0 ; k< steps.length(); k++){
                                  String polyline = steps.getJSONObject(k).getJSONObject("polyline").getString("points");
                                  List<LatLng> list= decodePoly(polyline);
                                  for(int l=0; l< list.size(); l++){
                                      LatLng positi = new LatLng((list.get(l)).latitude,(list.get(l).longitude));
                                      points.add(positi);
                                  }
                              }
                          }
                          polylineOptions.addAll(points);
                          polylineOptions.width(10);
                          polylineOptions.color(ContextCompat.getColor(MapsActivity.this, R.color.purple_500));
                          polylineOptions.geodesic(true);
                      }

                      mMap.addPolyline(polylineOptions);
                      mMap.addMarker(new MarkerOptions().position(new LatLng(-17.789262,-60.102875)).title("direccion"));
                      mMap.addMarker(new MarkerOptions().position(new LatLng(-17.789262,-68.102875)).title("llegada"));
                      LatLngBounds bounds = new LatLngBounds.Builder()
                              .include(new LatLng(-17.789262,-63.102875))
                              .include(new LatLng(-17.795368, -63.103808)).build();
                      Point point1 = new Point();
                      getWindowManager().getDefaultDisplay().getSize(point1);
                      mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, point1.x, 150 , 30));

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
        RetryPolicy retryPolicy =  new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES , DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(jsonObjectRequest);
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
            while(b>=0x20);
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
    private void dibujarRuta(JSONObject jso){
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;
        try {
            jRoutes = jso.getJSONArray("routes");
                for(int i=0; i< jRoutes.length(); i++){
                    jLegs=((JSONObject)(jRoutes.get(i))).getJSONArray("legs");

                    for(int j=0;j<jLegs.length(); j++){
                        jSteps=((JSONObject)(jRoutes.get(i))).getJSONArray("steps");
                        for(int k =0 ; k< jSteps.length(); k++){
                            String polyline = ""+((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                            Log.i("end", ""+polyline);
                            List<LatLng>list = PolyUtil.decode(polyline);
                            mMap.addPolyline(new PolylineOptions().addAll(list).color(Color.GRAY).width(5));

                        }
                    }

                }

            } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }
    private void speak() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,  "es-MX");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Comienza a hablar");
        try {
            startActivityForResult(intent, 100);
        }catch (ActivityNotFoundException e)
        {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100 && resultCode == RESULT_OK && data !=null){
            ArrayList<String> arrayList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            switch (arrayList.get(0).toString())
            {
                case "dónde estoy" :
                case "ubicación actual":
                case "mi ubicación":
                    ttsManager.initQueue("Se encuentra en " + direccion1);
                    break;
                case "mandar ayuda" :
                case "solicitar ayuda":
                case "mandar mi ubicación":
                    ttsManager.initQueue("Mandando ayuda");
                    onClickWhatsapp();
                    break;
                case "llamar" :
                case "realizar llamada":
                case "ayuda por llamada":
                    ttsManager.initQueue("realizando llamada");
                    onClickLlamada();
                    break;
                default:
                    ttsManager.initQueue("no se encontro el comando vuelva a intentar");
            }

        }

    }
    public void onClickLlamada() {
        int permiso= ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.CALL_PHONE);
        if(permiso != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "No tiene permiso de llamadas", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 255 );
        }
        else{
            String nro="78575353";
            String inicio="tel:" + nro;
            Intent i = new Intent(Intent.ACTION_CALL);
            i.setData(Uri.parse(inicio));
            startActivity(i);
        }

    }
    public void onClickWhatsapp(){
        Uri uri= Uri.parse("http://wa.link/lmnslp");
        Intent i = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(i);
    }
}