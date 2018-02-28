package vitalypanov.phototracker.utilities;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by Vitaly on 28.02.2018.
 */

public class FileUtils {

    /**
     * Getting File object of new photo file
     * @param context
     * @return File object for taking picture
     */
    public static File getPhotoFile(Context context, String fileName){
        File externalFileDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (externalFileDir == null){
            return null;
        }
        return new File(externalFileDir, fileName);
    }
}
