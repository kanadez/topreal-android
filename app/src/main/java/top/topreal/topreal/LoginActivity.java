package top.topreal.topreal;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {
    private XHR request;
    private EditText login_edit;
    private EditText password_edit;
    private TextView login_title_text;
    private CustomAlert alert;
    private CookieStore cookieStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login_edit = (EditText) findViewById(R.id.login_edit);
        password_edit = (EditText) findViewById(R.id.password_edit);
        login_title_text = (TextView) findViewById(R.id.login_title_text);
        alert = new CustomAlert(this);
        cookieStore = new PersistentCookieStore(this);

        if(Build.VERSION.SDK_INT > 22){
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE}, 101);

                return;
            }
        }
    }

    public void login(View v){
        request = new XHR(this);
        request.putParameter("email", login_edit.getText().toString());
        request.putParameter("password", md5(password_edit.getText().toString()));
        request.post(
                "/api/login/appgetkey.json",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject json_object = new JSONObject(response);

                            if (json_object.has("error")) {
                                JSONObject error_json = json_object.getJSONObject("error");

                                if (error_json.getString("description").equals("forbidden_credentials_incorrect")) {
                                    alert.error("Ошибка", "Неверные логин или пароль.");
                                }
                                else if(error_json.getString("description").equals("forbidden_voip_disabled")){
                                    alert.error("Ошибка", "Вы не можете войти, в Вашем агентстве не куплена услуга телефонии!");
                                }
                            }
                            else{
                                HttpCookie id_cookie = new HttpCookie("id", json_object.getString("sUserId"));
                                HttpCookie token_cookie = new HttpCookie("token", json_object.getString("sToken"));
                                cookieStore.add(URI.create(AppSettings.dev_host), id_cookie);
                                cookieStore.add(URI.create(AppSettings.dev_host), token_cookie);

                                showAuthActivity(json_object.getString("sUserId"));
                            }
                        }
                        catch (JSONException e) {
                            Log.w("JSONException", e);
                            alert.error("Ошибка данных", "Что-то не так с Вашими данными. Попробуйте ввести другие.");
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

    public static final String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void showAuthActivity(String user_id){
        SharedPreferences prefs = MainActivity.getInstace().getPreferences(MainActivity.getInstace().MODE_PRIVATE);
        String fcm_token = prefs.getString("fcm_token", "");

        try{
            if (fcm_token.equals("")){
                throw new Exception("fcm_token is empty");
            }

            request = new XHR(this);
            request.putParameter("user", user_id);
            request.putParameter("token", fcm_token);
            request.post("/api/fcm/settoken.json",
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {
                        Log.i("settoken", response);
                    }
                },
                new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        alert.error("Ошибка", "Что-то пошло не так. Перезапустите приложение.");
                    }
                });
        }
        catch (Exception e){
            alert.error("Ошибка", "Что-то пошло не так. Перезапустите приложение.");
        }

        Intent myIntent = new Intent(this, AuthActivity.class);
        startActivity(myIntent);
    }
}
