package vitalypanov.phototracker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import vitalypanov.phototracker.activity.GoogleMapActivity;
import vitalypanov.phototracker.activity.RunningTrackPagerActivity;
import vitalypanov.phototracker.activity.TrackImagesPagerActivity;
import vitalypanov.phototracker.database.TrackDbHelper;
import vitalypanov.phototracker.export.RunKeeperExport;
import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.model.TrackPhoto;
import vitalypanov.phototracker.utilities.AssyncBitmapLoaderTask;
import vitalypanov.phototracker.utilities.StringUtils;
import vitalypanov.phototracker.utilities.Utils;

/**
 * Created by Vitaly on 25.02.2018.
 */

public class TrackListFragment  extends Fragment {
    private static final String TAG = "PhotoTracker";
    private static final int REQUEST_CODE_IMAGES_PAGER = 0;
    private RecyclerView mTrackRecyclerView;
    private TrackAdapter mAdapter;
    private Callbacks mCallbacks;


    // Interface for activity host
    public interface Callbacks{
        void onTrackSelected(Track crime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
       //mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks =null;
    }

    public static TrackListFragment newInstance() {
        return new TrackListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_list, container, false);
        mTrackRecyclerView = (RecyclerView)view.
                findViewById(R.id.crime_recycler_view);
        mTrackRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // add divider
        DividerItemDecoration divider = new DividerItemDecoration(mTrackRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.custom_divider));
        mTrackRecyclerView.addItemDecoration(divider);
        updateUI();
        return view;
    }

    public void updateUI(){
        TrackDbHelper trackDbHelper = TrackDbHelper.get(getActivity());
        List<Track> tracks = trackDbHelper.getTracks();
        if (mAdapter == null) {
            mAdapter = new TrackAdapter(tracks);
            mTrackRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setTracks(tracks);
            mAdapter.notifyDataSetChanged();
        }
    }

    // ViewHolder
    private class TrackHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        Track mTrack;
        TrackAdapter mTrackAdapter;
        private TextView mStartDateTextView;
        private TextView mStartTimeTextView;
        private ImageButton mDeleteButton;
        private TextView mDistanceTextView;
        private TextView mDurationTextView;
        private ImageButton mTrackMapButton;
        private ImageButton mTrackContinueButton;
        private ImageButton mRunKeeperButton;
        private RelativeLayout mTrackPhotoLayout;
        private ImageView mTrackPhotoImageView;
        private TextView mImageCounterTextView;
        private RelativeLayout mLoadingPanel;
        private TextView mCommentTextView;

        public TrackHolder(View itemView ){
            super(itemView);
            itemView.setOnClickListener(this);
            mStartDateTextView = (TextView)itemView.findViewById(R.id.list_item_start_date_text_view);
            mStartTimeTextView = (TextView)itemView.findViewById(R.id.list_item_start_time_text_view);
            mDistanceTextView = (TextView)itemView.findViewById(R.id.list_item_distance_text_view);
            mDurationTextView = (TextView) itemView.findViewById(R.id.list_item_duration_text_view);
            mTrackMapButton = (ImageButton) itemView.findViewById(R.id.list_item_track_map_button);
            mTrackMapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = GoogleMapActivity.newIntent(getActivity(), mTrack.getUUID());
                    startActivity(intent);
                }
            });

            mTrackContinueButton = (ImageButton) itemView.findViewById(R.id.list_item_track_continue_button);
            mTrackContinueButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Run service within existing track object

                    // first check location services
                    if (!LocationServices.get(getActivity()).checkLocaionServices()){
                        return;
                    }
                    // show warning message before continue recording of the track
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setTitle(R.string.continue_track_confirm_title);
                    String sMessage = getResources().getString(R.string.continue_track_confirm_message) +
                            "\n" +
                            "\n" + mTrack.getStartDateFormatted() + " " + mTrack.getStartTimeFormatted()+
                            "\n" + mTrack.getDistanceFormatted() + " " + getResources().getString(R.string.distance_metrics) + ". " +
                            mTrack.getDurationTimeFormatted() + " " + getResources().getString(R.string.duration) + "." +
                            (mTrack.getComment()!= null? "\n" + mTrack.getComment() : "");
                    builder.setMessage(sMessage);
                    builder.setPositiveButton(R.string.continue_track_confirm_button_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // confirmed to continue existing track!

                                    // Run track recording service and provide them our selected track to continue
                                    Intent i = TrackerGPSService.newIntent(getActivity(), mTrack.getUUID());
                                    getActivity().startService(i);

                                    // third - hide current activity and show "status" activity
                                    getActivity().finish();
                                    Intent intent = RunningTrackPagerActivity.newIntent(getActivity());
                                    startActivity(intent);

                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // cancel confirmation dialog - do nothing
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });

            mRunKeeperButton = (ImageButton) itemView.findViewById(R.id.list_item_runkeeper_button);
            mRunKeeperButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // get acces token
                    final String accessToken = Settings.get(getActivity()).getString(Settings.KEY_MAP_RUNKEEPER_ACCESS_TOKEN);
                    if (StringUtils.isNullOrBlank(accessToken)) {
                        // if empty - inform user that he should define it...
                        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getActivity()).create();
                        alertDialog.setTitle(getResources().getString(R.string.runkeeper_not_setup_title));
                        alertDialog.setMessage(getResources().getString(R.string.runkeeper_not_setup_message));
                        alertDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                        return;
                    }

                    // if has access token ask confirmation to upload...
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setTitle(R.string.runkeeper_upload_confirm_title);
                    String sMessage = getResources().getString(R.string.runkeeper_upload_confirm_message) +
                            "\n" +
                            "\n" + mTrack.getStartDateFormatted() + " " + mTrack.getStartTimeFormatted()+
                            "\n" + mTrack.getDistanceFormatted() + " " + getResources().getString(R.string.distance_metrics) + ". " +
                            mTrack.getDurationTimeFormatted() + " " + getResources().getString(R.string.duration) + "." +
                            (mTrack.getComment()!= null? "\n" + mTrack.getComment() : "");
                    builder.setMessage(sMessage);
                    builder.setPositiveButton(R.string.runkeeper_confirm_button_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // confirmed uploading - do it
                                    exportRunKeeper(mTrack, accessToken);
                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // cancel confirmation dialog - do nothing
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            });

            // photo layout elements:
            mTrackPhotoLayout = (RelativeLayout) itemView.findViewById(R.id.list_item_track_photo_layout);
            mTrackPhotoImageView = (ImageView) itemView.findViewById(R.id.list_item_track_photo_image);
            mTrackPhotoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mTrack.getPhotoFiles().size()>0) {
                        Intent intent = TrackImagesPagerActivity.newIntent(getActivity(), mTrack.getUUID(), (ArrayList< TrackPhoto>) mTrack.getPhotoFiles(), null);
                        startActivityForResult(intent, REQUEST_CODE_IMAGES_PAGER);
                    }
                }
            });
            mImageCounterTextView= (TextView) itemView.findViewById(R.id.list_item_image_counter_textview);

            // progress of loading image layout
            mLoadingPanel = (RelativeLayout) itemView.findViewById(R.id.list_item_track_loading_photo);

            mCommentTextView = (TextView) itemView.findViewById(R.id.list_item_comment_text_view);

            mDeleteButton= (ImageButton) itemView.findViewById(R.id.list_item_track_delete_button);
            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setTitle(R.string.remove_track_confirm_title);
                    String sMessage = getResources().getString(R.string.remove_track_confirm_message) +
                            "\n" +
                            "\n" + mTrack.getStartDateFormatted() + " " + mTrack.getStartTimeFormatted()+
                            "\n" + mTrack.getDistanceFormatted() + " " + getResources().getString(R.string.distance_metrics) + ". " +
                            mTrack.getDurationTimeFormatted() + " " + getResources().getString(R.string.duration) + "." +
                            (mTrack.getComment()!= null? "\n" + mTrack.getComment() : "");
                    builder.setMessage(sMessage);
                    builder.setPositiveButton(R.string.remove_track_confirm_button_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // confirm deleting - do it
                                    TrackDbHelper.get(getContext()).deleteTrack(mTrack);
                                    mTrackAdapter.removeAt(getAdapterPosition());
                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // cancel confirmation dialog - do nothing
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        }


        public void bindTrack(Track track, TrackAdapter trackAdapter){
            mTrack = track;
            mTrackAdapter = trackAdapter;

            mStartDateTextView.setText(mTrack.getStartDateFormatted());
            mStartTimeTextView.setText(mTrack.getStartTimeFormatted());
            mDistanceTextView.setText(mTrack.getDistanceFormatted());
            mDurationTextView.setText(mTrack.getDurationTimeFormatted());
            mCommentTextView.setText(mTrack.getComment());
            mTrackPhotoLayout.setVisibility(mTrack.getPhotoFiles().size() > 0 ? View.VISIBLE : View.GONE);
            mImageCounterTextView.setText(" " + String.valueOf(mTrack.getPhotoFiles().size()) + " ");
            mImageCounterTextView.bringToFront();
            updatePhotoUI();
        }

        private void updatePhotoUI(){
            if (Utils.isNull(mTrack)){
                return;
            }
            String photoFileName = Utils.isNull(mTrack.getLastPhotoItem()) ? "" : mTrack.getLastPhotoItem().getPhotoFileName();
            AssyncBitmapLoaderTask assyncImageViewUpdater = new AssyncBitmapLoaderTask(photoFileName,
                    mTrackPhotoImageView,
                    mTrackRecyclerView.getWidth(),
                    getContext(),
                    mLoadingPanel);
            assyncImageViewUpdater.execute();
        }

        @Override
        public void onClick(View v) {

            //mCallbacks.onTrackSelected(mTrack);
        }

    }
    /**
     * Export to runkeeper.com
     * @param accessToken
     */
    private void exportRunKeeper(Track track, String accessToken){
        if (!StringUtils.isNullOrBlank(accessToken)) {
            RunKeeperExport runKeeperExport = new RunKeeperExport(track, getActivity());
            runKeeperExport.setAccessToken(accessToken);
            runKeeperExport.export();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK){
            return;
        }

        switch (requestCode){
            case REQUEST_CODE_IMAGES_PAGER:
                mAdapter.setTracks(TrackDbHelper.get(getActivity()).getTracks());
                mAdapter.notifyDataSetChanged();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);;
        }
    }

    // Adapter
    private class TrackAdapter extends RecyclerView.Adapter<TrackHolder>{
        private List<Track> mTracks;

        public TrackAdapter(List<Track> tracks) {
            mTracks = tracks;
        }

        @Override
        public TrackHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_track, parent, false);
            return new TrackHolder(view);
        }

        @Override
        public void onBindViewHolder(TrackHolder holder, int position) {
            Track track = mTracks.get(position);
            holder.bindTrack(track, this);
        }

        @Override
        public int getItemCount() {
            return mTracks.size();
        }

        public void setTracks(List<Track> tracks){
            mTracks = tracks;
        }

        /**
         * Delete position from list
         * @param position
         */
        public void removeAt(int position) {
            mTracks.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mTracks.size());
        }
    }
}