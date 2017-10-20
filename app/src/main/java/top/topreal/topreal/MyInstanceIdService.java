package top.topreal.topreal;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class MyInstanceIdService extends FirebaseInstanceIdService {
    private XHR request;
    private String token;

    private static final String TAG = "MyFirebaseIIDService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
        token = refreshedToken;

    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        SharedPreferences prefs = MainActivity.getInstace().getPreferences(MainActivity.getInstace().MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("fcm_token", token);
        editor.commit();

        //String foo = prefs.getString("foo", "");
        //Log.w("SharedPreferences", foo);
        //request = new XHR(this);
        //request.putParameter("user", "2");
        //request.putParameter("token", token);
        //request.post("/api/fcm/settoken.json");
    }
}