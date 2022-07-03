package com.copiloto.User;

import android.util.JsonReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiInterface {

    @FormUrlEncoded
    @POST("/api/authentication/login/")
    Call<User> getUserInformation(@Field("username") String name, @Field("password") String job);

    @FormUrlEncoded
    @POST("/api/geofences/ep-movil-general/")
    @Headers({
            "Authorization: Token 90cd889ded745d578216ff847223583666daf6ea"
    })
    Call<JsonArray> getGeozona(@Field("id_user") String id_user);
}
