package com.shockwave.clockproj;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Program: Clock Project
 *
 * Class: ClockMain
 * Author: Nolan Schock (Shockwave)
 * Version: 2.1
 * Description: Main Activity for Clock Project. Instantiates ActionBar Tabs which make use of Fragments
 * Last Updated: November 2012
 * Recent Changes:
 */
public class ClockMain extends SherlockFragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Clock Project");
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.Tab stopwatchTab = actionBar.newTab().setText("Stopwatch").setTabListener(new TabListener<StopwatchFragment>(this, "Stopwatch", StopwatchFragment.class));
        actionBar.addTab(stopwatchTab);
        ActionBar.Tab timerTab = actionBar.newTab().setText("Timer").setTabListener(new TabListener<TimerFragment>(this, "Timer", TimerFragment.class));
        actionBar.addTab(timerTab);

        if (savedInstanceState != null) {
            actionBar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }
    }

    public static class TabListener<T extends SherlockFragment> implements ActionBar.TabListener {
        private final SherlockFragmentActivity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private Fragment mFragment;
        private final Bundle mArgs;

        public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz) {
            this(activity, tag, clz, null);
        }

        public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz, Bundle args) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            mArgs = args;

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
        }

        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.getName());
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                ft.detach(mFragment);
            }
        }

        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            //Do nothing
        }
    }
}
