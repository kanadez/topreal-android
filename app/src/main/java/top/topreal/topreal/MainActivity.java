package top.topreal.topreal;

import android.Manifest; // доступ к манифесту
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent; // Intent - абстрактное описание операции, которая будет выполнена
import android.content.SharedPreferences;
import android.content.pm.PackageManager; // взаимодействие приложения с установленными пакетами на устройстве
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat; // хелпер для доступа к опциям Activity
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private String current_session; // номер текущей сессии Совы TopReal, откуда совершается исходящий звонок
    private TextView main_title_text;
    private TextView main_hint_text;
    private TextView main_logout_button;
    private TextView main_username_text;
    private RequestQueue MyRequestQueue;
    private static MainActivity ins;
    private CookieStore cookieStore;
    private String phone_number;
    private XHR request;
    private CustomAlert alert;
    private MyObserver my_observer;
    private JSONObject locale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ins = this;
        setContentView(R.layout.activity_main);
        alert = new CustomAlert(this);
        main_title_text = (TextView) findViewById(R.id.main_title_text);
        main_hint_text = (TextView) findViewById(R.id.main_hint_text);
        main_logout_button = (Button) findViewById(R.id.main_logout_button);
        main_username_text = (TextView) findViewById(R.id.main_username_text);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        MyRequestQueue = Volley.newRequestQueue(this);
        CallReceiver call_receiver = new CallReceiver();
        cookieStore = new PersistentCookieStore(this);
        CookieManager manager = new CookieManager( cookieStore, CookiePolicy.ACCEPT_ALL );
        CookieHandler.setDefault(manager);
        my_observer = new MyObserver(new Handler());
        ContentResolver contentResolver = this.getContentResolver();
        contentResolver.registerContentObserver(Uri.parse("content://sms"),true, my_observer);

        SharedPreferences prefs = this.getPreferences(this.MODE_PRIVATE);
        String fcm_token = prefs.getString("fcm_token", "");

        request = new XHR(this);
        request.post(
            "/api/user/isauth.json",
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("isauth", response);

                    if (response.equals("true")){
                        showUserName();
                    }
                    else if (response.equals("false")){
                        showLoginActivity();
                    }
                }
            },
            new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i("isauth", error.toString());
                    alert.error("Ошибка", "Что-то пошло не так. Перезапустите приложение.");
                    showLoginActivity();
                }
            }
        );

        Context context = getApplicationContext();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("TopReal");

        Intent intent = new Intent( context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0 , intent, 0);
        builder.setContentIntent(pIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notif = builder.build();
        notif.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(0, notif);

        /* Getting SMS permissions */
        String receive_permission = Manifest.permission.RECEIVE_SMS;
        String send_permission = Manifest.permission.SEND_SMS;
        String read_permission = Manifest.permission.READ_SMS;
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
                            localizeMainActivity();
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

    /*public void showCookie(View v){
        Log.i("CookieStore", cookieStore.get(URI.create("http://dev.topreal.top/")).toString());
        Log.i("CookieStore", cookieStore.getURIs().toString());
    }*/

    public void callPhoneNumber(String number){
        phone_number = number;

        try
        {
            if(Build.VERSION.SDK_INT > 22)
            {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 101);

                    return;
                }

                Intent callIntent = new Intent(Intent.ACTION_CALL); // создаем операцию соверешния вызова по телефону
                callIntent.setData(Uri.parse("tel:".concat(number))); // задаем номер для вызова

                /*if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) { // если разрешение на звонок получено от системы
                    return; // завершаем функцию
                }*/

                startActivity(callIntent); // начинаем звонок

            }
            else {
                Intent callIntent = new Intent(Intent.ACTION_CALL); // создаем операцию соверешния вызова по телефону
                callIntent.setData(Uri.parse("tel:".concat(number))); // задаем номер для вызова

                /*if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) { // если разрешение на звонок получено от системы
                    return; // завершаем функцию
                }*/

                startActivity(callIntent); // начинаем звонок
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void sendSMS(String phone_number, String sms_text){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phone_number));
        intent.putExtra("sms_body", sms_text);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if(requestCode == 101){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                callPhoneNumber(phone_number);
            }
            else{
                Log.e("Permission", "Permission not Granted");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public static MainActivity getInstace(){
        return ins;
    }

    public String getCurrentSessionID(){
        return current_session;
    }

    public void setCurrentSessionID(String value){
        current_session = value;
    }

    public void updateTheTextView(final String t) {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                TextView textV1 = (TextView) findViewById(R.id.main_hint_text);
                textV1.setText(t);
            }
        });
    }

    public void showLoginActivity(){
        Intent myIntent = new Intent(this, LoginActivity.class);
        startActivity(myIntent);
    }

    public void showUserName(){
        request = new XHR(this);
        request.post(
                "/api/user/getmyname.json",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        main_username_text.setText(response.replace("\"", ""));
                    }
                },
                new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                    @Override
                    public void onErrorResponse(VolleyError error) {
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

    public void logout(View v){
        request = new XHR(this);
        request.post(
            "/api/login/logout.json",
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response.equals("0")){
                        showLoginActivity();
                    }
                    else{
                        alert.error("Ошибка", "Что-то пошло не так. Попробуйте снова.");
                    }
                }
            },
            new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                @Override
                public void onErrorResponse(VolleyError error) {
                    alert.error("Ошибка", "Что-то пошло не так. Попробуйте снова.");
                }
            }
        );
    }

    public void localizeMainActivity(){
        try{
            JSONArray locale_data = locale.getJSONArray("locale_data");
            String locale_value = locale.getString("locale_value");

            for (int i = 0; i < locale_data.length(); i++){
                JSONObject variable = new JSONObject(locale_data.get(i).toString());

                switch(variable.getString("variable")) {
                    case "app_change_user":
                        main_logout_button.setText(variable.getString(locale_value));;
                    break;
                    case "app_to_make_call_hint":
                        main_hint_text.setText(variable.getString(locale_value));
                    break;
                    case "header_welcome":
                        main_title_text.setText(variable.getString(locale_value));
                    break;
                }
            }

        }
        catch (JSONException e) {
            Log.w("JSONException", e);
        }
    }
}
