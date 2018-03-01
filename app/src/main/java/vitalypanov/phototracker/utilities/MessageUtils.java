package vitalypanov.phototracker.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import vitalypanov.phototracker.R;

/**
 * Message boxes helper.
 *
 * Created by Vitaly on 01.03.2018.
 */

public class MessageUtils {

    /**
     * Show simple message box with one button OK
     * @param title     Dialog title
     * @param message   Dialog message body
     * @param context
     */
    public static  void ShowMessageBox(String title, String message, Context context ){
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, context.getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // nothing to do on exit - it just info message
                    }
                });
        alertDialog.show();
    }

    /**
     * ...Resource Id's variant
     * @param titleResourceId
     * @param messageResourceId
     * @param context
     */
    public static void ShowMessageBox(int titleResourceId, int messageResourceId, Context context){
        ShowMessageBox(
                context.getResources().getString(titleResourceId),
                context.getResources().getString(messageResourceId),
                context );
    }

}
