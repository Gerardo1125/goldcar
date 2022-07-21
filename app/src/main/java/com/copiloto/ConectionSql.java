package com.copiloto;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.copiloto.User.User;

import java.util.ArrayList;

public class ConectionSql extends SQLiteOpenHelper {

    final String CREATE_TABLA_USUARIO = "CREAtE TABLE usuarios (id INTEGER, token TEXT, password TEXT)";
    final String CREATE_TABLA_GEOFENCES = "CREAtE TABLE geofences (id INTEGER, data TEXT)";

    public ConectionSql(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLA_USUARIO);
        db.execSQL(CREATE_TABLA_GEOFENCES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        db.execSQL("DROP TABLE IF EXISTS geofences");
        onCreate(db);
    }


    public ArrayList getAllCotacts() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<User> array_list = new ArrayList<User>();
        Cursor res = db.rawQuery( "select * from usuarios", null );
        res.moveToFirst();
        if (res.moveToFirst()) {
            do {
                // on below line we are adding the data from cursor to our array list.
                array_list.add(new User(res.getString(1),
                        res.getInt(0), res.getString(2)));
            } while (res.moveToNext());
            // moving our cursor to next.
        }
        // at last closing our cursor
        // and returning our array list.
        res.close();
        return array_list;
    }

    public void deleteElement(){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from usuarios");
    }

    public ArrayList getAllGeofences() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> array_list = new ArrayList<>();
        Cursor res = db.rawQuery( "select * from geofences", null );
        res.moveToFirst();
        if (res.moveToFirst()) {
            do {
                // on below line we are adding the data from cursor to our array list.
                array_list.add(res.getString(1));
            } while (res.moveToNext());
            // moving our cursor to next.
        }
        // at last closing our cursor
        // and returning our array list.
        res.close();
        return array_list;
    }

    public void deleteGeofences(){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from geofences");
    }
}
