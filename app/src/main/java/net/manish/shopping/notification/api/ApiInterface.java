package net.manish.shopping.notification.api;


import com.google.gson.JsonObject;
import net.manish.shopping.model.notification.NotificationResponseModel;
import net.manish.shopping.utils.Constants;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface ApiInterface {

    @Headers({
            "Authorization: key=" + Constants.SERVER_KEY,
            "Content-Type: application/json"
    })
    @POST("send")
    Call<NotificationResponseModel> pushNotification(@Body JsonObject data);

}


