package top.topreal.topreal;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by x on 08.10.2017.
 */

class MyObserver extends ContentObserver {
    private XHR request;
    private CustomAlert alert;

    private String event_type;
    private JSONObject event_data;

    public MyObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Uri uriSMSURI = Uri.parse("content://sms");
        Cursor cur = MainActivity.getInstace().getContentResolver().query(uriSMSURI, null, null, null, null);
        cur.moveToNext();
        String address = cur.getString(cur.getColumnIndex("address"));
        String body = cur.getString(cur.getColumnIndex("body"));

        event_type = "call-out";

        try {
            event_data = new JSONObject();
            event_data.put("number", address);
            event_data.put("text", body);
            event_data.put("session", MainActivity.getInstace().getCurrentSessionID());
            Log.w("JSON", event_data.toString());

            //alert = new CustomAlert(ctx);
            request = new XHR(MainActivity.getInstace());
            request.putParameter("event", event_type);
            request.putParameter("data", event_data.toString());
            request.post(
                    "/api/owl/setappsmsoutevent.json",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.w("XHR", response);
                            MainActivity.getInstace().setCurrentSessionID("");
                        }
                    },
                    new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //alert.error("Ошибка", "Что-то пошло не так. Перезапустите приложение.");
                        }
                    }
            );
        }
        catch (JSONException e) {
            Log.w("JSONException", e);
            alert.error("Ошибка", "Что-то пошло не так. Перезапустите приложение.");
        }
    }
}