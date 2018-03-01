package vitalypanov.phototracker.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import vitalypanov.phototracker.R;
import vitalypanov.phototracker.RunningTrackGoogleMapFragment;
import vitalypanov.phototracker.RunningTrackShortInfoFragment;
import vitalypanov.phototracker.others.ViewPageUpdater;


/**
 * Created by Vitaly on 23.02.2018.
 */

public class RunningTrackPagerActivity extends AppCompatActivity {
    private static final String TAG = "PhotoTracker";
    // Pages
    private static final int PAGE_SHORT_INFO = 0;
    private static final int PAGE_GOOGLE_MAP = 1;
    private static final int PAGES_COUNT = PAGE_GOOGLE_MAP + 1;
    private ViewPager mViewPager;
    private FragmentStatePagerAdapter mPagerAdapter;
    private Button mLeftButton;

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
            @Override
            public Fragment getItem(int position) {
                switch (position){
                    case PAGE_SHORT_INFO:
                        // short info page of current track
                        return RunningTrackShortInfoFragment.newInstance();
                    case PAGE_GOOGLE_MAP:
                        // google map page of current track
                        return RunningTrackGoogleMapFragment.newInstance();
                    default:
                        return null; // Oooops!
                }
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
        mViewPager.setCurrentItem(PAGE_SHORT_INFO);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        // TODO
        // Check here if track recording are already started
        // in this case show warning message before leave
        // super.onBackPressed();
    }
}
