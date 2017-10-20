package top.topreal.topreal;

import android.content.Context;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by x on 29.09.2017.
 */

public class XHR {
    private RequestQueue MyRequestQueue;
    private String hostname;
    private Map<String, String> data;

    public XHR(Context ctx){
        MyRequestQueue = Volley.newRequestQueue(ctx);
        hostname = AppSettings.production ? AppSettings.prod_host : AppSettings.dev_host;
        data = new HashMap<String, String>();
    }

    public void putParameter(String key, String value){
        data.put(key, value);
    }

    public void post(String url, Response.Listener<String> listener, Response.ErrorListener errorListener){
        String full_url = hostname.concat(url);
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, full_url, listener, errorListener){
            protected Map<String, String> getParams() {
                return data;
            }
        };

        MyRequestQueue.add(MyStringRequest);
    }

    /* Success response listener
    new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {

        }
    },
    new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
        @Override
        public void onErrorResponse(VolleyError error) {

        }
    }
     */
}
