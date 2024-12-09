package com.cs407.virtualwiscard;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;


public class MyHostApduService extends HostApduService {

    private static final String SELECT_APDU_HEADER = "00A40400"; // APDU command for SELECT
    private static final String RESPONSE_OK = "9000"; // Success response
    private static final String RESPONSE_ERROR = "6F00"; // Error response

    private static final String KEEP_ALIVE_APDU = "00"; // Empty APDU for keeping connection alive

    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
        Log.d("HCE", "Received APDU: " + bytesToHex(apdu));
        String apduHex = bytesToHex(apdu);

        if (apduHex.startsWith(SELECT_APDU_HEADER)) {
            // Handle the SELECT command
            String message = "Hello Bitch!";
            byte[] responseBytes = message.getBytes();
            Log.d("HCE", "APDU response: " + bytesToHex(responseBytes));
            return buildResponse(message.getBytes());
        }

        // Return error if command is not recognized
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

    private byte[] buildResponse(byte[] data) {
        return concatenateArrays(data, hexStringToByteArray(RESPONSE_OK));
    }

    private byte[] concatenateArrays(byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
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
}

