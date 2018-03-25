/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vitalypanov.phototracker.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.UUID;

import vitalypanov.phototracker.CameraFragment;
import vitalypanov.phototracker.R;
import vitalypanov.phototracker.model.TrackHolder;
import vitalypanov.phototracker.model.TrackPhoto;
import vitalypanov.phototracker.utilities.Utils;

public class CameraActivity extends SingleFragmentActivity {
    private static final String TAG = "PhotoTracker";
    private static final String EXTRA_TRACK_UUID = "phototracker.track_uuid";
    private static final String EXTRA_PHOTO_LIST = "phototracker.photo_list";
    UUID mTrackUUID;

    @Override
    protected int getLayoutResId() {
        // for camera is better black background:
        return  R.layout.activity_fragment_black;
    }

    public static Intent newIntent(Context context, UUID trackUUID){
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra(EXTRA_TRACK_UUID, trackUUID);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return CameraFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // VERY IMPORTANT line is below (without it camera API doesn't work)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        mTrackUUID= (UUID)getIntent().getSerializableExtra(EXTRA_TRACK_UUID);
        if (Utils.isNull(TrackHolder.get().getTrack())){
            // TODO due to some reasons track object is empty - may be we need to read it from db or something else
        }
    }

    public static ArrayList<TrackPhoto> getTrackPhotos(Intent data){
        return (ArrayList<TrackPhoto>)data.getSerializableExtra(EXTRA_PHOTO_LIST);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
