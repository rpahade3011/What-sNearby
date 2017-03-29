package com.nearby.whatsnearby.firebase;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by rudhraksh.pahade on 8/29/2016.
 */

public class FireBaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "FireBaseIIDService";
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */

    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        try {
            // Get updated InstanceID token.
            String refreshToken = FirebaseInstanceId.getInstance().getToken();
            if (refreshToken != null) {
                Log.e(TAG, "Refreshed token: " + refreshToken);
            } else {
                Log.e(TAG, "ERROR GETTING TOKEN FROM FIREBASE ");
            }
            sendRegistrationToServer(refreshToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // [END refresh_token]

    private void sendRegistrationToServer(final String updatedToken) {
        // Getting old FCM ID
    }
}
