package com.cs407.virtualwiscard;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.cardemulation.HostApduService;
import com.google.firebase.firestore.FirebaseFirestore;
import android.os.Bundle;
import android.util.Log;

public class MyHostApduService extends HostApduService {

    private static final String SELECT_APDU_HEADER = "00A40400";
    private static final String RESPONSE_ERROR = "6F00";

    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
        Log.d("HCE", "Received APDU: " + bytesToHex(apdu));
        String apduHex = bytesToHex(apdu);

        if (apduHex.startsWith(SELECT_APDU_HEADER)) {
            Log.d("HCE", "AID selected");

            //check user permissions
            boolean userHasAccess = checkCachedUserAccess();

            String response = "";
            if (userHasAccess) {
                Log.d("HCE", "Permission granted: PASS");
                response = "PASS"; // User has permission
            } else {
                Log.d("HCE", "Permission denied: FAIL");
                response = "FAIL"; // User does not have permission
            }

            byte[] responseBytes = response.getBytes();
            Log.d("HCE", "APDU response: " + bytesToHex(responseBytes));
            return responseBytes;
        }

        //return if error
        return hexStringToByteArray(RESPONSE_ERROR);
    }

    @Override
    public void onDeactivated(int reason) {
        Log.d("HCE", "onDeactivated called with reason: " + reason);
        switch (reason) {
            case DEACTIVATION_LINK_LOSS:
                Log.d("HCE", "Deactivation due to link loss.");
                break;
            case DEACTIVATION_DESELECTED:
                Log.d("HCE", "Deactivation due to deselection.");
                break;
            default:
                Log.d("HCE", "Deactivation for unknown reason.");
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public interface Callback {
        void onComplete(boolean success);
    }

    public static void updateUserAccessCache(Context context, String username, Callback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users").document(username)
                .get()
                .addOnSuccessListener(document -> {
                    boolean hasAccess = document != null && document.getBoolean("hasAccess") != null && document.getBoolean("hasAccess");
                    Log.d("MyHostApduService", "Access fetched for " + username + ": " + hasAccess);
                    SharedPreferences sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    sharedPreferences.edit().putBoolean("has_access", hasAccess).apply();
                    callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("MyHostApduService", "Failed to fetch access for " + username, e);
                    callback.onComplete(false);
                });
    }

    public boolean checkCachedUserAccess() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        boolean hasAccess = sharedPreferences.getBoolean("has_access", false);
        Log.d("MyHostApduService", "Cached user access: " + hasAccess);
        return hasAccess;
    }
}
