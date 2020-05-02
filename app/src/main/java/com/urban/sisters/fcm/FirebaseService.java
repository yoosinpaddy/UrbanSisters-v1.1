package com.urban.sisters.fcm;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.urban.sisters.utils.Utils;

import static com.urban.sisters.utils.Utils.FIREBASE_DEVICE_TOKEN;
import static com.urban.sisters.utils.Utils.REGISTRATION_COMPLETE;

public class FirebaseService extends FirebaseInstanceIdService {
    private static final String TAG = FirebaseService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // Saving reg id to shared preferences
        new Utils(this).writeStringPref(FIREBASE_DEVICE_TOKEN, refreshedToken);

        // sending reg id to your server
        sendRegistrationToServer(refreshedToken);

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(REGISTRATION_COMPLETE);
        registrationComplete.putExtra(FIREBASE_DEVICE_TOKEN, refreshedToken);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void sendRegistrationToServer(final String token) {
        // sending gcm token to server
        //Log.e(TAG, "sendRegistrationToServer: " + token);
    }
}
