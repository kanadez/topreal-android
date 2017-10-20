package top.topreal.topreal;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AuthActivity extends AppCompatActivity {
    private TextView auth_title_text;
    private TextView auth_caption_text;
    private TextView auth_hint_text;
    private JSONObject locale;
    private XHR request;
    private CustomAlert alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        alert = new CustomAlert(this);
        auth_title_text = (TextView) findViewById(R.id.auth_title_text);
        auth_hint_text = (TextView) findViewById(R.id.auth_hint_text);
        auth_caption_text = (TextView) findViewById(R.id.auth_caption_text);

        /* Getting SMS permissions */
        String receive_permission = android.Manifest.permission.RECEIVE_SMS;
        String send_permission = android.Manifest.permission.SEND_SMS;
        String read_permission = android.Manifest.permission.READ_SMS;
        int receive_grant = ContextCompat.checkSelfPermission(this, receive_permission);
        int send_grant = ContextCompat.checkSelfPermission(this, send_permission);
        int read_grant = ContextCompat.checkSelfPermission(this, read_permission);

        if (
                receive_grant != PackageManager.PERMISSION_GRANTED ||
                send_grant != PackageManager.PERMISSION_GRANTED ||
                read_grant != PackageManager.PERMISSION_GRANTED
        ){
            String[] permission_list = new String[3];
            permission_list[0] = receive_permission;
            permission_list[1] = send_permission;
            permission_list[2] = read_permission;
            ActivityCompat.requestPermissions(this, permission_list, 1);
        }

        request = new XHR(this);
        request.post(
                "/api/localization/getdefaultlocale.json",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            Log.i("locale", response);
                            locale = new JSONObject(response);
                            localizeAuthActivity();
                        }
                        catch (JSONException e) {
                            Log.w("JSONException", e);
                        }
                    }
                },
                new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("localization", error.toString());
                        alert.error("Ошибка", "Что-то пошло не так. Перезапустите приложение.");
                    }
                }
        );
    }

    public void minimizeApp(View v){
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    public void localizeAuthActivity(){
        try{
            JSONArray locale_data = locale.getJSONArray("locale_data");
            String locale_value = locale.getString("locale_value");

            for (int i = 0; i < locale_data.length(); i++){
                JSONObject variable = new JSONObject(locale_data.get(i).toString());

                switch(variable.getString("variable")) {
                    case "app_you_authorized":
                        auth_title_text.setText(variable.getString(locale_value));;
                    break;
                    case "app_can_close_now":
                        auth_caption_text.setText(variable.getString(locale_value));
                    break;
                    case "app_to_make_call_hint":
                        auth_hint_text.setText(variable.getString(locale_value));
                    break;
                }
            }

        }
        catch (JSONException e) {
            Log.w("JSONException", e);
        }
    }
}
