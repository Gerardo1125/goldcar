package com.copiloto;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

import android.Manifest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.OnLocationClickListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import com.mapbox.turf.TurfJoins;
import com.mapbox.turf.TurfMisc;
import com.mapbox.turf.TurfMeasurement;
import com.mapbox.turf.TurfTransformation;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;


//public class MapController extends Activity implements OnMapReadyCallback {
public class MapController extends AppCompatActivity implements
        OnMapReadyCallback, OnLocationClickListener, PermissionsListener,
        OnCameraTrackingChangedListener, LocationListener {

    private boolean go = true;

    private PermissionsManager permissionsManager;
    MapView mapView;
    private List<Point> routeCoordinates;
    private LocationComponent locationComponent;
    private LocationManager locationManager;
    private boolean isInTrackingMode, isFinalRoute = false;

    Context mContext;
    private MapboxMap mapboxMap;
    private boolean sw_metric;

    TextView v_actual, v_max, geofenceCurrentName;
    TextView v_actual_full_screen, v_max_full_screen;
    JsonObject allData;
    Double latitude;
    Double longitude;
    int speedLimit= 0, geofenceTypeCurrent = 0;

    JsonArray geofence_data;

    LinearLayout indicatorVelocity;
    LinearLayout full_screen_info, withou_full_screen_info;

    List<JsonObject> geofences_Nearby;
    List<JsonObject> interest_Geofences_Nearby;
    List<JsonObject> geofences_Iam;
    List<JsonObject> interest_geofences_Iam;
    List<Point> geofenceDataMarkersCurrent = new ArrayList<>();

    Feature geofenceMarkersData_centroid = null;
    Boolean geofenceMarkersData_point_of_interest = false;

    TextToSpeech tts;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Mapbox.getInstance(this, "sk.eyJ1IjoiZm9ydW16IiwiYSI6ImNrc2ppMXB4aTJlYnkydXM2Y2Z0cGN2MmEifQ.RWFGaBJokilq4hjhenQvzg");
        setContentView(R.layout.tracking_map);

        getSupportActionBar().hide();
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primaryColor));
        ImageButton goBack = (ImageButton) findViewById(R.id.goBack);

        withou_full_screen_info = findViewById(R.id.without_full_screen);
        full_screen_info = findViewById(R.id.full_screen_info);
        indicatorVelocity = findViewById(R.id.indicator_velocity);
        v_actual = findViewById(R.id.v_actual);
        v_max = findViewById(R.id.v_max);
        v_actual_full_screen = findViewById(R.id.v_actual_full_screen);
        v_max_full_screen = findViewById(R.id.v_max_full_screen);
        geofenceCurrentName = findViewById(R.id.geofenceNameCurrent);
        //stop_continue = findViewById(R.id.stop_continue);
        sw_metric = false;

        //LineString.

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        String aux = extras.getString("geofence_group");
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(aux);
        allData = jsonElement.getAsJsonObject();
        geofence_data = (JsonArray) allData.get("geofencess");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permisos con el identificador 1000 (1000 identifica que permiso habilitar, en este caso location)
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            // Si la version del build es correcta y se dieron los permisos anteriormente
            //Log.e("PERMISOS", "PERIMISO CONCEIDOS");
            //turnGPSOn();
            doStuff();
        }

        updateSpeed(null);

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.stop_continue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go = !go;
                if(go){
                    findViewById(R.id.stop_continue).setBackgroundResource(R.drawable.ir);

                }else{
                    findViewById(R.id.stop_continue).setBackgroundResource(R.drawable.parar);
                }
            }
        });

        findViewById(R.id.full_screen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                withou_full_screen_info.setVisibility(View.GONE);
                full_screen_info.setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.full_screen_hide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                full_screen_info.setVisibility(View.GONE);
                withou_full_screen_info.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null){
            mapView.onStart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null){
            mapView.onStop();
        }

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null){
            mapView.onLowMemory();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        locationManager.removeUpdates(this);
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(Style style) {

        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            locationComponent = mapboxMap.getLocationComponent();

            // Activate with a built LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, style).build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            // Add the camera tracking listener. Fires if the map camera is manually moved.
            locationComponent.addOnCameraTrackingChangedListener(this);
        } else {

            permissionsManager = new PermissionsManager(this);

            permissionsManager.requestLocationPermissions(this);

        }
    }

    @Override
    public void onExplanationNeeded(List<String> list) {
        Toast.makeText(this, "UBICACION", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                    doStuff();
                }
            });
            turnGPSOn();
        } else {
            Toast.makeText(this, "PERMISOS DENEGADOS", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onCameraTrackingDismissed() {
        isInTrackingMode = false;
    }

    @Override
    public void onCameraTrackingChanged(int currentMode) {

    }

    @Override
    public void onLocationComponentClick() {

    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.OUTDOORS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                enableLocationComponent(style);

                String polygonFeatureJson = "{\"type\": \"Polygon\",\"properties\": {},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[[-20.7421875,38.8225909761771],[-22.8515625,-36.03133177633187],[51.328125,-36.597889133070204],[48.515625,39.90973623453719],[-20.7421875,38.8225909761771]]]}}";

                //Log.e("POLIGONO", polygonFeatureJson);

                try {
                    Feature singleFeature = Feature.fromJson(polygonFeatureJson);
                    Polygon polygon = (Polygon) singleFeature.geometry();
                    style.addSource(new GeoJsonSource("source-id",
                            singleFeature));
                    style.addLayerBelow(new FillLayer("layer-id", "source-id").withProperties(
                            fillColor(Color.GRAY)), "settlement-label"
                    );
                }catch (Exception e){
                    //Log.e("ERROR", e.getMessage());
                }


            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) { // Si el usuario le da permisos al aplicativo
                //Log.e("PERMISOS", "PERIMISO CONCEIDOS");
                doStuff();
            } else { // Si el usuario no le da permisos al aplicativo
                Toast.makeText(this, "Activar Permisos de Ubicacion", Toast.LENGTH_SHORT).show();
                //finish();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    // UBICACION Y CALCULO DE VELOCIDAD
    @SuppressLint("MissingPermission")
    private void doStuff() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // Verificar si la ubicacion esta activado
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(this, "GPS DESHABILITADO", Toast.LENGTH_SHORT).show();
        } else if (locationManager != null) { // Si el gps esta habilitado
            sw_metric = true;
            Toast.makeText(this, "GPS HABILITADO - CARGANDO GPS", Toast.LENGTH_SHORT).show();
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
    }

    private boolean allowSpeedometer() {
        Log.e("AllowSpeedMeter", "->" + sw_metric);
        return sw_metric;
    }

    private void updateSpeed(CLocation location) {
        float nCurrentSpeed = 0;

        if(go){
            if(location != null) { // Location diferente a null
                location.setAllowCalculation(allowSpeedometer());
                nCurrentSpeed = location.getSpeed();
            }

            Formatter fmt = new Formatter(new StringBuilder());
            fmt.format(Locale.US, "%2.1f", nCurrentSpeed); // Dale formato sin decimales
            /*/String strCurrentSpeed = fmt.toString(); // Cast a string
            strCurrentSpeed = strCurrentSpeed.replace(" ","0");

            v_actual.setText(strCurrentSpeed);
            Log.e("Velocidad: ", strCurrentSpeed);*/

            v_actual.setText(String.valueOf(((int) nCurrentSpeed)));
            v_actual_full_screen.setText(String.valueOf(((int) nCurrentSpeed)));
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        if(location != null) {

            CLocation myLocation = new CLocation(location);
            updateSpeed(myLocation);
            if(!isInTrackingMode && go){
                //Log.e("On location Change", "4");
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                /*****************************************/
                geofences_Nearby = geofencesNearby();
                //Log.e("Geofences Nearby", geofences_Nearby.toString());
                //-----------------------------------------
                interest_Geofences_Nearby = interestGeofencesNearby();
                //Log.e("Inte_Geofences-Nearby", interest_Geofences_Nearby.toString());
                //-----------------------------------------
                if (interest_Geofences_Nearby.size() > 0){
                    geofences_Nearby = interest_Geofences_Nearby;
                }
                geofences_Iam = geofencesIamIn();
                //Log.e("GeofencesIamIn", geofences_Iam.toString());
                //-----------------------------------------
                interest_geofences_Iam = interestGeofencesIamIn();
                //Log.e("INTERES IAMIN",interest_geofences_Iam.toString());
                /*****************************************/
                logicRestant();
                isInTrackingMode = true;
                locationComponent.setCameraMode(CameraMode.TRACKING, 500, 15.2,null,null,null);
                //locationComponent.setCameraMode(CameraMode.TRACKING);
                //locationComponent.zoomWhileTracking(16f);

            }
            if (speedLimit != 0){
                String g = v_actual.getText().toString();
                if (speedLimit < Integer.parseInt(g)){
                    indicatorVelocity.setBackgroundResource(R.drawable.vactualrr);
                    full_screen_info.setBackgroundResource(R.drawable.fondoliteerror);
                    tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            // TODO Auto-generated method stub
                            if(status == TextToSpeech.SUCCESS){
                                int result=tts.setLanguage(Locale.US);
                                if(result==TextToSpeech.LANG_MISSING_DATA ||
                                        result==TextToSpeech.LANG_NOT_SUPPORTED){
                                    Log.e("error", "This Language is not supported");
                                }
                                else{
                                    tts.speak("Reduzca la velocidad", TextToSpeech.QUEUE_FLUSH, null);
                                }
                            }
                            else
                                Log.e("error", "Initilization Failed!");
                        }
                    });
                    tts.setLanguage(Locale.US);
                }else{
                    if (tts != null){
                        if (tts.isSpeaking()){
                            tts.shutdown();
                        }
                    }

                    indicatorVelocity.setBackgroundResource(R.drawable.vactualbr);
                    full_screen_info.setBackgroundResource(R.drawable.fondolite);
                }
            }

            if (!go){
                v_actual.setText("0");
            }

        }
    }

    private void turnGPSOn(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!provider.contains("gps")){ //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Log.e("On location Change", "2");
        doStuff();
        //LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.e("On location Change", "3");
        Toast.makeText(this, "GPS DESHABILITADO", Toast.LENGTH_SHORT).show();
        //LocationListener.super.onProviderDisabled(provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.e("On location Change", "1");
        //LocationListener.super.onStatusChanged(provider, status, extras);
    }

    private ArrayList<JsonObject> geofencesNearby(){
        //Log.e("Data QUE SE PASO", "onResponse: "+ allData);
        ArrayList<JsonObject> listGeofenceNearby = new ArrayList<>();
        for (int i = 0; i < geofence_data.size(); i++) {
            boolean isPointInGeofence = false;
            double distanceMts = 0;
            JsonObject aux = (JsonObject) geofence_data.get(i);
            String polygonFeatureJson = aux.get("geofence_data").toString().substring(1, aux.get("geofence_data").toString().length()-1).replaceAll("\\\\", "");
            polygonFeatureJson = "{ \"type\": \"Feature\", \"properties\": {}, \"geometry\": " + polygonFeatureJson +"}";
            Point pt = Point.fromLngLat(longitude, latitude);
            //JsonObject geodata = polygonFeatureJson;
            /*try {
                JSONObject jsonObject = new JSONObject(aux.get("geofence_data").toString());
                Log.i("INFO",jsonObject.toString());
            }catch (Exception e){
                Log.e("Error",e.getMessage());
            }*/
            //Log.e("Data QUE SE PASO" + i, "onResponse: "+ polygonFeatureJson);

            if (latitude == null){
                i--;
                return null;
            }
            //TurfMeasurement.d
            if (aux.get("geofence_type").toString().equals("1")){
                Feature singleFeature = Feature.fromJson((polygonFeatureJson));
                Polygon polygon = (Polygon) singleFeature.geometry();
                List routeCoordinates = new ArrayList<Point>();
                for (int j = 0; j < polygon.coordinates().get(0).size(); j++) {
                    routeCoordinates.add(polygon.coordinates().get(0).get(j));
                }

                Feature distanceMisc = TurfMisc.nearestPointOnLine(pt, routeCoordinates);
                Point distanceMiscPoint = (Point) distanceMisc.geometry();
                distanceMts = TurfMeasurement.distance(pt, distanceMiscPoint) * 1000;
                isPointInGeofence = TurfJoins.inside(pt, polygon);
                if (!isPointInGeofence) {
                    //Verify is near of the linestring (25mts)
                    distanceMts = TurfMeasurement.distance(pt,
                            Point.fromLngLat(((Point) distanceMisc.geometry()).longitude(),
                                    ((Point) distanceMisc.geometry()).latitude()));
                    if (distanceMts <= 25) {
                        isPointInGeofence = true;
                    }
                }
            }else if (aux.get("geofence_type").toString().equals("2")){
                Feature singleFeature = Feature.fromJson((polygonFeatureJson));
                //Log.e("Data QUE SE PASO" + i, "onResponse: "+ singleFeature.toString());
                Polygon polygon = (Polygon) singleFeature.geometry();
                List routeCoordinates = new ArrayList<Point>();
                for (int j = 0; j < polygon.coordinates().get(0).size(); j++) {
                    routeCoordinates.add(polygon.coordinates().get(0).get(j));
                }
                //Log.e("Data QUE SE PASO" + i, "onResponse: "+ polygon.coordinates().get(0).get(0).toString());
                // turf.booleanPointInPolygon = TurfJoins.inside
                isPointInGeofence = TurfJoins.inside(pt, polygon);
                //Log.i("Punto Cercano", ""+isPointInGeofence);
                //turf.pointToLineDistance = TurfMeasurement.distance}
                Feature distanceMisc = TurfMisc.nearestPointOnLine(pt, routeCoordinates);
                Point distanceMiscPoint = (Point) distanceMisc.geometry();
                distanceMts = TurfMeasurement.distance(pt, distanceMiscPoint) * 1000;
                //distanceMts = TurfMisc.nearestPointOnLine(Point.fromLngLat(latitude, longitude), Collections.singletonList(Point.fromJson(polygonFeatureJson)));
                //Log.i("Punto Cercano", ""+distanceMisc);
                //Log.i("Punto Cercano", ""+distanceMts);
                //TurfMisc
                /*style.addSource(new GeoJsonSource("source-id",
                        singleFeature));*/
            }else{
                Feature singleFeature = Feature.fromJson((polygonFeatureJson));
                //Log.e("Data QUE SE PASO" + i, "onResponse: "+ singleFeature.toString());
                Polygon polygon = (Polygon) singleFeature.geometry();
                Point geofence_coordinate = Point.fromLngLat(polygon.coordinates().get(0).get(0).longitude() ,
                        polygon.coordinates().get(0).get(0).latitude());
                int geofence_radius = (int) polygon.coordinates().get(0).get(1).latitude();
                Point toPoint = Point.fromLngLat(polygon.coordinates().get(0).get(0).longitude(),
                        polygon.coordinates().get(0).get(0).latitude());
                double radius = geofence_radius / 1000;

                Polygon toCircle = TurfTransformation.circle(toPoint, radius);
                List auxRouteCoordinates = new ArrayList<Point>();
                for (int j = 0; j < polygon.coordinates().get(0).size(); j++) {
                    //Log.e("COORDENADAS_otro ", polygon.coordinates().get(0).get(j).toString());
                    auxRouteCoordinates.add(polygon.coordinates().get(0).get(j));
                }
                isPointInGeofence = TurfJoins.inside(pt, toCircle);

                Feature distanceMisc = TurfMisc.nearestPointOnLine(pt, auxRouteCoordinates);
                Point distanceMiscPoint = (Point) distanceMisc.geometry();
                distanceMts = TurfMeasurement.distance(pt, distanceMiscPoint);
                //String json = "{ \"steps\": 40, \"units\": \"kilometers\", \"properties\": { \"foo\": \"bar\" } }";
            }
            //JsonObject a = LineString.fromJson((polygonFeatureJson));
            //int distanceMts = 0;
            //boolean isPointInGeofence = false;

            if (distanceMts <= Integer.parseInt(aux.get("alert_range").toString()) && isPointInGeofence){
                listGeofenceNearby.add(aux);
            }
        }
        return listGeofenceNearby;
    }

    private ArrayList<JsonObject> interestGeofencesNearby(){
        ArrayList<JsonObject> listGeofenceNearby = new ArrayList<>();
        Bundle extras = getIntent().getExtras();
        String aux = extras.getString("interestGeofences");
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(aux);
        JsonObject interestGeofence = jsonElement.getAsJsonObject();
        JsonArray auxList = (JsonArray) interestGeofence.get("geofencess");

        JsonObject uniqueInterestGeofence = (JsonObject)auxList.get(0);
        if (Integer.parseInt(uniqueInterestGeofence.get("alert_range").toString()) == 0){
            if (Integer.parseInt(uniqueInterestGeofence.get("geofence_type").toString()) == 3){
                String coordinateString = uniqueInterestGeofence.get("geofence_data").toString().substring(1,
                        uniqueInterestGeofence.get("geofence_data").toString().length()-1).replaceAll("\\\\", "");
                JsonParser jsonParser = new JsonParser();
                JsonElement element = jsonParser.parse(coordinateString);
                JsonObject coordinate = element.getAsJsonObject();
                double distanceMts = 0;
                boolean isPointInGeofence = false;
                Point pt = Point.fromLngLat(longitude, latitude);
                JsonArray jsonArray = (JsonArray) coordinate.get("coordinates");
                JsonArray aux1JsonArray = (JsonArray) jsonArray.get(0);
                JsonArray aux2JsonArray = (JsonArray) jsonArray.get(1);
                Point toPoint = Point.fromLngLat(Double.parseDouble(aux1JsonArray.get(0).toString()), Double.parseDouble(aux1JsonArray.get(1).toString()));
                double geofence_radius = Double.parseDouble(aux2JsonArray.get(0).toString());
                double radius = geofence_radius / 1000;
                Polygon toCircle = TurfTransformation.circle(toPoint, radius);
                List auxRouteCoordinates = new ArrayList<Point>();
                for (int j = 0; j < toCircle.coordinates().get(0).size(); j++) {
                    auxRouteCoordinates.add(toCircle.coordinates().get(0).get(j));
                }
                //Point pt = Point.fromLngLat(longitude, latitude);
                Feature distanceMisc = TurfMisc.nearestPointOnLine(pt, auxRouteCoordinates);
                Point distanceMiscPoint = (Point) distanceMisc.geometry();
                isPointInGeofence = TurfJoins.inside(pt, toCircle);
                distanceMts = TurfMeasurement.distance(pt, distanceMiscPoint);

                if (!TurfJoins.inside(pt, toCircle)){
                    if (distanceMts <= Double.parseDouble(uniqueInterestGeofence.get("alert_range").toString())){
                        listGeofenceNearby.add(uniqueInterestGeofence);
                    }
                }

/*                Log.e("Interest Geofence", coordinate.toString());
                Log.e("Interest Geofence", aux1JsonArray.get(0).toString());
                Log.e("Interest Geofence", toPoint.toString());
                Log.e("Interest Geofence", String.valueOf(geofence_radius));*/
            }
        }
        return listGeofenceNearby;
    }

    private ArrayList<JsonObject> geofencesIamIn(){
        ArrayList<JsonObject> listgeofencesIamIn = new ArrayList<>();

        for (int i = 0; i < geofence_data.size(); i++) {
            JsonObject aux = (JsonObject) geofence_data.get(i);
            String polygonFeatureJson = aux.get("geofence_data").toString().substring(1, aux.get("geofence_data").toString().length()-1).replaceAll("\\\\", "");

            boolean isPointInGeofence = false;
            Point pt = Point.fromLngLat(longitude, latitude);
            if (Integer.parseInt(aux.get("geofence_type").toString()) == 1){
                polygonFeatureJson = "{ \"type\": \"Feature\", \"properties\": {}, \"geometry\": " + polygonFeatureJson +"}";
                Feature singleFeature = Feature.fromJson((polygonFeatureJson));
                Polygon polygon = (Polygon) singleFeature.geometry();

                isPointInGeofence = TurfJoins.inside(pt, polygon);
                if (!isPointInGeofence){
                    List routeCoordinates = new ArrayList<Point>();
                    for (int j = 0; j < polygon.coordinates().get(0).size(); j++) {
                        routeCoordinates.add(polygon.coordinates().get(0).get(j));
                    }
                    Feature distanceMisc = TurfMisc.nearestPointOnLine(Point.fromLngLat(longitude, latitude), routeCoordinates);
                    Point distanceMiscPoint = (Point) distanceMisc.geometry();

                    double distanceMts = TurfMeasurement.distance(distanceMiscPoint, pt)* 1000;
                    if (distanceMts <= 45) {
                        isPointInGeofence = true;
                    }
                }
                //Log.e("DATA",aux.get("geofence_type").toString());
            }else if (Integer.parseInt(aux.get("geofence_type").toString()) == 2){
                polygonFeatureJson = "{ \"type\": \"Feature\", \"properties\": {}, \"geometry\": " + polygonFeatureJson +"}";
                Feature singleFeature = Feature.fromJson((polygonFeatureJson));
                Polygon polygon = (Polygon) singleFeature.geometry();
                isPointInGeofence = TurfJoins.inside(pt, polygon);
                //Log.e("DATA",String.valueOf(isPointInGeofence));
            }else{
                JsonParser jsonParser = new JsonParser();
                JsonElement element = jsonParser.parse(polygonFeatureJson);
                JsonObject coordinate = element.getAsJsonObject();
                JsonArray jsonArray = (JsonArray) coordinate.get("coordinates");
                JsonArray aux1JsonArray = (JsonArray) jsonArray.get(0);
                JsonArray aux2JsonArray = (JsonArray) jsonArray.get(1);
                Point toPoint = Point.fromLngLat(Double.parseDouble(aux1JsonArray.get(0).toString()), Double.parseDouble(aux1JsonArray.get(1).toString()));
                double geofence_radius = Double.parseDouble(aux2JsonArray.get(0).toString());
                double radius = geofence_radius / 1000;
                Polygon toCircle = TurfTransformation.circle(toPoint, radius);

                isPointInGeofence = TurfJoins.inside(pt, toCircle);
            }
            if (isPointInGeofence){
                listgeofencesIamIn.add(aux);
            }
        }
        return  listgeofencesIamIn;
    }

    private ArrayList<JsonObject> interestGeofencesIamIn(){
        ArrayList<JsonObject> listInterestGeofenceIamIn = new ArrayList<>();
        Bundle extras = getIntent().getExtras();
        String aux = extras.getString("interestGeofences");
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(aux);
        JsonObject interestGeofence = jsonElement.getAsJsonObject();
        JsonArray auxList = (JsonArray) interestGeofence.get("geofencess");

        boolean isPointInGeofence = false;
        Point pt = Point.fromLngLat(longitude, latitude);

        for (int i = 0; i < auxList.size(); i++) {
            JsonObject interestPoint = (JsonObject) auxList.get(i);
            String polygonFeatureJson = interestPoint.get("geofence_data").toString().substring(1, interestPoint.get("geofence_data").toString().length()-1).replaceAll("\\\\", "");
            polygonFeatureJson = "{ \"type\": \"Feature\", \"properties\": {}, \"geometry\": " + polygonFeatureJson +"}";

            if (Integer.parseInt(interestPoint.get("geofence_type").toString()) == 1){
                Feature singleFeature = Feature.fromJson((polygonFeatureJson));
                Polygon polygon = (Polygon) singleFeature.geometry();
                isPointInGeofence = TurfJoins.inside(pt, polygon);
                if (!isPointInGeofence){
                    List routeCoordinates = new ArrayList<Point>();
                    for (int j = 0; j < polygon.coordinates().get(0).size(); j++) {
                        routeCoordinates.add(polygon.coordinates().get(0).get(j));
                    }
                    Feature distanceMisc = TurfMisc.nearestPointOnLine(Point.fromLngLat(longitude, latitude), routeCoordinates);
                    Point distanceMiscPoint = (Point) distanceMisc.geometry();

                    double distanceMts = TurfMeasurement.distance(distanceMiscPoint, pt)* 1000;
                    if (distanceMts <= 45) {
                        isPointInGeofence = true;
                    }
                }
            }else if (Integer.parseInt(interestPoint.get("geofence_type").toString()) == 2){
                //Log.e("Ocurrui en "+i, polygonFeatureJson);
                Feature singleFeature = Feature.fromJson((polygonFeatureJson));
                Polygon polygon = (Polygon) singleFeature.geometry();

                isPointInGeofence = TurfJoins.inside(pt, polygon);
                //Log.e("DATA",String.valueOf(isPointInGeofence));
            }else{
                JsonParser jsonParser = new JsonParser();
                JsonElement element = jsonParser.parse(polygonFeatureJson);
                JsonObject coordinate = element.getAsJsonObject();
                JsonArray jsonArray = (JsonArray) coordinate.get("coordinates");
                JsonArray aux1JsonArray = (JsonArray) jsonArray.get(0);
                JsonArray aux2JsonArray = (JsonArray) jsonArray.get(1);
                Point toPoint = Point.fromLngLat(Double.parseDouble(aux1JsonArray.get(0).toString()), Double.parseDouble(aux1JsonArray.get(1).toString()));
                double geofence_radius = Double.parseDouble(aux2JsonArray.get(0).toString());
                double radius = geofence_radius / 1000;
                Polygon toCircle = TurfTransformation.circle(toPoint, radius);

                isPointInGeofence = TurfJoins.inside(pt, toCircle);
            }

            if (isPointInGeofence){
                listInterestGeofenceIamIn.add(interestPoint);
            }
        }

        return listInterestGeofenceIamIn;
    }

    private void logicRestant(){
        if ( interest_geofences_Iam.size() > 0) {
            //Log.e("ESTOY EN P. INTERES", interest_geofences_Iam.toString());
            //setInterestCurrentGeofences(interestGeofencesIamIn);
            //geofencesIamIn = interestGeofencesIamIn;
        }

        if (geofences_Iam.size() == 0){
            //Log.e("SppedLimit", 0);
            geofenceCurrentName.setText("GOLDCAR-COPILOTO");
            speedLimit = 0;
            geofenceTypeCurrent = 0;

            //setGeofenceDataMarkersCurrent({});
        }else if (geofences_Iam.size() == 1){
            JsonObject aux = geofences_Iam.get(0);
            geofenceTypeCurrent = Integer.parseInt(aux.get("geofence_type").toString());
            geofenceCurrentName.setText(aux.get("name").toString());
            speedLimit = Integer.parseInt(aux.get("speed_limit").toString());

            if (interest_geofences_Iam.size() > 0) {
                if (geofences_Nearby.get(0).get("pk").toString() == interest_geofences_Iam.get(0).get("pk").toString()) {
                    geofences_Nearby = new ArrayList<>();
                }
            }
            if (geofences_Nearby.size() > 0 && geofences_Iam.size() > 0) {
                if (geofences_Nearby.get(0).get("pk").toString() == geofences_Iam.get(0).get("pk").toString()) {
                    geofences_Nearby = new ArrayList<>();
                }
            }
            if ( interest_Geofences_Nearby.size() == 0 && geofences_Nearby.size() > 0) {
                if ((Integer.parseInt(geofences_Nearby.get(0).get("order").toString()) +1)  == Integer.parseInt(geofences_Iam.get(0).get("order").toString())) {
                    //Nearby geofence is the geofenche that I was before
                    geofences_Nearby = new ArrayList<>();
                }
            }
            int maxOrder = 0;
            for (int index = 0; index < geofence_data.size(); index++) {
                JsonObject aux_geofenceData = (JsonObject) geofence_data.get(index);
                int order = Integer.parseInt(aux_geofenceData.get("order").toString());
                if (order > maxOrder) {
                    maxOrder = order;
                }
            }

            if (Integer.parseInt(geofences_Iam.get(0).get("geofence_type").toString()) == 1){
                JsonObject aux_geofencesIam = geofences_Iam.get(0);
                String polygonFeatureJson = aux_geofencesIam.get("geofence_data").toString().substring(1, aux_geofencesIam.get("geofence_data").toString().length()-1).replaceAll("\\\\", "");
                polygonFeatureJson = "{ \"type\": \"Feature\", \"properties\": {}, \"geometry\": " + polygonFeatureJson +"}";
                Feature singleFeature = Feature.fromJson((polygonFeatureJson));
                Polygon polygon = (Polygon) singleFeature.geometry();
                List routeCoordinates = new ArrayList<Point>();
                for (int j = 0; j < polygon.coordinates().get(0).size(); j++) {
                    //Point alaba = polygon.coordinates().get(0).get(j)
                    routeCoordinates.add(polygon.coordinates().get(0).get(j));
                }
                geofenceDataMarkersCurrent = routeCoordinates;

                int orderCurrentGeofence = Integer.parseInt(geofences_Iam.get(0).get("order").toString());
                if (maxOrder == orderCurrentGeofence){
                    Point pt = Point.fromLngLat(longitude, latitude);
                    JsonObject aux_geofenceIam = geofences_Iam.get(0);
                    String featureJson = aux_geofenceIam.get("geofence_data").toString().substring(1, aux_geofenceIam.get("geofence_data").toString().length()-1).replaceAll("\\\\", "");
                    featureJson = "{ \"type\": \"Feature\", \"properties\": {}, \"geometry\": " + featureJson +"}";

                    Feature singleFeatureOrder = Feature.fromJson((featureJson));
                        Polygon polygonOrder = (Polygon) singleFeatureOrder.geometry();

                        Point ptFinal = polygonOrder.coordinates().get(0).get(polygon.coordinates().get(0).size());
                    double distanceMts = TurfMeasurement.distance(pt, ptFinal) *1000;
                    if (distanceMts < 20){
                        isFinalRoute = true;
                    }else {
                        isFinalRoute = false;
                    }
                }else{
                    isFinalRoute = false;
                }
            } else if (Integer.parseInt(geofences_Iam.get(0).get("geofence_type").toString()) == 2){
                //Polygon geofence
                //Calculate the centroid of polygon
                String featureJson = geofences_Iam.get(0).get("geofence_data").toString().substring(1, geofences_Iam.get(0).get("geofence_data").toString().length()-1).replaceAll("\\\\", "");
                featureJson = "{ \"type\": \"Feature\", \"properties\": {}, \"geometry\": " + featureJson +"}";

                Feature singleFeatureOrder = Feature.fromJson((featureJson));
                geofenceMarkersData_centroid = TurfMeasurement.center(singleFeatureOrder);
                JsonObject aux_geofenceIam = geofences_Iam.get(0);
                String featureJsonIam = aux_geofenceIam.get("geofence_data").toString().substring(1, aux_geofenceIam.get("geofence_data").toString().length()-1).replaceAll("\\\\", "");
                featureJsonIam = "{ \"type\": \"Feature\", \"properties\": {}, \"geometry\": " + featureJsonIam +"}";
                Feature singleFeatureOrderIam = Feature.fromJson((featureJsonIam));
                Polygon polygon = (Polygon) singleFeatureOrderIam.geometry();
                List routeCoordinates = new ArrayList<Point>();
                for (int j = 0; j < polygon.coordinates().get(0).size(); j++) {
                    //Point alaba = polygon.coordinates().get(0).get(j)
                    routeCoordinates.add(polygon.coordinates().get(0).get(j));
                }
                geofenceDataMarkersCurrent = routeCoordinates;
                geofenceMarkersData_point_of_interest = Boolean.valueOf(geofences_Iam.get(0).get("is_point_interest").toString());

                int orderCurrentGeofence = Integer.parseInt(geofences_Iam.get(0).get("order").toString());
                if (maxOrder  == orderCurrentGeofence){
                    Point pt = Point.fromLngLat(longitude, latitude);
                    String featureJson2 = aux_geofenceIam.get("geofence_data").toString().substring(1, aux_geofenceIam.get("geofence_data").toString().length()-1).replaceAll("\\\\", "");
                    featureJson2 = "{ \"type\": \"Feature\", \"properties\": {}, \"geometry\": " + featureJson2 +"}";
                    Feature singleFeatureOrder2 = Feature.fromJson((featureJson2));
                    Polygon polygonOrder = (Polygon) singleFeatureOrder2.geometry();

                    Point ptFinal = polygonOrder.coordinates().get(0).get(polygon.coordinates().get(0).size());
                    double distanceMts = TurfMeasurement.distance(pt, ptFinal) *1000;
                    if (distanceMts < 20) {
                        isFinalRoute = (true);
                    } else {
                        isFinalRoute = (false);
                    }
                }else{
                    isFinalRoute = false;
                }
            }else{
                //Point geofence
                JsonObject aux_geofenceIam = geofences_Iam.get(0);
                String featureJsonIam = aux_geofenceIam.get("geofence_data").toString().substring(1, aux_geofenceIam.get("geofence_data").toString().length()-1).replaceAll("\\\\", "");
                featureJsonIam = "{ \"type\": \"Feature\", \"properties\": {}, \"geometry\": " + featureJsonIam +"}";
                Feature singleFeatureOrderIam = Feature.fromJson((featureJsonIam));
                Polygon polygon = (Polygon) singleFeatureOrderIam.geometry();
                List routeCoordinates = new ArrayList<Point>();
                for (int j = 0; j < polygon.coordinates().get(0).size(); j++) {
                    //Point alaba = polygon.coordinates().get(0).get(j)
                    routeCoordinates.add(polygon.coordinates().get(0).get(j));
                }
                routeCoordinates.remove(routeCoordinates.size()-1);
                geofenceDataMarkersCurrent = routeCoordinates;
                geofenceMarkersData_point_of_interest = Boolean.valueOf(geofences_Iam.get(0).get("is_point_interest").toString());

                int orderCurrentGeofence = Integer.parseInt(geofences_Iam.get(0).get("order").toString());
                if (maxOrder == orderCurrentGeofence){
                    Point pt = Point.fromLngLat(longitude, latitude);
                    String featureJson2 = aux_geofenceIam.get("geofence_data").toString().substring(1, aux_geofenceIam.get("geofence_data").toString().length()-1).replaceAll("\\\\", "");
                    featureJson2 = "{ \"type\": \"Feature\", \"properties\": {}, \"geometry\": " + featureJson2 +"}";

                    Feature singleFeatureOrder2 = Feature.fromJson((featureJson2));
                    Polygon polygonOrder = (Polygon) singleFeatureOrder2.geometry();

                    Point ptFinal = polygonOrder.coordinates().get(0).get(0);
                    double distanceMts = TurfMeasurement.distance(pt, ptFinal) * 1000;
                    if (distanceMts < 20) {
                        isFinalRoute = (true);
                    } else {
                        isFinalRoute = (false);
                    }
                }else{
                    isFinalRoute = false;
                }
            }
        }else if (geofences_Iam.size() > 2){
            JsonObject geofencePriority;
            ArrayList<JsonObject> geofencePriorities = new ArrayList<>();
            boolean point_of_interest = false;
            for (int i = 0; i < geofences_Iam.size(); i++) {
                if (Boolean.valueOf(geofences_Iam.get(i).get("is_point_interest").toString())){
                    geofencePriorities.add(geofences_Iam.get(i));
                }
            }
            if (geofencePriorities.size() == 0){
                geofencePriority = geofences_Iam.get(0);
            }else{
                geofencePriority = geofencePriorities.get(0);
                point_of_interest = true;
            }
            geofenceTypeCurrent = Integer.parseInt(geofencePriority.get("geofence_type").toString());
            geofenceCurrentName.setText(geofencePriority.get("name").toString());
            speedLimit = Integer.parseInt(geofencePriority.get("speed_limit").toString());

            if (interest_geofences_Iam.size() > 0){
                if (Integer.parseInt(geofences_Nearby.get(0).get("pk").toString()) ==
                        Integer.parseInt(interest_geofences_Iam.get(0).get("pk").toString())){
                    geofences_Nearby = new ArrayList<>();
                }
            }
            if (Integer.parseInt(geofences_Nearby.get(0).get("pk").toString()) ==
                    Integer.parseInt(geofencePriority.get("pk").toString())){
                geofences_Nearby = new ArrayList<>();
            }
            if (interest_Geofences_Nearby.size() == 0 && geofences_Nearby.size() >0){
                if ((Integer.parseInt(geofences_Nearby.get(0).get("order").toString())+1) ==
                        Integer.parseInt(geofencePriority.get("order").toString())){
                    geofences_Nearby = new ArrayList<>();
                }
            }
            int maxOrder = 0;
            for (int i = 0; i < geofence_data.size(); i++) {
                JsonObject aux_geodata = (JsonObject) geofence_data.get(0);
                int order = Integer.parseInt(aux_geodata.get("order").toString());
                if (order > maxOrder) {
                    maxOrder = order;
                }
            }
            // Format data to send component map to draw markers
            //var geofenceMarkersData = {};
            if (Integer.parseInt(geofencePriority.get("geofence_type").toString()) == 1){
                String featureJsonIam = geofencePriority.get("geofence_data").toString().substring(1, geofencePriority.get("geofence_data").toString().length()-1).replaceAll("\\\\", "");
                featureJsonIam = "{ \"type\": \"Feature\", \"properties\": {}, \"geometry\": " + featureJsonIam +"}";
                Feature singleFeatureOrderIam = Feature.fromJson((featureJsonIam));
                Polygon polygon = (Polygon) singleFeatureOrderIam.geometry();
                List routeCoordinates = new ArrayList<Point>();
                for (int j = 0; j < polygon.coordinates().get(0).size(); j++) {
                    //Point alaba = polygon.coordinates().get(0).get(j)
                    routeCoordinates.add(polygon.coordinates().get(0).get(j));
                }
                geofenceDataMarkersCurrent = routeCoordinates;
                //Calc if the current position is the final
                int orderCurrentGeofence = Integer.parseInt(geofencePriority.get("order").toString());
                if (maxOrder == orderCurrentGeofence){
                    String featureJsonIam2 = geofences_Iam.get(0).get("geofence_data").toString().substring(1, geofences_Iam.get(0).get("geofence_data").toString().length()-1).replaceAll("\\\\", "");
                    featureJsonIam2 = "{ \"type\": \"Feature\", \"properties\": {}, \"geometry\": " + featureJsonIam2 +"}";
                    Feature singleFeatureOrderIam2 = Feature.fromJson((featureJsonIam2));
                    Polygon polygon2 = (Polygon) singleFeatureOrderIam2.geometry();
                    List routeCoordinates2 = new ArrayList<Point>();
                    for (int j = 0; j < polygon2.coordinates().get(0).size(); j++) {
                        //Point alaba = polygon.coordinates().get(0).get(j)
                        routeCoordinates2.add(polygon.coordinates().get(0).get(j));
                    }
                    //591
                    Point pt = Point.fromLngLat(longitude, latitude);
                    Point ptFinal = (Point) routeCoordinates.get(routeCoordinates2.size()-1);
                    double distanceMts = TurfMeasurement.distance(pt, ptFinal);
                    if (distanceMts < 20){
                        isFinalRoute = true;
                    }else{
                        isFinalRoute = false;
                    }
                }else{
                    isFinalRoute = false;
                }
                //602
            }else if (Integer.getInteger(geofencePriority.get("geofence_type").toString()) == 2){
                String featureJsonPriority = geofencePriority.get("geofence_data").toString().substring(1, geofencePriority.get("geofence_data").toString().length()-1).replaceAll("\\\\", "");
                featureJsonPriority = "{ \"type\": \"Feature\", \"properties\": {}, \"geometry\": " + featureJsonPriority +"}";
                Feature singleFeaturePriority = Feature.fromJson((featureJsonPriority));
                geofenceMarkersData_centroid = singleFeaturePriority;
                //610
                Polygon polygon = (Polygon) singleFeaturePriority.geometry();
                List routeCoordinates = new ArrayList<Point>();
                for (int j = 0; j < polygon.coordinates().get(0).size(); j++) {
                    //Point alaba = polygon.coordinates().get(0).get(j)
                    routeCoordinates.add(polygon.coordinates().get(0).get(j));
                }
                geofenceDataMarkersCurrent = routeCoordinates;
                geofenceMarkersData_point_of_interest = point_of_interest;

                //616 Calc if the current position is the final
                int orderCurrentGeofence = Integer.parseInt(geofencePriority.get("order").toString());
                if (maxOrder == orderCurrentGeofence){
                    Point pt = Point.fromLngLat(longitude, latitude);
                    Point ptFinal = (Point) routeCoordinates.get(routeCoordinates.size()-1);
                    double distanceMts = TurfMeasurement.distance(pt, ptFinal) *1000;

                    if (distanceMts < 20){
                        isFinalRoute = true;
                    }else{
                        isFinalRoute = false;
                    }
                }else{
                    isFinalRoute = false;
                }
                //631
            }else{
                String featureJsonPriority = geofencePriority.get("geofence_data").toString().substring(1, geofencePriority.get("geofence_data").toString().length()-1).replaceAll("\\\\", "");
                featureJsonPriority = "{ \"type\": \"Feature\", \"properties\": {}, \"geometry\": " + featureJsonPriority +"}";
                Feature singleFeaturePriority = Feature.fromJson((featureJsonPriority));
                Polygon polygon = (Polygon) singleFeaturePriority.geometry();
                List routeCoordinates = new ArrayList<Point>();
                for (int j = 0; j < polygon.coordinates().get(0).size(); j++) {
                    //Point alaba = polygon.coordinates().get(0).get(j)
                    routeCoordinates.add(polygon.coordinates().get(0).get(j));
                }
                routeCoordinates.remove(routeCoordinates.size()-1);
                geofenceDataMarkersCurrent = routeCoordinates;
                geofenceMarkersData_point_of_interest = point_of_interest;

                int orderCurrentGeofence = Integer.parseInt(geofencePriority.get("order").toString());
                if (maxOrder == orderCurrentGeofence){
                    Point pt = Point.fromLngLat(longitude, latitude);
                    Point ptFinal = (Point) routeCoordinates.get(0);
                    double distanceMts = TurfMeasurement.distance(pt, ptFinal) *1000;

                    if (distanceMts < 20){
                        isFinalRoute = true;
                    }else{
                        isFinalRoute = false;
                    }
                }else{
                    isFinalRoute = false;
                }
            }
        }
        //Step 5:
        // Calc Speed current
        v_max.setText(String.valueOf(speedLimit));
        v_max_full_screen.setText(String.valueOf(speedLimit));
    }
}
