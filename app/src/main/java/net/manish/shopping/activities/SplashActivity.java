package net.manish.shopping.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import net.manish.shopping.R;
import net.manish.shopping.model.BlockContactModel;
import net.manish.shopping.model.CommentModel;
import net.manish.shopping.model.ContactModel;
import net.manish.shopping.model.CountryCodeModel;
import net.manish.shopping.model.TodoModel;
import net.manish.shopping.model.UserModel;
import net.manish.shopping.network.ConnectionChecker;
import net.manish.shopping.network.NetworkConnectivity;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.service.InternetStateChangeBR;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.Mylogger;
import net.manish.shopping.utils.SessionManager;

import java.util.Objects;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private final String TAG = SplashActivity.this.getClass().getName();
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private ProgressBar progressBar;
    private TextView tvRetry;

    private SessionManager sessionManager;
    private NetworkConnectivity networkConnectivity;

    private DatabaseReference myDbRef;
    private String userId; //for authentication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);
        networkConnectivity = new NetworkConnectivity(this);

        Mylogger.getInstance().setLockLog(true);

        //init firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference();
        userId = sessionManager.getUserId();

        findViews();


        int contact = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (contact == PackageManager.PERMISSION_GRANTED) {


            //manageFirstTimeAfterInstall();
            checkLogedInOrNot();

        } else {
            //permission not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS/*, Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC*/}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }

    }

    private void checkLogedInOrNot() {
        if (sessionManager.isLoggedIn()) {
            if (ConnectionChecker.isInternetAvailable(this)) {
                syncDownloadTodosWithRealtimeDatabase(userId);

                if (RealmController.getInstance().getAllRegisteredContacts().size() == 0) {
                    new GetContacts().execute();
                }
            } else {
                new Handler().postDelayed(this::gotoMainActivity, 3000);
            }
        } else {
            new Handler().postDelayed(() -> {
                Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }, 2000);

        }
    }

    /*private void manageFirstTimeAfterInstall() {

        if (sessionManager.isFirstTime()) {
            addCountryCodeInLocalDb();
        }
    }*/

    private void findViews() {

        progressBar = findViewById(R.id.progressBar);
        tvRetry = findViewById(R.id.tv_retry);

        //setClick listeners
        tvRetry.setOnClickListener(v -> {
            if (networkConnectivity.isNetworkAvailable()) {
                syncDownloadTodosWithRealtimeDatabase(userId);
            }
        });

    }

    public void syncDownloadTodosWithRealtimeDatabase(final String uId) {

        //get all comments from firebase real time db by taskId, And store all comment in single table
        tvRetry.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        //This is for bunch child items in list listener
        myDbRef.child(Constants.TABLE_TODO_LIST).child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                RealmController.getInstance().clearNonChangedTodosDuringOfflineFromTable();//ade

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    TodoModel post = postSnapshot.getValue(TodoModel.class);
                    RealmController.getInstance().addTodoItem(post);
                }

                new SyncDownloadLocalDb().execute();
                gotoMainActivity();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value

                tvRetry.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    private void syncDownloadCommentsWithRealtimeDatabase() {


        //This is for bunch child items in list listener
        myDbRef.child(Constants.TABLE_COMMENTS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    String taskId = postSnapshot.getKey();

                    myDbRef.child(Constants.TABLE_COMMENTS).child(Objects.requireNonNull(taskId)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                                CommentModel post = postSnapshot.getValue(CommentModel.class);

                                RealmController.getInstance().addComment(post);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Failed to read value
                        }
                    });
                }

                syncBlockListWithRealTimeD();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value

                tvRetry.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    private void syncBlockListWithRealTimeD() {


        //This is for bunch child items in list listener
        myDbRef.child(Constants.TABLE_BLOCKED_USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    BlockContactModel post = postSnapshot.getValue(BlockContactModel.class);
                    RealmController.getInstance().addOrUpdateBlockUser(post);
                }

                syncLocalDbWithFirebase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
            }
        });
    }

    private void syncLocalDbWithFirebase() {

        InternetStateChangeBR mNetworkReceiver = new InternetStateChangeBR();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkReceiver, intentFilter);

        Intent broadcastIntent = new Intent(this, mNetworkReceiver.getClass());
        sendBroadcast(broadcastIntent);
    }

    private void gotoMainActivity() {

        tvRetry.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        new Handler().postDelayed(() -> {
            Intent i = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }, 2000);
    }

    private void addCountryCodeInLocalDb() {

        RealmController.getInstance().removeCountryCodes();

        RealmController.getInstance().addNewCountry(new CountryCodeModel(getNextId(), "Ind", Constants.COUNTRY_CODE_INDIA));
        RealmController.getInstance().addNewCountry(new CountryCodeModel(getNextId(), "US", Constants.COUNTRY_CODE_US));
        RealmController.getInstance().addNewCountry(new CountryCodeModel(getNextId(), "Australia", Constants.COUNTRY_CODE_AUSTRALIA));

    }

    public int getNextId() {
        try {
            Number number = RealmController.getInstance().getRealm().where(CountryCodeModel.class).max("id");
            if (number != null) {
                return number.intValue() + 1;
            } else {
                return 1;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    public int getNextIdForContact() {
        try {
            Number number = RealmController.getInstance().getRealm().where(ContactModel.class).max("id");
            if (number != null) {
                return number.intValue() + 1;
            } else {
                return 1;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    class SyncDownloadLocalDb extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            syncDownloadCommentsWithRealtimeDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            RealmController.getInstance().clearRegisteredContacts();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            getContactList();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

    @SuppressLint("Range")
    private void getContactList() {

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {

            while (cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);

                    while (Objects.requireNonNull(pCur).moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        phoneNo = phoneNo.replaceAll(" ", "");
                        if (phoneNo.length() > 10) {
                            Mylogger.getInstance().printLog(TAG, "greter 10 : " + phoneNo);
                            phoneNo = phoneNo.substring(phoneNo.length() - 10);
                        }

                        Mylogger.getInstance().printLog(TAG, "Name: " + name + "\tPhone : " + phoneNo);

                        if (phoneNo.length() == 10) {
                            isPhoneRegistered(phoneNo, name);
                        }

                    }
                    pCur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }

    }

    public void isPhoneRegistered(final String phone, final String name) {

        myDbRef.child(Constants.TABLE_USERS).orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                UserModel userModel = new UserModel();
                for (DataSnapshot user : dataSnapshot.getChildren()) {
                    userModel = user.getValue(UserModel.class);
                }

                if (userModel != null) {
                    if (userModel.getUserId() != null) {
                        //registered

                        ContactModel contactModel = new ContactModel();
                        contactModel.setId(getNextIdForContact());
                        contactModel.setName(name);
                        contactModel.setNumber(phone);
                        contactModel.setUserId(userModel.getUserId());
                        contactModel.setSelected(false);

                        if (!contactModel.getNumber().equals(sessionManager.getPhoneNumber())) {//can't add own number
                            RealmController.getInstance().addRegisteredContact(contactModel);
                        }

                    } else {
                        //not registered
                        Mylogger.getInstance().printLog(TAG, "is Not Registered : " + name);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            // permission was granted, yay! do the
            //manageFirstTimeAfterInstall();
            checkLogedInOrNot();
        }
    }

}
