package net.manish.shopping.utils;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;

import net.manish.shopping.listeners.OnDateSelectListener;
import net.manish.shopping.listeners.OnTimeSelectListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


public class DateAndTimePicker {

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    final private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy", Locale.US);
    final private SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a", Locale.US);
    final private SimpleDateFormat timeSecondFormatter = new SimpleDateFormat("hh:mm:ss a", Locale.US);
    final private SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.US);
    final private SimpleDateFormat dateTimeSecondFormatter = new SimpleDateFormat("dd MMM yyyy hh:mm:ss a", Locale.US);
    private static OnDateSelectListener onDateSelectListener;
    private static OnTimeSelectListener onTimeSelectListener;

    public DateAndTimePicker(Context context) {
        DateAndTimePicker.context = context;
    }

    public void setOnDateSelectListener(OnDateSelectListener onDateSelectListener) {
        DateAndTimePicker.onDateSelectListener = onDateSelectListener;
    }

    public void setOnTimeSelectListener(OnTimeSelectListener onTimeSelectListener) {
        DateAndTimePicker.onTimeSelectListener = onTimeSelectListener;
    }

    public void datePicker() {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);
            System.out.print(dateFormatter.format(newDate.getTime()));

            onDateSelectListener.onDateSelected(dateFormatter.format(newDate.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        datePickerDialog.show();
    }

    public String getCurrentDate() {
        Date today = new Date();

        return dateFormatter.format(today);

    }

    public String getCurrentTime() {
        Date today = new Date();

        return timeFormatter.format(today);

    }

    public String extractTimeFromDateTime(String dateTime) {
        Date day = convertStringToDateTime(dateTime);

        return timeFormatter.format(day);

    }

    public String extractDateFromDateTime(String dateTime) {
        Date day = convertStringToDateTime(dateTime);

        return dateFormatter.format(day);

    }

    public String extractTimeFromDateTimeSecond(String dateTime) {
        Date day = convertStringToDateTimeSecond(dateTime);

        return timeSecondFormatter.format(day);

    }


    public String extactDateFromDateTimeSecond(String dateTime) {
        Date day = convertStringToDateTimeSecond(dateTime);
        return dateFormatter.format(day);

    }

    public String getFormatedDate(Date date) {

        return dateTimeFormatter.format(date);

    }

    public String getCurrentDateTime() {
        Date today = new Date();

        return dateTimeFormatter.format(today);

    }

    public String getCurrentDateTimeWithSecond() {
        Date today = new Date();

        return dateTimeSecondFormatter.format(today);

    }

    public String incrementDateByDay(int day, String currentDate) {

        Calendar c = Calendar.getInstance();
        try {
            c.setTime(Objects.requireNonNull(dateTimeFormatter.parse(currentDate)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.DATE, day);  // number of days to add, can also use Calendar.DAY_OF_MONTH in place of Calendar.DATE

        return dateTimeFormatter.format(c.getTime());
    }

    public String incrementDateInDateFormate(int day, String currentDate) {

        //return only date not a time
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(Objects.requireNonNull(dateFormatter.parse(currentDate)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.DATE, day);  // number of days to add, can also use Calendar.DAY_OF_MONTH in place of Calendar.DATE

        return dateFormatter.format(c.getTime());
    }

    public Date convertStringToDateTime(String d) {

        Date date = null;
        try {
            date = dateTimeFormatter.parse(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public Date convertStringToDateTimeSecond(String d) {

        Date date = null;
        try {
            date = dateTimeSecondFormatter.parse(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public Date convertStringToDate(String d) {

        Date date = null;
        try {
            date = dateFormatter.parse(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public void timePicker() {
        final Calendar mCalendar = Calendar.getInstance();

        final int mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        final int mMinute = mCalendar.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener mTimeSetListener = (view, hourOfDay, minute) -> {

            mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mCalendar.set(Calendar.MINUTE, minute);
            String time = timeFormatter.format(mCalendar.getTime());
            onTimeSelectListener.onTimeSelected(time);
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(context, mTimeSetListener, mHour, mMinute, false);

        timePickerDialog.show();
    }

    public void timePicker(String savedTime) {//For start date picker with saved time

        Date date = null;
        try {
            date = timeFormatter.parse(savedTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int hour = 0; // int
        int minute = 0;
        if (date != null) {
            hour = date.getHours();
            minute = date.getMinutes(); // int
        }

        final Calendar mCalendar = Calendar.getInstance();
        TimePickerDialog.OnTimeSetListener mTimeSetListener = (view, hourOfDay, minute1) -> {

            mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mCalendar.set(Calendar.MINUTE, minute1);
            String time = timeFormatter.format(mCalendar.getTime());
            onTimeSelectListener.onTimeSelected(time);
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(context, mTimeSetListener, hour, minute, false);

        timePickerDialog.show();
    }

    public boolean isSelectedTimeSmallerThanCurrentTime(String selectedTime, String currentTime) {

        try {
            Date date1 = timeFormatter.parse(selectedTime);
            Date date2 = timeFormatter.parse(currentTime);

            if (date1 != null) {
                if (date1.before(date2)) {
                    return true;
                } else {
                    // is both time are same?
                    return date1.compareTo(date2) == 0;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
}
