package vitalypanov.phototracker;

import android.content.Context;
import android.content.DialogInterface;
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

import java.util.List;

import vitalypanov.phototracker.database.TrackDbHelper;
import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.utilities.BitmapScalerUtils;

/**
 * Created by Vitaly on 25.02.2018.
 */

public class TrackListFragment  extends Fragment {
    private static final String TAG = "PhotoTracker";
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
        private TextView mDistanceTextView;
        private TextView mDurationTextView;
        private TextView mCommentTextView;
        private ImageButton mDeleteButton;
        private ImageView mTrackPhotoImageView;
        private RelativeLayout mLoadingPanel;

        public TrackHolder(View itemView ){
            super(itemView);
            itemView.setOnClickListener(this);
            mStartDateTextView = (TextView)itemView.findViewById(R.id.list_item_start_date_text_view);
            mStartTimeTextView = (TextView)itemView.findViewById(R.id.list_item_start_time_text_view);
            mDistanceTextView = (TextView)itemView.findViewById(R.id.list_item_distance_text_view);
            mDurationTextView = (TextView) itemView.findViewById(R.id.list_item_duration_text_view);
            mTrackPhotoImageView = (ImageView) itemView.findViewById(R.id.list_item_track_photo_image);
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
            updatePhotoUI();
        }

        private void updatePhotoUI(){
            BitmapScalerUtils.updatePhotoAssync(mTrack, mTrackPhotoImageView, mTrackRecyclerView.getWidth(), getContext(), mLoadingPanel);
        }

        @Override
        public void onClick(View v) {

            //mCallbacks.onTrackSelected(mTrack);
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