package net.manish.shopping.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;

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
import net.manish.shopping.model.CountryCodeModel;
import net.manish.shopping.model.UserModel;
import net.manish.shopping.realm.RealmController;
import net.manish.shopping.utils.CommonUtils;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.Mylogger;
import net.manish.shopping.utils.Validator;

import com.hbb20.CountryCodePicker;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import at.markushi.ui.CircleButton;

public class ForgotPasswordActivity extends AppCompatActivity {

    private final String TAG = ForgotPasswordActivity.this.getClass().getName();
    private EditText etPhone, etOTP;
    private Button buttonGenerateOTP;
    private Toolbar toolbar;
    private LinearLayout lnrLoginView;

    private Button btnVerify;

    CountryCodePicker ccp;
    private Button btnResend;
    private TextView tvLogin, tvTitleOTPView, tvMsgOTPView;
    private CircleButton btnDone;

    private final List<CountryCodeModel> countryList = new ArrayList<>();

    private RelativeLayout rltvRootOTPView;
    private AVLoadingIndicatorView avLoadingIndicatorView;

    private Animation slideInFromRight;

    //firebase auth
    private FirebaseAuth mAuth;
    private DatabaseReference myDbRef;

    private String verificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forgot_password);

        slideInFromRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_right);
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference();

        initViews();
        setupToolbar();

        getViewReferences();
        setOtpCallBack();
        setupClickListeners();

    }

    private void initViews() {

        getViewReferences();
        setupClickListeners();

    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.forgot_password));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupClickListeners() {
        //Setup click listeners
        buttonGenerateOTP.setOnClickListener(v -> {
            CommonUtils.hideKeyboard(ForgotPasswordActivity.this);
            if (isValid()) {
                isPhoneRegistered();
            }
        });

        tvLogin.setOnClickListener(v -> onBackPressed());

        btnVerify.setOnClickListener(v -> {

            String otp = etOTP.getText().toString();
            if (otp.length() == 6) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, otp);
                signInWithPhone(credential);
            } else {
                etOTP.setError(getString(R.string.invalid_otp));
                tvTitleOTPView.setText(getString(R.string.verify));
            }
        });

        btnResend.setOnClickListener(v -> {
            if (isValid()) {
                isPhoneRegistered();
            }
        });
    }

    public void isPhoneRegistered() {

        myDbRef.child(Constants.TABLE_USERS).orderByChild("phone").equalTo(etPhone.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                UserModel userModel = dataSnapshot.getValue(UserModel.class);
                if (userModel != null) {
                    Mylogger.getInstance().printLog(TAG, "isRegistered : " + userModel.getName());
                    //registered
                    makeVisibleOTPView();//go for otp verification

                } else {
                    //not registered
                    etPhone.setError(getString(R.string.account_not_found));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public boolean isValid() {

        String phone = etPhone.getText().toString();

        if (!Validator.isValidPhone(phone)) {
            etPhone.setError(getResources().getString(R.string.invalid_phone));
            return false;
        }

        return true;
    }

    private void getViewReferences() {

        toolbar = findViewById(R.id.toolbar);
        etPhone = findViewById(R.id.et_phone);
        etOTP = findViewById(R.id.et_otp);
        buttonGenerateOTP = findViewById(R.id.btn_generate_otp);
        lnrLoginView = findViewById(R.id.lnr_login_view);
        ccp = findViewById(R.id.ccp);
        tvLogin = findViewById(R.id.tv_login);
        tvTitleOTPView = findViewById(R.id.tv_title);
        tvMsgOTPView = findViewById(R.id.tv_msg);
        btnDone = findViewById(R.id.btn_done);
        btnVerify = findViewById(R.id.btn_verify);
        btnResend = findViewById(R.id.btn_resend);
        rltvRootOTPView = findViewById(R.id.root_verify_otp_view);
        avLoadingIndicatorView = findViewById(R.id.progressBar);

    }

    private void sendOTP() {

        tvMsgOTPView.setText(getString(R.string.sending_otp));
        //for verify otp
        String countryCode = ccp.getSelectedCountryCode();
        String phoneNumber = countryCode + etPhone.getText().toString();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber) // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallback)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void makeVisibleOTPView() {

        slideInFromRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                lnrLoginView.setVisibility(View.GONE);
                sendOTP();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        rltvRootOTPView.setVisibility(View.VISIBLE);
        rltvRootOTPView.startAnimation(slideInFromRight);

    }

    private void setOtpCallBack() {

        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                //goto reset password screen with user id
                signInWithPhone(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                tvTitleOTPView.setText(getString(R.string.failed));
                tvMsgOTPView.setText(getString(R.string.sending_otp_failed));
                tvTitleOTPView.setVisibility(View.VISIBLE);
                avLoadingIndicatorView.setVisibility(View.GONE);
                etOTP.setVisibility(View.GONE);
                btnVerify.setVisibility(View.GONE);
                btnResend.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                verificationCode = s;
                tvTitleOTPView.setText(getString(R.string.otp_sent));
                tvMsgOTPView.setText(getString(R.string.otp_sent_msg));
                tvTitleOTPView.setVisibility(View.VISIBLE);
                avLoadingIndicatorView.setVisibility(View.GONE);
            }
        };
    }

    private void signInWithPhone(PhoneAuthCredential credential) {

        tvTitleOTPView.setVisibility(View.GONE);
        tvMsgOTPView.setText(getString(R.string.verifing_otp));
        avLoadingIndicatorView.setVisibility(View.VISIBLE);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        avLoadingIndicatorView.setVisibility(View.GONE);
                        etPhone.setVisibility(View.GONE);
                        btnVerify.setVisibility(View.GONE);
                        etOTP.setVisibility(View.GONE);
                        btnDone.setVisibility(View.VISIBLE);
                        tvTitleOTPView.setVisibility(View.VISIBLE);
                        tvTitleOTPView.setText(getString(R.string.done));
                        tvMsgOTPView.setText(getString(R.string.phone_verified));

                        //goto reset password screen
                        gotoResetPassword(Objects.requireNonNull(task.getResult().getUser()).getUid());

                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Incorrect OTP", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void gotoResetPassword(String userId) {

        Intent i = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
        i.putExtra(Constants.KEY_USERID, userId);
        i.putExtra(Constants.KEY_PHONE_NUMBER, etPhone.getText().toString());
        i.putExtra(Constants.KEY_COME_FROM, Constants.SCREEN_FORGOT_PASSWORD);
        startActivity(i);
        finish();

    }

}
