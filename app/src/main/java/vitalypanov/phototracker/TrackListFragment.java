package vitalypanov.phototracker;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;

import java.util.List;

import vitalypanov.phototracker.database.TrackDbHelper;
import vitalypanov.phototracker.model.Track;

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
        private TextView mStartTimeTextView;
        private TextView mDistanceTextView;
        private TextView mDurationTextView;
        private TextView mCommentTextView;

        public TrackHolder(View itemView ){
            super(itemView);
            itemView.setOnClickListener(this);
            mStartTimeTextView = (TextView)itemView.findViewById(R.id.list_item_start_time_text_view);
            mDistanceTextView = (TextView)itemView.findViewById(R.id.list_item_distance_text_view);
            mDurationTextView = (TextView) itemView.findViewById(R.id.list_item_duration_text_view);
            mCommentTextView = (TextView) itemView.findViewById(R.id.list_item_comment_text_view);
        }

        public void bindTrack(Track track){
            mTrack = track;
            mStartTimeTextView.setText(mTrack.getStartTimeFormatted());
            mDistanceTextView.setText(mTrack.getDistanceFormatted());
            mDurationTextView.setText(mTrack.getDurationTimeFormatted());
            mCommentTextView.setText(mTrack.getComment());
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
            holder.bindTrack(track);
        }

        @Override
        public int getItemCount() {
            return mTracks.size();
        }

        public void setTracks(List<Track> tracks){
            mTracks = tracks;
        }

    }
}