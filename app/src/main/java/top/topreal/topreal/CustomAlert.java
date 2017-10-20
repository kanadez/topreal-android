package top.topreal.topreal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;

/**
 * Created by x on 30.09.2017.
 */

public class CustomAlert {
    private Context context;

    public CustomAlert(Context ctx){
        this.context = ctx;
    }

    public void error(String title, String message){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }
}
