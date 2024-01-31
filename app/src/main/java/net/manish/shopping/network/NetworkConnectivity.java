package net.manish.shopping.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import net.manish.shopping.model.UserModel;
import net.manish.shopping.model.notification.NotificationResponseModel;
import net.manish.shopping.notification.api.ApiClient;
import net.manish.shopping.notification.api.ApiInterface;
import net.manish.shopping.utils.CommonUtils;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.Mylogger;
import net.manish.shopping.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class NetworkConnectivity {

    private static final String TAG = "NetworkConnectivity";

    private final Context context;
    private final DatabaseReference myDbRef;
    private final String userId; //for authentication

    public NetworkConnectivity(Context context) {
        super();
        this.context = context;

        SessionManager sessionManager = new SessionManager(context);

        //For firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference();
        userId = sessionManager.getUserId();
    }


    public boolean isNetworkAvailable() {
        boolean isConnected = false;
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();


        if (activeInfo != null
                && (activeInfo.isConnected() || activeInfo.isConnectedOrConnecting())) {


            switch (activeInfo.getType()) {
                case ConnectivityManager.TYPE_MOBILE:
                case ConnectivityManager.TYPE_WIFI:
                case ConnectivityManager.TYPE_WIMAX:
                case ConnectivityManager.TYPE_ETHERNET:
                    isConnected = true;
                    break;

                default:
                    isConnected = false;
                    break;
            }
        }

        if (!isConnected) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle("No Internet Connection");
            alertDialogBuilder.setMessage("You are offline please check your internet connection");

            alertDialogBuilder.setPositiveButton("Ok", (arg0, arg1) -> arg0.dismiss());
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        return isConnected;
    }

    public void pushNotificationForThisUser(String userId, final String body, final String title, String notiFor) {

        final JsonObject bodyJsonObject = CommonUtils.createJsonObjectFromValues(this.userId, body, title, notiFor);

        myDbRef.child(Constants.TABLE_USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                UserModel userModel = dataSnapshot.getValue(UserModel.class);

                if (userModel != null && userModel.getUserId() != null) {
                    //registered
                    if (userModel.getFireToken() != null) {
                        String[] fireTokenArray = userModel.getFireToken().split(",");

                        for (String aFireTokenArray : fireTokenArray) { // send to multiple users

                            NetworkConnectivity.pushNotification(context,
                                    aFireTokenArray,
                                    bodyJsonObject);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void pushNotification(final Context context, String to,JsonObject body) {
        JsonObject data = new JsonObject();

        try {

            data.add("data", body);
            data.addProperty("to", to);

        } catch (Exception e) {
            e.printStackTrace();
        }

        ApiClient.getClient().create(ApiInterface.class).pushNotification(data).enqueue(new Callback<NotificationResponseModel>() {
            @Override
            public void onResponse(@NonNull Call<NotificationResponseModel> call, @NonNull Response<NotificationResponseModel> response) {

                if (!response.isSuccessful()) {
                    Toast.makeText(context, "Something went wrong!!!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<NotificationResponseModel> call, @NonNull Throwable t) {
                Mylogger.getInstance().printLog(TAG, "Unable to fetch json: " + t.getMessage());

            }
        });
    }
}
