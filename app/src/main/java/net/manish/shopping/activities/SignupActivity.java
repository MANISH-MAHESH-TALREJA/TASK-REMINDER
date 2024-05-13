package net.manish.shopping.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.manish.shopping.R;
import net.manish.shopping.firebaseutils.FirebaseRealtimeController;
import net.manish.shopping.listeners.OnCallCompleteListener;
import net.manish.shopping.model.UserModel;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.utils.CommonUtils;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.Mylogger;
import net.manish.shopping.utils.SessionManager;
import net.manish.shopping.utils.Validator;

import com.hbb20.CountryCodePicker;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import at.markushi.ui.CircleButton;

public class SignupActivity extends AppCompatActivity
{

    private final String TAG = SignupActivity.this.getClass().getName();
    private EditText etPhone, etUsername, etPassword, etConfirmPassword, etOTP;

    CountryCodePicker ccp;
    private Button buttonSignup, btnVerify, btnResend;
    private TextView tvLogin, tvTitleOTPView, tvMsgOTPView;
    private CircleButton btnDone;
    private LinearLayout enterOPTView, resendOtpView;

    private RelativeLayout rltvRootOTPView;
    private ScrollView scrollRootLoginView;
    private AVLoadingIndicatorView avLoadingIndicatorView;

    private Animation slideInFromRight;

    //firebase auth
    private FirebaseAuth mAuth;
    private DatabaseReference myDbRef;

