package net.manish.shopping.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import net.manish.shopping.R;
import net.manish.shopping.listeners.OnFilterClickedListener;
import net.manish.shopping.utils.Constants;

@SuppressLint("ValidFragment")
public class FilterDialogFragmentBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {

    private TextView  tvClearFilter;

    private Button buttonAdd;
    private View view;
    private String time = "";
    private static OnFilterClickedListener listener;
    private RadioButton rbtnToday, rbtnWeek, rbtnMonth, rbtnYear;

    public static void setOnFilterClickedListener(OnFilterClickedListener onItemClickedListener) {
        listener = onItemClickedListener;
    }

    @SuppressLint("ValidFragment")
    public FilterDialogFragmentBottomSheet(String selectedTime) {
        this.time = selectedTime;
    }

    public FilterDialogFragmentBottomSheet() {
    }


    private void findViews() {

        //below rbtn for time
        rbtnToday = view.findViewById(R.id.radio_tomorrow);
        rbtnWeek = view.findViewById(R.id.radio_next_week);
        rbtnMonth = view.findViewById(R.id.radio_next_month);
        rbtnYear = view.findViewById(R.id.radio_next_year);

        buttonAdd = view.findViewById(R.id.btn_add);


        tvClearFilter = view.findViewById(R.id.tv_clear_filter);

        makeTimeSelected();

        //init click listeners
        rbtnToday.setOnClickListener(this);
        rbtnWeek.setOnClickListener(this);
        rbtnMonth.setOnClickListener(this);
        rbtnYear.setOnClickListener(this);


    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.dialog_filter, null);

        findViews();

        buttonAdd.setOnClickListener(v -> {
            if (time.equals("")) {
                listener.onFilterCleared();
            } else {
                listener.onItemClicked(time);
            }
            dismiss();
        });

        tvClearFilter.setOnClickListener(v -> {
            dismiss();
            listener.onFilterCleared();
        });

        return view;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id == R.id.radio_tomorrow) {
            clearRadioCheckedOfTime();

            rbtnToday.setChecked(true);
            time = Constants.FILTER_TOMORROW;
        } else if (id == R.id.radio_next_week) {
            clearRadioCheckedOfTime();

            rbtnWeek.setChecked(true);
            time = Constants.FILTER_NEXT_WEEK;
        } else if (id == R.id.radio_next_month) {
            clearRadioCheckedOfTime();

            rbtnMonth.setChecked(true);
            time = Constants.FILTER_NEXT_MONTH;
        } else if (id == R.id.radio_next_year) {
            clearRadioCheckedOfTime();

            rbtnYear.setChecked(true);
            time = Constants.FILTER_NEXT_YEAR;
        }
    }

    private void makeTimeSelected() {

        switch (time) {
            case Constants.FILTER_TOMORROW:
                rbtnToday.setChecked(true);
                break;
            case Constants.FILTER_NEXT_WEEK:
                rbtnWeek.setChecked(true);
                break;
            case Constants.FILTER_NEXT_MONTH:
                rbtnMonth.setChecked(true);
                break;
            case Constants.FILTER_NEXT_YEAR:
                rbtnYear.setChecked(true);
                break;
        }

    }

    public void clearRadioCheckedOfTime() {
        rbtnToday.setChecked(false);
        rbtnMonth.setChecked(false);
        rbtnWeek.setChecked(false);
        rbtnYear.setChecked(false);
    }

}
