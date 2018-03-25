package vitalypanov.phototracker;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

import vitalypanov.phototracker.utilities.FileUtils;

/**
 * Created by Vitaly on 25.03.2018.
 */

public class PhotoHandler implements PictureCallback {
    private static final String TAG = "PhotoTracker";

    private final Context mContext;
    private String mCurrentPhotoFileName;

    public PhotoHandler(Context context, String currentPhotoFileName) {
        this.mContext = context;
        this.mCurrentPhotoFileName = currentPhotoFileName;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        /*
        File pictureFileDir = getDir();

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            Log.d(TAG, "Can't create directory to save image.");
            Toast.makeText(mContext, "Can't create directory to save image.",
                    Toast.LENGTH_LONG).show();
            return;

        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());
        String photoFile = "Picture_" + date + ".jpg";

        String filename = pictureFileDir.getPath() + File.separator + photoFile;
        */
        File pictureFile  = FileUtils.getPhotoFile(mContext,mCurrentPhotoFileName);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            Toast.makeText(mContext, "New Image saved:" + mCurrentPhotoFileName,
                    Toast.LENGTH_LONG).show();
        } catch (Exception error) {
            Log.d(TAG, "File" + mCurrentPhotoFileName + "not saved: "
                    + error.getMessage());
            Toast.makeText(mContext, "Image could not be saved.",
                    Toast.LENGTH_LONG).show();
        }
    }
}