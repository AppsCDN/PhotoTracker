package vitalypanov.phototracker.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import vitalypanov.phototracker.model.Track;

/**
 * Drawing photo on image view control asynchronously.
 * Do drawing with scaling within provided scaleWidth value
 *
 * Created by Vitaly on 28.02.2018.
 */

public class AssyncBitmapLoaderTask extends AsyncTask<Void, Void, Bitmap> {

    Track mTrack;           // Track object - from which we take LAST photo - and draw it on image view
    ImageView mImageView;   // Which image view we place photo
    int mScaleWidth;        // Which width bitmap of photo will be scaled
    Context mContext;
    RelativeLayout mLoadingPanel;   // Animated progress picture which shows when image loads (can be null)

    public AssyncBitmapLoaderTask(Track track, ImageView imageView, int scaleWidth, Context context, RelativeLayout loadingPanel) {
        this.mTrack = track;
        this.mImageView = imageView;
        this.mScaleWidth = scaleWidth;
        this.mContext = context;
        this.mLoadingPanel=loadingPanel;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        return BitmapScalerUtils.getScaledBitmap(mTrack, mScaleWidth, mContext);
    }

    @Override
    protected void onPreExecute() {
        // show loading progress image only if photos exists in track
        if (mLoadingPanel!=null) {
            mLoadingPanel.setVisibility(mTrack.getPhotoFiles().size() > 0 ? View.VISIBLE : View.GONE);
        }
        mImageView.setImageBitmap(null);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (mLoadingPanel!=null) {
            mLoadingPanel.setVisibility(View.GONE);
        }
        mImageView.setImageBitmap(bitmap);
    }
}
