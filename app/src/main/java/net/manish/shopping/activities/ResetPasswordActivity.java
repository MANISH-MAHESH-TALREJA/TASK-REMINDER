package net.manish.shopping.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.manish.shopping.R;
import net.manish.shopping.firebaseutils.FirebaseRealtimeController;
import net.manish.shopping.listeners.OnCallCompleteListener;
import net.manish.shopping.model.UserModel;
import net.manish.shopping.network.ConnectionChecker;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.Validator;

import java.util.Objects;

public class ResetPasswordActivity extends AppCompatActivity
{

    private Toolbar toolbar;
    private EditText etOldPassword, etPassword, etConfirmPassword;
    private View viewOldLine;
    private ProgressDialog progressDialog;

    private String userId;
    private String comeFrom;
    // private String phoneNumber;


    private DatabaseReference myDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reset_password);

        //For firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference();

        getIntentValues();

        findViews();
        setupToolbar();
        manageScreen();

    }

    private void manageScreen()
    {
        if (comeFrom.equals(Constants.SCREEN_FORGOT_PASSWORD))
        {
            etOldPassword.setVisibility(View.GONE);
            viewOldLine.setVisibility(View.GONE);
            findViewById(R.id.et_old_password_eyes).setVisibility(View.GONE);
        }
    }

    private void getIntentValues()
    {

        userId = getIntent().getStringExtra(Constants.KEY_USERID);
        comeFrom = getIntent().getStringExtra(Constants.KEY_COME_FROM);
        // phoneNumber = getIntent().getStringExtra(Constants.KEY_PHONE_NUMBER);
    }

    private void findViews()
    {

        progressDialog = new ProgressDialog(this);

        toolbar = findViewById(R.id.toolbar);
        viewOldLine = findViewById(R.id.view_old_pass);
        etOldPassword = findViewById(R.id.et_old_password);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        Button btnReset = findViewById(R.id.btn_reset);
        btnReset.setOnClickListener(v ->
        {
            if (ConnectionChecker.isInternetAvailable(ResetPasswordActivity.this))
            {
                if (isValid())
                {
                    saveNewPassword();
                }
            } else
            {
                Toast.makeText(ResetPasswordActivity.this, "Internet not available !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupToolbar()
    {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.reset_password));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home)
        {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }


    public boolean isValid()
    {

        String oldPass = etOldPassword.getText().toString();

        if (!comeFrom.equals(Constants.SCREEN_FORGOT_PASSWORD))
        {
            if (oldPass.isEmpty())
            {
                etOldPassword.setError(getResources().getString(R.string.password_required));
                return false;
            }
        }
        if (etPassword.getText().toString().isEmpty())
        {
            etPassword.setError(getResources().getString(R.string.password_required));
            return false;
        }
        if (!Validator.isValidPassword(etPassword.getText().toString()))
        {
            etPassword.setError(getResources().getString(R.string.provide_valid_password));
            return false;
        }

        if (!etPassword.getText().toString().equals(etConfirmPassword.getText().toString()))
        {
            etConfirmPassword.setError(getResources().getString(R.string.password_not_matched));
            return false;
        }

        return true;
    }

    public void saveNewPassword()
    {

        progressDialog.setMessage(getString(R.string.pls_wait));
        progressDialog.show();

        myDbRef.child(Constants.TABLE_USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                UserModel userModel = dataSnapshot.getValue(UserModel.class);

                if (userModel != null)
                {
                    //registered
                    if (comeFrom.equals(Constants.SCREEN_FORGOT_PASSWORD))
                    {
                        createNewUser(userModel);
                    }

                    else if (comeFrom.equals(Constants.SCREEN_SETTINGS))
                    {
                        if (etOldPassword.getText().toString().equals(userModel.getPassword()))
                        {
                            createNewUser(userModel);
                        }
                        else
                        {
                            etOldPassword.setError(getString(R.string.wrong_old_password));
                            progressDialog.dismiss();
                        }
                    }

                } else
                {
                    //not registered
                    progressDialog.dismiss();
                    Toast.makeText(ResetPasswordActivity.this, getString(R.string.account_not_found), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });


    }

    private void createNewUser(UserModel user)
    {

        UserModel userModel = new UserModel();
        userModel.setId(getNextId());
        userModel.setUserId(userId);
        userModel.setName(user.getName());
        userModel.setPassword(etPassword.getText().toString());
        userModel.setPhone(user.getPhone());
        userModel.setFireToken(user.getFireToken());

        //save on firebase
        FirebaseRealtimeController.getInstance().addOrUpdateUser(userModel, userId).setOnCompleteListener(new OnCallCompleteListener()
        {
            @Override
            public void onComplete()
            {

                progressDialog.setMessage(getString(R.string.transferring_to_dashboard));
                Toast.makeText(ResetPasswordActivity.this, getString(R.string.password_reset_success), Toast.LENGTH_SHORT).show();
                if (comeFrom.equals(Constants.SCREEN_FORGOT_PASSWORD))
                {
                    gotoLoginScreen();
                } else
                {
                    progressDialog.dismiss();
                    onBackPressed();
                }
            }

            @Override
            public void onFailed()
            {
                progressDialog.dismiss();
                Toast.makeText(ResetPasswordActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public int getNextId()
    {
        try
        {
            Number number = RealmController.getInstance().getRealm().where(UserModel.class).max("id");
            if (number != null)
            {
                return number.intValue() + 1;
            } else
            {
                return 1;
            }
        } catch (ArrayIndexOutOfBoundsException e)
        {
            return 0;
        }
    }

    private void gotoLoginScreen()
    {

        new Handler().postDelayed(() ->
        {
            if (progressDialog != null)
            {
                progressDialog.dismiss();
            }

            startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        }, 1000);
    }
}
