package net.manish.shopping.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;

import net.manish.shopping.R;
import net.manish.shopping.listeners.OnTimeSelectListener;
import net.manish.shopping.utils.Constants;
import net.manish.shopping.utils.SessionManager;


@SuppressLint("ValidFragment")
public class SnoozeDialogFragmentBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {


    private View view;
    private RadioButton rbtn5Min, rbtn10Min, rbtn15Min, rbtn30Min,rbtn1Hour;

    private SessionManager sessionManager;
    private String newSnooze = "5";

    // private OnTimeSelectListener onTimeSelectListener;

    /*public void setOnTimeSelectListener(OnTimeSelectListener onTimeSelectListener) {
        this.onTimeSelectListener = onTimeSelectListener;
    }
*/
    @SuppressLint("ValidFragment")
    public SnoozeDialogFragmentBottomSheet(Context context) {
        this.sessionManager = new SessionManager(context);
    }

    public SnoozeDialogFragmentBottomSheet() {

    }


    private void findViews() {

        //below rbtn for time
        rbtn5Min = view.findViewById(R.id.radio_5min);
        rbtn10Min = view.findViewById(R.id.radio_10min);
        rbtn15Min = view.findViewById(R.id.radio_15min);
        rbtn30Min = view.findViewById(R.id.radio_30min);
        rbtn1Hour = view.findViewById(R.id.radio_1hour);

        ImageView buttonDismiss = view.findViewById(R.id.iv_close);
        Button buttonAdd = view.findViewById(R.id.btn_save);

        makeTimeSelected();

        //init click listeners
        rbtn5Min.setOnClickListener(this);
        rbtn10Min.setOnClickListener(this);
        rbtn15Min.setOnClickListener(this);
        rbtn30Min.setOnClickListener(this);
        rbtn1Hour.setOnClickListener(this);
        buttonDismiss.setOnClickListener(this);
        buttonAdd.setOnClickListener(this);


    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_set_snooze, null);

        findViews();

        return view;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id == R.id.btn_save) {
            sessionManager.setSnoozeTime(newSnooze);
            // onTimeSelectListener.onTimeSelected(sessionManager.getSnoozeTime());
            dismiss();
        } else if (id == R.id.iv_close) {
            dismiss();
        } else if (id == R.id.radio_5min) {
            clearRadioChecked();
            rbtn5Min.setChecked(true);
            newSnooze = Constants.SNOOZE_5_MIN;
        } else if (id == R.id.radio_10min) {
            clearRadioChecked();
            rbtn10Min.setChecked(true);
            newSnooze = Constants.SNOOZE_10_MIN;
        } else if (id == R.id.radio_15min) {
            clearRadioChecked();
            rbtn15Min.setChecked(true);
            newSnooze = Constants.SNOOZE_15_MIN;
        } else if (id == R.id.radio_30min) {
            clearRadioChecked();
            rbtn30Min.setChecked(true);
            newSnooze = Constants.SNOOZE_30_MIN;
        } else if (id == R.id.radio_1hour) {
            clearRadioChecked();
            rbtn1Hour.setChecked(true);
            newSnooze = Constants.SNOOZE_1_HOUR;
        }
    }

    private void makeTimeSelected() {

        String time = sessionManager.getSnoozeTime();

        switch (time) {
            case Constants.SNOOZE_5_MIN:
                rbtn5Min.setChecked(true);
                break;
            case Constants.SNOOZE_10_MIN:
                rbtn10Min.setChecked(true);
                break;
            case Constants.SNOOZE_15_MIN:
                rbtn15Min.setChecked(true);
                break;
            case Constants.SNOOZE_30_MIN:
                rbtn30Min.setChecked(true);
                break;
            case Constants.SNOOZE_1_HOUR:
                rbtn1Hour.setChecked(true);
                break;
        }

    }

    public void clearRadioChecked() {
        rbtn5Min.setChecked(false);
        rbtn10Min.setChecked(false);
        rbtn15Min.setChecked(false);
        rbtn30Min.setChecked(false);
        rbtn1Hour.setChecked(false);
    }

}
