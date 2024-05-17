package net.manish.shopping.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import net.manish.shopping.R;
import net.manish.shopping.fragments.SnoozeDialogFragmentBottomSheet;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.SessionManager;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvSnoozeDigit;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sessionManager = new SessionManager(SettingsActivity.this);
        findViews();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void findViews() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.action_settings));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView tvChangePassword = findViewById(R.id.tv_change_password);
        TextView tvChangeRingtone = findViewById(R.id.tv_change_ringtone);
        TextView tvChangeSnoozeTime = findViewById(R.id.tv_change_snooze_time);
        tvSnoozeDigit = findViewById(R.id.tv_snooze_digit);

        //set values to view
        setSnoozeValue(sessionManager.getSnoozeTime());

        //set click listeners
        tvChangePassword.setOnClickListener(this);
        tvChangeRingtone.setOnClickListener(this);
        tvChangeSnoozeTime.setOnClickListener(this);

    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id == R.id.tv_change_password) {
            gotoResetPassword(sessionManager.getUserId());
        } else if (id == R.id.tv_change_ringtone) {
        } else if (id == R.id.tv_change_snooze_time) {
            SnoozeDialogFragmentBottomSheet snoozeDialog = new SnoozeDialogFragmentBottomSheet(SettingsActivity.this);
            snoozeDialog.show(getSupportFragmentManager(), "");
            //snoozeDialog.setOnTimeSelectListener(this::setSnoozeValue);
        }
    }

    @SuppressLint("SetTextI18n")
    private void setSnoozeValue(String snoozeTime) {
        //set values to view
        if (snoozeTime.equals(Constants.SNOOZE_1_HOUR)) {
            tvSnoozeDigit.setText("1 Hour");
        } else {
            tvSnoozeDigit.setText(sessionManager.getSnoozeTime() + " Min");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999 && resultCode == RESULT_OK) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            sessionManager.setRingtoneUri(Objects.requireNonNull(uri).toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void gotoResetPassword(String userId){

        Intent i = new Intent(this,ResetPasswordActivity.class);
        i.putExtra(Constants.KEY_USERID,userId);
        i.putExtra(Constants.KEY_PHONE_NUMBER, sessionManager.getPhoneNumber());
        i.putExtra(Constants.KEY_COME_FROM,Constants.SCREEN_SETTINGS);
        startActivity(i);

    }
}
