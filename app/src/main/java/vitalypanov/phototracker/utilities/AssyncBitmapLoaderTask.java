package vitalypanov.phototracker.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Drawing photo on image view control asynchronously.
 * Do drawing with scaling within provided scaleWidth value
 *
 * Created by Vitaly on 28.02.2018.
 */

public class AssyncBitmapLoaderTask extends AsyncTask<Void, Void, Bitmap> {

    String mTrackPhotoFileName; // Track object - from which we take LAST photo - and draw it on image view
    ImageView mImageView;   // Which image view we place photo
    int mScaleWidth;        // Which width bitmap of photo will be scaled
    Context mContext;
    RelativeLayout mLoadingPanel;   // Animated progress picture which shows when image loads (can be null)

    public AssyncBitmapLoaderTask(String trackPhotoFileName, ImageView imageView, int scaleWidth, Context context, RelativeLayout loadingPanel) {
        this.mTrackPhotoFileName = trackPhotoFileName;
        this.mImageView = imageView;
        this.mScaleWidth = scaleWidth;
        this.mContext = context;
        this.mLoadingPanel=loadingPanel;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        if (StringUtils.isNullOrBlank(mTrackPhotoFileName)){
            return null;
        }
        return BitmapHandler.get(mContext).getBitmapScaleToFitWidth(mTrackPhotoFileName, mScaleWidth);
    }

    @Override
    protected void onPreExecute() {
        // show loading progress image only if photos exists in track
        if (mLoadingPanel!=null) {
            mLoadingPanel.setVisibility(!StringUtils.isNullOrBlank(mTrackPhotoFileName)? View.VISIBLE : View.GONE);
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
