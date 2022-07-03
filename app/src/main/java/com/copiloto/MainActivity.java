package com.copiloto;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primaryColor));
        getSupportActionBar().hide();



        EditText username = (EditText) findViewById(R.id.userName);
        EditText password = (EditText) findViewById(R.id.password);

        Button login= (Button)findViewById(R.id.button_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("FUNCION DEL BOTON", "Username: "+username.getText() +"\nPassword: "+ password.getText() );
                /*Intent i = new Intent(MainActivity.this, CategoryRoutes.class);
                startActivity(i);
                /*if (isNetwork(getApplicationContext())){

                    Toast.makeText(getApplicationContext(), "Internet Connected", Toast.LENGTH_SHORT).show();

                } else {

                    Toast.makeText(getApplicationContext(), "Internet Is Not Connected", Toast.LENGTH_SHORT).show();
                }*/
                btnSendPostRequestClicked(""+username.getText(), ""+password.getText());

            }
        });
    }
    public boolean isNetwork(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    private void btnSendPostRequestClicked(String username, String password) {
        ApiInterface apiInterface = Client.getRetrofitInstance().create(ApiInterface.class);
        Call<User> call = apiInterface.getUserInformation("marcobre2", "@marcobre2@");
        call.enqueue(new Callback<User>(){
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
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
                    Intent i = new Intent(MainActivity.this, CategoryRoutes.class);
                    startActivity(i);
                }
                if ( response.code() == 400){
                    Toast.makeText(MainActivity.this, "DATOS INCORRECTOS ", Toast.LENGTH_LONG).show();
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
}