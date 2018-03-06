package vitalypanov.phototracker.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import vitalypanov.phototracker.R;
import vitalypanov.phototracker.RunningTrackGoogleMapFragment;
import vitalypanov.phototracker.RunningTrackShortInfoFragment;
import vitalypanov.phototracker.TrackerGPSService;
import vitalypanov.phototracker.others.BindTrackerGPSService;
import vitalypanov.phototracker.others.ViewPageUpdater;
import vitalypanov.phototracker.utilities.Utils;


/**
 * Created by Vitaly on 23.02.2018.
 */

public class RunningTrackPagerActivity extends AppCompatActivity {
    private static final String TAG = "PhotoTracker";
    // Pages
    private static final int PAGE_SHORT_INFO = 0;
    private static final int PAGE_GOOGLE_MAP = 1;
    private static final int PAGES_COUNT = 2;
    private ViewPager mViewPager;
    private FragmentStatePagerAdapter mPagerAdapter;
    private Button mLeftButton;
    private HashMap<String, Fragment> mFragmentHashMap = new HashMap<String, Fragment>();

    private TrackerGPSService mService;
    private ServiceConnection mConnection ; // Defines callbacks for service binding, passed to bindService()
    private boolean mIsBound;

    public static Intent newIntent(Context packageContext){
        Intent intent = new Intent(packageContext, RunningTrackPagerActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager_running_track);

        mViewPager = (ViewPager)findViewById(R.id.activity_pager_running_track_view_pager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mPagerAdapter =new FragmentStatePagerAdapter(fragmentManager) {
            private Fragment mCurrentFragment;

            public Fragment getCurrentFragment() {
                return mCurrentFragment;
            }

            @Override
            public Fragment getItem(int position) {
                Fragment fragment = null;
                switch (position){
                    case PAGE_SHORT_INFO:
                        fragment = mFragmentHashMap.get(RunningTrackShortInfoFragment.class.getName());
                        if (Utils.isNull(fragment)) {
                            fragment = RunningTrackShortInfoFragment.newInstance();
                            mFragmentHashMap.put(RunningTrackShortInfoFragment.class.getName(), fragment);
                        }
                        return fragment;
                    case PAGE_GOOGLE_MAP:
                        fragment = mFragmentHashMap.get(RunningTrackGoogleMapFragment.class.getName());
                        if (Utils.isNull(fragment)) {
                            fragment = RunningTrackGoogleMapFragment.newInstance();
                            mFragmentHashMap.put(RunningTrackGoogleMapFragment.class.getName(), fragment);
                        }
                        return fragment;
                    default:
                        return null; // Oooops!
                }
            }

            @Override
            public void setPrimaryItem(ViewGroup container, int position, Object object) {
                if (getCurrentFragment() != object) {
                    mCurrentFragment = ((Fragment) object);
                }
                super.setPrimaryItem(container, position, object);
            }

            @Override
            public int getCount() {
                return PAGES_COUNT;
            }

            @Override
            public void startUpdate(ViewGroup container) {
                super.startUpdate(container);
            }
        };
        mViewPager.setAdapter(mPagerAdapter);

        // when page is changed we can update fragment UI
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.i(TAG, "onPageScrolled position= " + position);
            }

            @Override
            public void onPageSelected(int position) {
                ViewPageUpdater fragment = (ViewPageUpdater) mPagerAdapter.instantiateItem(mViewPager, position);
                if (fragment != null) {
                    fragment.onPageSelected();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                int i = 0;
            }
        });

        // GPS Service binding...
        Intent i = TrackerGPSService.newIntent(this);
        mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                TrackerGPSService.LocalBinder binder = (TrackerGPSService.LocalBinder) service;
                mService = binder.getService();
                // need to update current visible page...
                List<Fragment> allFragments = new LinkedList<Fragment>();
                for (int i = 0; i < mPagerAdapter.getCount(); i++) {
                    BindTrackerGPSService fragment = (BindTrackerGPSService)mPagerAdapter.getItem(i);
                    fragment.onBindService(mService);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mService = null;
            }


        };

        doBindService();

        mViewPager.setCurrentItem(PAGE_SHORT_INFO);
    }

    private void doBindService() {
        Intent i = TrackerGPSService.newIntent(this);
        bindService(i, mConnection, 0); //Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        // pressing back on google map causing return to short info page
        if (mViewPager.getCurrentItem() == PAGE_GOOGLE_MAP){
            mViewPager.setCurrentItem(PAGE_SHORT_INFO);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}
