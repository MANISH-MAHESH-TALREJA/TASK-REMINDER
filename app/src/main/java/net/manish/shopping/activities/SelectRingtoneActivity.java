package net.manish.shopping.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.manish.shopping.R;
import net.manish.shopping.adapter.RingtoneListAdapter;
import net.manish.shopping.helper.RecyclerViewClickListener;
import net.manish.shopping.helper.RecyclerViewTouchListener;
import net.manish.shopping.model.RingtoneModel;
import net.manish.shopping.utils.CommonUtils;
import net.manish.shopping.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SelectRingtoneActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SessionManager sessionManager;

    private final List<RingtoneModel> defaultsNotificationList = new ArrayList<>();
    private RingtoneListAdapter adapter;

    private MediaPlayer mp;
    private Uri ringtoneUri;
    private boolean isRingtonePlaying = false;
    private int ringtoneId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_ringtone);

        sessionManager = new SessionManager(this);
        defaultsNotificationList.clear();
        defaultsNotificationList.addAll(CommonUtils.getDefaultRingtoneList(this));
        findViews();
        setupRecyclerView();
    }

    private void findViews() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.notification_tone));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recycler_view);
        LinearLayout linearLayoutRoot = findViewById(R.id.lnr_root_tv);

        //set On click listeners
        linearLayoutRoot.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, sessionManager.getRingtoneUri());
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            //noinspection deprecation
            startActivityForResult(intent, 999);
        });
    }

    private void setupRecyclerView() {
        //recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new RingtoneListAdapter(defaultsNotificationList, this);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(this, recyclerView, new RecyclerViewClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view, final int position) {

                ringtoneUri = defaultsNotificationList.get(position).getUri();

                final ImageView ivPlay = view.findViewById(R.id.iv_play);
                CheckBox checkBox = view.findViewById(R.id.checkbox);

                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

                    if (isChecked) {
                        sessionManager.setRingtoneUri(defaultsNotificationList.get(position).getUri().toString());
                    } else {
                        sessionManager.setDefaultRingtone();
                    }
                    adapter.notifyDataSetChanged();
                });
                ivPlay.setOnClickListener(v -> handlePlayingAudio(position));

            }

            @Override
            public void onLongClick(View view, int position) {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), notification);
                mp.start();
            }
        }));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void handlePlayingAudio(int position) {
        if (isRingtonePlaying && mp != null) {
            mp.stop();
            mp.release();
        }

        if (ringtoneId == defaultsNotificationList.get(position).getId() && isRingtonePlaying) {
            defaultsNotificationList.get(position).setPlaying(false);
            isRingtonePlaying = false;
        } else {
            mp = MediaPlayer.create(getApplicationContext(), ringtoneUri);
            mp.start();
            ringtoneId = defaultsNotificationList.get(position).getId();
            isRingtonePlaying = true;
            changePlayingRingtoneFlag();
            defaultsNotificationList.get(position).setPlaying(true);

            mp.setOnCompletionListener(mp -> {

                isRingtonePlaying = false;
                mp.release();
                changePlayingRingtoneFlag();

                adapter.notifyDataSetChanged();
            });
        }

        adapter.notifyDataSetChanged();
    }

    private void changePlayingRingtoneFlag() {
        for (int i = 0; i < defaultsNotificationList.size(); i++) {
            defaultsNotificationList.get(i).setPlaying(false);
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

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onPause() {

        if (isRingtonePlaying && mp != null) {

            mp.stop();
            mp.release();
            changePlayingRingtoneFlag();
            adapter.notifyDataSetChanged();
            isRingtonePlaying = false;
        }
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999 && resultCode == RESULT_OK) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            sessionManager.setRingtoneUri(Objects.requireNonNull(uri).toString());
        }
    }
}
