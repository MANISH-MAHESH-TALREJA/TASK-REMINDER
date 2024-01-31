package net.manish.shopping.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.manish.shopping.R;
import net.manish.shopping.model.CountryCodeModel;
import net.manish.shopping.model.UserModel;
import net.manish.shopping.network.NetworkConnectivity;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.utils.CommonUtils;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.DateAndTimePicker;
import net.manish.shopping.utils.Mylogger;
import net.manish.shopping.utils.SessionManager;

public class LoginActivity extends AppCompatActivity
{

    private final String TAG = LoginActivity.this.getClass().getName();
    private EditText etUsername, etPassword;
    private ProgressDialog progressDialog;

    private SessionManager sessionManager;
    private NetworkConnectivity networkConnectivity;

    private DatabaseReference myDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        //firebase auth
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference();

        sessionManager = new SessionManager(this);
        networkConnectivity = new NetworkConnectivity(this);

        manageFirstTimeAfterInstall();

        findViews();

        DateAndTimePicker dateAndTimePicker = new DateAndTimePicker(this);
        Mylogger.getInstance().printLog(TAG, "todo size : " + RealmController.getInstance().getAllActiveTodos(dateAndTimePicker.getCurrentDate()));

    }

    private void findViews()
    {

        progressDialog = new ProgressDialog(this);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        Button btnSignup = findViewById(R.id.login_button_sign_up);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);

        Button buttonLogin = findViewById(R.id.login_button_login);

        //Setup click listeners
        buttonLogin.setOnClickListener(v ->
        {

            CommonUtils.hideKeyboard(LoginActivity.this);

            if (networkConnectivity.isNetworkAvailable())
            {
                if (isValid())
                {
                    checkLogin();
                }
                else
                {
                    if (progressDialog != null)
                    {
                        progressDialog.dismiss();
                    }
                }
            }
        });

        btnSignup.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));

        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));

    }

    private void checkLogin()
    {
        progressDialog.setMessage(getResources().getString(R.string.logging));
        progressDialog.show();
        progressDialog.setCancelable(false);

        myDbRef.child(Constants.TABLE_USERS).orderByChild("phone").equalTo(etUsername.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                UserModel userModel = new UserModel();
                for (DataSnapshot user : dataSnapshot.getChildren())
                {
                    userModel = user.getValue(UserModel.class);
                }

                if (userModel != null)
                {
                    if (userModel.getUserId() == null)
                    {
                        //not registered
                        etUsername.setError(getString(R.string.account_not_found));
                        progressDialog.dismiss();
                    }
                    else
                    {
                        //registered
                        verifyPassword(userModel);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

    }

    private void verifyPassword(UserModel userModel)
    {

        progressDialog.dismiss();

        if (userModel.getPassword().equals(etPassword.getText().toString()))
        {
            Mylogger.getInstance().printLog(TAG, "getPhone:-" + userModel.getPhone());
            sessionManager.createLogin(userModel.getUserId(), userModel.getName(), userModel.getPhone());
            startActivity(new Intent(LoginActivity.this, MainActivity.class));

            finish();
        }
        else
        {
            etPassword.setError(getString(R.string.invalid_password));
        }
    }

    public boolean isValid()
    {

        if (etUsername.getText().toString().isEmpty())
        {
            etUsername.setError(getResources().getString(R.string.invalid_username));
            return false;
        }
        if (etPassword.getText().toString().isEmpty())
        {
            etPassword.setError(getResources().getString(R.string.invalid_password));
            return false;
        }
        return true;
    }

    private void manageFirstTimeAfterInstall()
    {

        if (sessionManager.isFirstTime())
        {

            addCountryCodeInLocalDb();

        }
    }

    private void addCountryCodeInLocalDb()
    {

        RealmController.getInstance().removeCountryCodes();

        RealmController.getInstance().addNewCountry(new CountryCodeModel(getNextId(), "Ind", Constants.COUNTRY_CODE_INDIA));
        RealmController.getInstance().addNewCountry(new CountryCodeModel(getNextId(), "US", Constants.COUNTRY_CODE_US));
        RealmController.getInstance().addNewCountry(new CountryCodeModel(getNextId(), "Australia", Constants.COUNTRY_CODE_AUSTRALIA));

    }

    public int getNextId()
    {
        try
        {
            Number number = RealmController.getInstance().getRealm().where(CountryCodeModel.class).max("id");
            if (number != null)
            {
                return number.intValue() + 1;
            }
            else
            {
                return 1;
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return 0;
        }
    }

}
