package net.manish.shopping.activities;

import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import net.manish.shopping.R;
import net.manish.shopping.fragments.ActiveContactsFragment;
import net.manish.shopping.fragments.BlockedContactsFragment;

import java.util.Objects;

public class MyContactsActivityWithTabs extends AppCompatActivity {

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs_my_contacts);

        findViews();

    }

    private void findViews() {

        //setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.my_contacts));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //setup tab bar
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.active)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.blocked)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //setup viewpager
        viewPager = findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(0);
        MyContactsPagerAdapter pagerAdapter = new MyContactsPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("deprecation")
    public static class MyContactsPagerAdapter extends FragmentStatePagerAdapter {
        int mNumOfTabs;

        public MyContactsPagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new ActiveContactsFragment();
                case 1:
                    return new BlockedContactsFragment();
                default:
                    //noinspection ConstantConditions
                    return null;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }

}