    //for verify otp
    private String phoneNumber;
    private String verificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_signup);

        slideInFromRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_right);
        mAuth = FirebaseAuth.getInstance();
        //For firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference();

        initViews();
        setOtpCallBack();

    }

    private void initViews()
    {

        getViewReferences();
        setupClickListeners();
    }

    private void setupClickListeners()
    {
        //Setup click listeners
        buttonSignup.setOnClickListener(v ->
        {
            CommonUtils.hideKeyboard(SignupActivity.this);
            if (isValid())
            {
                isPhoneRegistered();
            }
        });

        tvLogin.setOnClickListener(v -> onBackPressed());

        btnDone.setOnClickListener(v -> onBackPressed());

        btnVerify.setOnClickListener(v ->
        {

            String otp = etOTP.getText().toString();
            if (otp.length() == 6)
            {
                verifyEnteredOTP(otp);
            }
            else
            {
                etOTP.setError(getString(R.string.invalid_otp));
                tvTitleOTPView.setText(getString(R.string.verify));
            }
        });

        btnResend.setOnClickListener(v ->
        {
            CommonUtils.hideKeyboard(SignupActivity.this);
            enterOPTView.setVisibility(View.VISIBLE);
            resendOtpView.setVisibility(View.GONE);
            avLoadingIndicatorView.setVisibility(View.VISIBLE);
            sendOTP();
        });
    }

    private void getViewReferences()
    {
        etOTP = findViewById(R.id.et_otp);
        etPhone = findViewById(R.id.et_phone);
        etUsername = findViewById(R.id.et_name);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        tvLogin = findViewById(R.id.tv_login);
        tvTitleOTPView = findViewById(R.id.tv_title);
        tvMsgOTPView = findViewById(R.id.tv_msg);
        btnDone = findViewById(R.id.btn_done);
        btnVerify = findViewById(R.id.btn_verify);
        btnResend = findViewById(R.id.btn_resend);
        rltvRootOTPView = findViewById(R.id.root_verify_otp_view);
        scrollRootLoginView = findViewById(R.id.root_login_view);
        avLoadingIndicatorView = findViewById(R.id.progressBar);
        buttonSignup = findViewById(R.id.btn_signup);
        ccp = findViewById(R.id.ccp);
        enterOPTView = findViewById(R.id.lnr_enter_otp_and_verify_view);
        resendOtpView = findViewById(R.id.lnr_resend_otp_view);
    }

    private void verifyEnteredOTP(final String otp)
    {
        if (verificationCode != null && !verificationCode.isEmpty())
        {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, otp);
            signInWithPhone(credential);
        }
    }

    private void sendOTP()
    {

        tvMsgOTPView.setText(getString(R.string.sending_otp));
        String countryCode = ccp.getSelectedCountryCodeWithPlus();

        phoneNumber = countryCode + etPhone.getText().toString();
        System.out.println("PHONE NUMBER : " + phoneNumber);
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber) // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallback)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void makeVisibleOTPView()
    {

        slideInFromRight.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                scrollRootLoginView.setVisibility(View.GONE);
                sendOTP();
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });

        rltvRootOTPView.setVisibility(View.VISIBLE);
        rltvRootOTPView.startAnimation(slideInFromRight);

    }

    public boolean isValid()
    {

        String phone = etPhone.getText().toString();

        if (!Validator.isValidPhone(phone))
        {
            etPhone.setError(getResources().getString(R.string.invalid_phone));
            return false;
        }
        if (etUsername.getText().toString().isEmpty())
        {
            etUsername.setError(getResources().getString(R.string.invalid_username));
            return false;
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

    public void isPhoneRegistered()
    {
        myDbRef.child(Constants.TABLE_USERS).orderByChild("phone").equalTo(etPhone.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                if (userModel != null)
                {
                    //registered
                    etPhone.setError(getString(R.string.phone_registered));
                }
                else
                {
                    //not registered
                    makeVisibleOTPView();//go for otp verification
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.d(TAG, "onCanceled() : " + databaseError.toString());
            }
        });
    }

    private void setOtpCallBack()
    {

        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential)
            {

                etOTP.setText(phoneAuthCredential.getSmsCode());
                signInWithPhone(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e)
            {
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken)
            {
                super.onCodeSent(s, forceResendingToken);

                verificationCode = s;

                tvTitleOTPView.setText(getString(R.string.otp_sent));
                tvMsgOTPView.setText(getString(R.string.otp_sent_msg));
                tvTitleOTPView.setVisibility(View.VISIBLE);
                avLoadingIndicatorView.setVisibility(View.GONE);

            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s)
            {
                super.onCodeAutoRetrievalTimeOut(s);

                enterOPTView.setVisibility(View.GONE);
                resendOtpView.setVisibility(View.VISIBLE);
                avLoadingIndicatorView.setVisibility(View.GONE);

            }
        };
    }

    private void signInWithPhone(PhoneAuthCredential credential)
    {

        tvTitleOTPView.setVisibility(View.GONE);
        tvMsgOTPView.setText(getString(R.string.verifing_otp));
        avLoadingIndicatorView.setVisibility(View.VISIBLE);//SzXIWF7B09hcYUqsGbQVQB6gSc22

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task ->
                {
                    if (task.isSuccessful())
                    {

                        avLoadingIndicatorView.setVisibility(View.GONE);
                        etOTP.setVisibility(View.GONE);
                        btnVerify.setVisibility(View.GONE);
                        btnDone.setVisibility(View.VISIBLE);
                        tvTitleOTPView.setVisibility(View.VISIBLE);
                        tvTitleOTPView.setText(getString(R.string.done));
                        tvMsgOTPView.setText(getString(R.string.phone_verified));

                        userId = Objects.requireNonNull(task.getResult().getUser()).getUid();
                        createNewUser();

                    }
                    else
                    {
                        Toast.makeText(SignupActivity.this, "Incorrect OTP", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void createNewUser()
    {

        UserModel userModel = new UserModel();
        userModel.setId(getNextId());
        userModel.setUserId(userId);
        userModel.setName(etUsername.getText().toString());
        userModel.setPassword(etPassword.getText().toString());
        userModel.setPhone(etPhone.getText().toString());

        userModel.setFireToken("");

        Mylogger.getInstance().printLog(TAG, "createNewUser() : user Id :  " + userId);
        //save on firebase
        FirebaseRealtimeController.getInstance().addOrUpdateUser(userModel, etPhone.getText().toString()).setOnCompleteListener(new OnCallCompleteListener()
        {
            @Override
            public void onComplete()
            {

                SessionManager sessionManager = new SessionManager(SignupActivity.this);
                sessionManager.createLogin(userId, etUsername.getText().toString(), phoneNumber);
                gotoMainScreen();
            }

            @Override
            public void onFailed()
            {
                Mylogger.getInstance().printLog(TAG, "createNewUser() Failed");
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

    private void gotoMainScreen()
    {

        new Handler().postDelayed(() ->
        {
            startActivity(new Intent(SignupActivity.this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        }, 1000);


    }
}
