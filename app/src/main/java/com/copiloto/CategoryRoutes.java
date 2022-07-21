package com.copiloto;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.DialogCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.copiloto.R;
import com.copiloto.User.ApiInterface;
import com.copiloto.User.Client;
import com.copiloto.User.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryRoutes extends Activity {

    ArrayList<JsonObject> listdata = new ArrayList<JsonObject>();
    ListView listNameGeofence, listNamegeofenceGroup;
    LinearLayout linearLayout;
    ImageButton logout;
    ConectionSql myConnection;

    String id_user, token, password_User;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_routs);
        //listNameGeofence = (ListView)findViewById(R.id.category_list);
        linearLayout = findViewById(R.id.linear_content);
        logout = findViewById(R.id.logout);
        //listNamegeofenceGroup = view.findViewById(R.id.acid_list);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primaryColor));
        myConnection = new ConectionSql(getApplicationContext(), "bd_usuarios", null, 1);

        //myConnection.getAllCotacts();
        User aux = (User) myConnection.getAllCotacts().get(0);

        id_user = String.valueOf(aux.getUser_id());
        token = aux.getToken();
        password_User = aux.getPassword();
        try {
            String geofences = (String) myConnection.getAllGeofences().get(0);

            //Log.e("DATA DE LAS GEOFENCES", geofences);
            JsonParser jsonParser = new JsonParser();
            Object object = jsonParser.parse(geofences);
            JsonArray jArray = (JsonArray) object;
            if (jArray != null) {
                for (int i = 0; i < jArray.size(); i++) {
                    listdata.add((JsonObject) jArray.get(i));
                }
            }
            Log.e("DATA DE LAS GEOFENCES", listdata.toString());
            showList();
        } catch (Exception e) {
            Log.e("ERROR EN LA BD", e.getMessage());

            try {
                /*myConnection.getAllCotacts();
                User aux = (User) myConnection.getAllCotacts().get(0);

                id_user = String.valueOf(aux.getUser_id());
                token = aux.getToken();
                ;*/
                getGeofencesData();
            } catch (Exception ex) {
                Log.e("ERROR", ex.getMessage());

            }
        }


        Log.e("DATA DE CURSOR", id_user + " - " + token);


        /*listNameGeofence.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                Log.i("The Item selected is :" , selectedItem);
                Intent i = new Intent(CategoryRoutes.this, MapController.class);
                startActivity(i);
            }
        });*/
        EditText password = new EditText(this);

        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle("Salir de su cuenta")
                        .setMessage("Ingrese su contraseña")
                                .setView(password)
                                        .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Log.e("ConfirnButton", password.getText().toString());
                                                if (password.getText().toString().equals(password_User)){
                                                    logout();
                                                }else{
                                                    password.setText("");
                                                    Toast.makeText(CategoryRoutes.this, "Contraseña incorrecta", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        })
                                                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.cancel();
                                                    }
                                                }).create();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.show();
            }
        });
    }

    private void logout(){
        deleteUser();
        deleteGeofences();
        Intent i = new Intent(CategoryRoutes.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        finish();
        startActivity(i);
    }

    private void getGeofencesData() {
        ApiInterface apiInterface = Client.getRetrofitInstance().create(ApiInterface.class);
        Call<JsonArray> call = apiInterface.getGeozona(id_user, "Token " + token);
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                //Log.e("RESPUESTA DE CONSULTA", "onResponse: "+response.body());
                JsonArray jArray = response.body();
                if (jArray != null) {
                    for (int i = 0; i < jArray.size(); i++) {
                        listdata.add((JsonObject) jArray.get(i));
                    }
                }
                addGeofence();
                Log.e("Data", "onResponse: " + listdata.get(0).get("name"));
                showList();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                Log.e("ERROR DE CONSULTA", "onFailure: " + t.getMessage());
            }
        });
    }

    private void addGeofence() {
        //Log.e("DATA DE GEOFENCES",listdata.toString());
        ConectionSql conectionSql = new ConectionSql(this, "bd_usuarios", null, 1);
        SQLiteDatabase db = conectionSql.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", 1);
        values.put("data", listdata.toString());

        Log.e("DATOS AÑADIDOS", values.toString());

        long newRowId = db.insert("geofences", null, values);

        Log.e("DATOS AÑADIDOS", "EXITOSO");
    }

    private void showList() {
        String name_geofence[] = new String[listdata.size()];
        for (int i = 0; i < listdata.size(); i++) {
            ListView listNameGeofence = new ListView(this);
            TextView textView = new TextView(this);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(45);
            textView.setTextColor(Color.parseColor("#016FC6"));
            textView.setText(String.valueOf(listdata.get(i).get("name")).replaceAll("\"", ""));
            View view = new View(this);
            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
            view.setBackgroundColor(Color.GRAY);
            //view.setTop(15);
            listNameGeofence.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


            LinearLayout listAcid = new LinearLayout(this);
            listAcid.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


            //name_geofence[i] = String.valueOf(listdata.get(i).get("name"));
            JsonArray auxList = (JsonArray) listdata.get(i).get("geofence_groups");
            String contentAcid[] = new String[auxList.size()];
            linearLayout.addView(textView);
            linearLayout.addView(view);
            JsonObject interestList = null;
            for (int j = 0; j < auxList.size(); j++) {
                JsonObject aux = (JsonObject) auxList.get(j);
                if (Boolean.valueOf(String.valueOf(aux.get("is_group_points_insterest")))) {
                    interestList = aux;
                    break;
                }
            }
            for (int j = 0; j < auxList.size(); j++) {
                JsonObject aux = (JsonObject) auxList.get(j);
                //Log.e("Data", "onResponse: "+ aux.get("name"));
                if (!Boolean.valueOf(String.valueOf(aux.get("is_group_points_insterest")))) {
                    Log.e("Data", "onResponse: " + aux.get("name"));
                    contentAcid[j] = String.valueOf(aux.get("name"));
                    TextView nameGeofence = new TextView(this);
                    nameGeofence.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    nameGeofence.setGravity(Gravity.CENTER);
                    nameGeofence.setTextSize(40);
                    nameGeofence.setTextColor(Color.parseColor("#19A7D7"));
                    nameGeofence.setText(String.valueOf(aux.get("name")).replaceAll("\"", ""));

                    JsonObject finalInterestList = interestList;
                    nameGeofence.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Log.e("Data", "onResponse: "+ aux);
                            Intent i = new Intent(CategoryRoutes.this, MapController.class);
                            i.putExtra("geofence_group", aux.toString());
                            i.putExtra("interestGeofences", finalInterestList.toString());
                            //Log.e("Puntos de interes", finalInterestList.toString());
                            startActivity(i);
                        }
                    });
                    linearLayout.addView(nameGeofence);
                    //listAcid.addView(nameGeofence);
                }
            }


            //ArrayAdapter<String> arrayAdapterNameGroupRouteContent = new ArrayAdapter<>(this, R.layout.conttent_acid_gas,R.id.name_geofence_group, contentAcid);
            //listNameGeofence.setAdapter(arrayAdapterNameGroupRouteContent);


            //listNamegeofenceGroup.setAdapter(arrayAdapterNameGroupRouteContent);
        }
        //ArrayAdapter<String> arrayAdapterNameGroupRoute = new ArrayAdapter<>(this, R.layout.group_geofence,R.id.name_geofence, name_geofence);
        //listNameGeofence.setAdapter(arrayAdapterNameGroupRoute);

    }

    private void deleteUser() {
        ConectionSql conectionSql = new ConectionSql(this, "bd_usuarios", null, 1);
        conectionSql.deleteElement();
        Log.e("DATOS Borrados", "EXITOSO");
    }
    private void deleteGeofences() {
        ConectionSql conectionSql = new ConectionSql(this, "bd_usuarios", null, 1);
        conectionSql.deleteGeofences();
        Log.e("GEOFENCES Borrados", "EXITOSO");
    }

}
