package net.manish.shopping.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import net.manish.shopping.fragments.CompletedTaskFragment;
import net.manish.shopping.fragments.PendingTaskFragment;

public class TaskListPagerAdapter extends FragmentStatePagerAdapter {
    private final int mNumOfTabs;

    public TaskListPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new PendingTaskFragment();
            case 1:
                return new CompletedTaskFragment();
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
