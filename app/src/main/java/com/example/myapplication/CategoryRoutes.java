package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class CategoryRoutes extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_routs);

        ListView simpleList;
        String acidList[] = {"Acido-Gases [Cargado]", "Acido-Gases [Vacio]", "Gases-Acido"};

        simpleList = (ListView)findViewById(R.id.acid_list);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.conttent_acid_gas, R.id.textView, acidList);
        simpleList.setAdapter(arrayAdapter);

        simpleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                Log.i("The Item selected is :" , selectedItem);
            }
        });

    }
}
