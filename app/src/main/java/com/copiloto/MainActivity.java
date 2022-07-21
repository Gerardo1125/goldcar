package com.copiloto;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.copiloto.R;
import com.copiloto.User.ApiInterface;
import com.copiloto.User.Client;
import com.copiloto.User.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Response";
    ConectionSql myConnection;
    String pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primaryColor));
        getSupportActionBar().hide();


        EditText username = (EditText) findViewById(R.id.userName);
        EditText password = (EditText) findViewById(R.id.password);

        myConnection = new ConectionSql(getApplicationContext(), "bd_usuarios", null,1);
        SQLiteDatabase database = myConnection.getReadableDatabase();
        try {
            myConnection.getAllCotacts();
            User aux = (User) myConnection.getAllCotacts().get(0);
            Log.e("DATA MAIN ACTIVITY" , aux.getToken() + "- " + aux.getUser_id());
            Intent i = new Intent(MainActivity.this, CategoryRoutes.class);
            i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            finish();
            startActivity(i);
        }catch (Exception e){
            Log.e("ERROR", e.getMessage());
        }

        Button login= (Button)findViewById(R.id.button_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("FUNCION DEL BOTON", "Username: "+username.getText() +"\nPassword: "+ password.getText() );
                btnSendPostRequestClicked(username.getText().toString().trim(), password.getText().toString().trim());

            }
        });
    }

    private void btnSendPostRequestClicked(String username, String password) {
        ApiInterface apiInterface = Client.getRetrofitInstance().create(ApiInterface.class);
        //Call<User> call = apiInterface.getUserInformation("marcobre2", "@marcobre2@");
        Call<User> call = apiInterface.getUserInformation(username, password);
        call.enqueue(new Callback<User>(){
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if ( response.code() == 400){
                    Toast.makeText(MainActivity.this, "DATOS INCORRECTOS ", Toast.LENGTH_LONG).show();
                    return;
                }
                Log.e(TAG, "onResponse: "+response.code());
                Log.e(TAG, "onResponse name: "+response.body().getToken());
                Log.e(TAG, "onResponse name: "+response.body().getUser_id());
                Log.e(TAG, "onResponse name: "+response.body().getUser_name());
                Log.e(TAG, "onResponse name: "+response.body().getUser_first_name());
                Log.e(TAG, "onResponse name: "+response.body().getUser_last_name());
                Log.e(TAG, "onResponse name: "+response.body().getUser_email());
                Log.e(TAG, "onResponse name: "+response.body().getUser_is_staff());
                Log.e(TAG, "onResponse name: "+response.body().getUser_is_super());
                Toast.makeText(MainActivity.this, "Exitoso ", Toast.LENGTH_LONG).show();

                if ( response.code() == 200){
                    addUser(response.body().getUser_id(), response.body().getToken(), password);
                    //deleteUser();
                    Intent i = new Intent(MainActivity.this, CategoryRoutes.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    finish();
                    startActivity(i);
                }

            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "onFailure: "+t.getMessage());
                Log.i("FUNCION DEL BOTON", "Username: "+username +"\nPassword: "+ password );
                Toast.makeText(MainActivity.this, "onFailure: "+t.getMessage(), Toast.LENGTH_LONG).show();
            }

        });
    }

    private void addUser(Integer id , String token, String password){

        ConectionSql conectionSql = new ConectionSql(this, "bd_usuarios",null, 1);
        SQLiteDatabase db = conectionSql.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("token", token);
        values.put("password", password);

        long newRowId = db.insert("usuarios", null, values);

        Log.e("DATOS AÑADIDOS", "EXITOSO");
    }

}