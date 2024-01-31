package net.manish.shopping.notification.api;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import net.manish.shopping.firebaseutils.FirebaseRealtimeController;
import net.manish.shopping.listeners.OnCallCompleteListener;
import net.manish.shopping.model.UserModel;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.Mylogger;
import net.manish.shopping.utils.SessionManager;

@SuppressLint("StaticFieldLeak")
public class FirebaseUtils {

    public static Context context;
    private static FirebaseUtils instance;

    public static SessionManager sessionManager;

    //For firebase
    public static FirebaseDatabase database;
    private static DatabaseReference myDbRef;
    private static String userId; //for authentication

    public FirebaseUtils() {
    }

    public static void init(Context c) {
        context = c;
        instance = new FirebaseUtils();
        //init firebase
        database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference();
        sessionManager = new SessionManager(c);
        userId = sessionManager.getUserId();
    }

    public static FirebaseUtils newInstance() {
        return instance;
    }

    public void saveFirebaseToken(final String token) {
        myDbRef.child(Constants.TABLE_USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                UserModel userModel = dataSnapshot.getValue(UserModel.class);

                if (userModel != null) {
                    //registered

                    //------start edit code-----------------
                    String newTokens;
                    if (userModel.getFireToken() != null) {
                        newTokens = generateTokenArrayBySplit(userModel.getFireToken(), token);
                    } else {
                        newTokens = token;
                    }
                    //-------end---------------------

                    userModel.setFireToken(newTokens);
                    //save on firebase
                    FirebaseRealtimeController.getInstance().addOrUpdateUser(userModel, userId).setOnCompleteListener(new OnCallCompleteListener() {
                        @Override
                        public void onComplete() {
                            Mylogger.getInstance().printLog("Firee", "token saved on firebsae completed. ");
                        }

                        @Override
                        public void onFailed() {

                        }
                    });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void removeFirebaseToken(final String token) {

        myDbRef.child(Constants.TABLE_USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                UserModel userModel = dataSnapshot.getValue(UserModel.class);

                if (userModel != null) {
                    //registered

                    //------start edit code-----------------
                    if (userModel.getFireToken() != null) {
                        String[] oldTokenArray = userModel.getFireToken().split(",");

                        StringBuilder newTokens = new StringBuilder();
                        for (String s : oldTokenArray) {

                            if (!s.equals(token)) {
                                if (newTokens.toString().equals("")) {
                                    newTokens = new StringBuilder(s);
                                } else {
                                    newTokens.append(",").append(s);
                                }
                            }
                        }

                        userModel.setFireToken(newTokens.toString());

                        //save on firebase
                        FirebaseRealtimeController.getInstance().addOrUpdateUser(userModel, userId).setOnCompleteListener(new OnCallCompleteListener() {
                            @Override
                            public void onComplete() {

                            }

                            @Override
                            public void onFailed() {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String generateTokenArrayBySplit(String oldTokens, String newToken) {

        String[] oldTokenArray = oldTokens.split(",");

        boolean isTokenAvailable = false;
        for (String s : oldTokenArray) {
            if (s.equals(newToken)) {
                isTokenAvailable = true;
                break;
            }
        }

        if (!isTokenAvailable) {
            if (oldTokens.equals("")) {
                oldTokens = newToken;
            } else {
                oldTokens = oldTokens + "," + newToken;
            }
        }

        return oldTokens;
    }

}
