package top.topreal.topreal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by x on 08.10.2017.
 */

public class SmsListener extends BroadcastReceiver{
    private SharedPreferences preferences;
    private XHR request;
    private CustomAlert alert;

    private String event_type;
    private JSONObject event_data;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
            SmsMessage[] msgs = null;

            if (bundle != null){
                //---retrieve the SMS message received---
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];

                    for (int i=0; i<msgs.length; i++){
                        String msg_from;
                        String msgBody;
                        Long time;

                        if (Build.VERSION.SDK_INT < 23){
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                            msg_from = msgs[i].getOriginatingAddress();
                            msgBody = msgs[i].getMessageBody();
                            time = msgs[i].getTimestampMillis();
                        }
                        else{
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], "3gpp");
                            msg_from = msgs[i].getOriginatingAddress();
                            msgBody = msgs[i].getMessageBody();
                            time = msgs[i].getTimestampMillis();
                        }

                        String event_type = "sms-in";
                        JSONObject event_data = new JSONObject();

                        try{
                            event_data.put("number", msg_from);
                            event_data.put("content", msgBody);
                            event_data.put("time", time);
                            Log.w("JSON", event_data.toString());
                        }
                        catch (JSONException e) {
                            Log.w("JSONException", e);
                            alert.error("Ошибка", "Что-то пошло не так. Перезапустите приложение.");
                        }

                        alert = new CustomAlert(context);
                        request = new XHR(context);
                        request.putParameter("event", event_type);
                        request.putParameter("data", event_data.toString());
                        request.post(
                                "/api/owl/setappsmsinevent.json",
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
                }
                catch(Exception e){
//                  Log.d("Exception caught",e.getMessage());
                }
            }
        }
    }
}
