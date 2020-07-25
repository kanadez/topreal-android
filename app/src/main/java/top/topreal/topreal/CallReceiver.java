package top.topreal.topreal;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class CallReceiver extends PhonecallReceiver {
    private XHR request;
    private CustomAlert alert;

    private String event_type;
    private JSONObject event_data;

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start){
        //String output = String.format("incoming_call: %s, Start: %s", number, start.toString());
        //MainActivity.getInstace().updateTheTextView(output);

        /*String event_type = "call-in";
        JSONObject event_data = new JSONObject();

        try{
            event_data.put("number", number);
            event_data.put("start", start.getTime());
            event_data.put("end", start.getTime()+12000);
            Log.w("JSON", event_data.toString());
        }
        catch (JSONException e) {
            Log.w("JSONException", e);
            alert.error("Ошибка", "Что-то пошло не так. Перезапустите приложение.");
        }

        alert = new CustomAlert(ctx);
        request = new XHR(ctx);
        request.putParameter("event", event_type);
        request.putParameter("data", event_data.toString());
        request.post(
                "/api/owl/setappevent.json",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.w("XHR", response);
                    }
                },
                new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        alert.error("Ошибка", "Что-то пошло не так. Перезапустите приложение.");
                    }
                }
        );*/

    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start){
        //String output = String.format("Incoming Call Answered: %s, Start: %s", number, start.toString());
        //MainActivity.getInstace().updateTheTextView(output);
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end){
        //String output = String.format("Incoming Call Ended: %s, Start: %s, End: %s", number, start.toString(), end.toString());
        //MainActivity.getInstace().updateTheTextView(output);

        String event_type = "call-in";
        JSONObject event_data = new JSONObject();

        try{
            event_data.put("number", number);
            event_data.put("start", start.getTime());
            event_data.put("end", end.getTime());
            Log.w("JSON", event_data.toString());
        }
        catch (JSONException e) {
            Log.w("JSONException", e);
            alert.error("Ошибка", "Что-то пошло не так. Перезапустите приложение.");
        }

        alert = new CustomAlert(ctx);
        request = new XHR(ctx);
        request.putParameter("event", event_type);
        request.putParameter("data", event_data.toString());
        request.post(
                "/api/owl/setappcallinevent.json",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.w("XHR", response);
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

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start){
        //String output = String.format("Outgoing Call: %s, Start: %s", number, start.toString());
        //MainActivity.getInstace().updateTheTextView(output);
        /*event_type = "call-out";

        try {
            event_data = new JSONObject();
            event_data.put("number", number);
            event_data.put("start", start.getTime());
            event_data.put("end", start.getTime()+13000);
            Log.w("JSON", event_data.toString());

            alert = new CustomAlert(ctx);
            request = new XHR(ctx);
            request.putParameter("event", event_type);
            request.putParameter("data", event_data.toString());
            request.post(
                    "/api/owl/setappevent.json",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.w("XHR", response);
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
        catch (JSONException e) {
            Log.w("JSONException", e);
            alert.error("Ошибка", "Что-то пошло не так. Перезапустите приложение.");
        }*/
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end){
        //String output = String.format("Outgoing Call: %s, Start: %s, End: %s", number, start.toString(), end.toString());
        //MainActivity.getInstace().updateTheTextView(output);
        event_type = "call-out";

        try {
            event_data = new JSONObject();
            event_data.put("number", number);
            event_data.put("start", start.getTime());
            event_data.put("end", end.getTime());
            event_data.put("session", MainActivity.getInstace().getCurrentSessionID());
            Log.w("JSON", event_data.toString());

            //alert = new CustomAlert(ctx);
            request = new XHR(ctx);
            request.putParameter("event", event_type);
            request.putParameter("data", event_data.toString());
            request.post(
                    "/api/owl/setappcalloutevent.json",
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

    @Override
    protected void onMissedCall(Context ctx, String number, Date start){
        //
    }

}
