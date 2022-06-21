package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        EditText username = (EditText) findViewById(R.id.userName);
        EditText password = (EditText) findViewById(R.id.password);

        Button login= (Button)findViewById(R.id.button_login);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("FUNCION DEL BOTON", "Username: "+username.getText() +"\nPassword: "+ password.getText() );
            }
        });
    }

}