package vitalypanov.phototracker.utilities;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.Nullable;

import java.io.File;

import vitalypanov.phototracker.R;

/**
 * Created by Vitaly on 28.02.2018.
 */

public class FileUtils {

    /**
     * Getting File object of new photo file
     * @param context
     * @return File object for taking picture
     */
    @Nullable
    public static File getPhotoFile(Context context, String fileName){
        //File externalFileDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File externalFileDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ),
                context.getResources().getString(R.string.photo_folder)
        );
        if (externalFileDir == null) {
            return null;
        }

        // create photo dir if it not exists yet
        if (!externalFileDir.exists()) {
            if(!externalFileDir.mkdirs()){
                // possibly doesnt have permissions for writing into external storage
                return null;
            }
        }

        File fileResult = new File(externalFileDir, fileName);
        return fileResult;
    }
}
