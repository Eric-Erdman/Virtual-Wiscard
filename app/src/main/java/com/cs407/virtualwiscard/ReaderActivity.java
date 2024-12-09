package com.cs407.virtualwiscard;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReaderActivity extends AppCompatActivity {

    private static final String TAG = "NFCReceiver";

    private NfcAdapter nfcAdapter;
    private RecyclerView recyclerView;
    private LogAdapter logAdapter;
    private List<String> logList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);

        recyclerView = findViewById(R.id.recyclerView);
        logList = new ArrayList<>();
        logAdapter = new LogAdapter(logList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(logAdapter);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not supported on this device.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        enableReaderMode();
    }

    private void enableReaderMode() {
        nfcAdapter.enableReaderMode(this, tag -> {
            Log.d(TAG, "NFC Tag Detected");
            handleTag(tag);
        }, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
    }

    private void handleTag(Tag tag) {
        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep != null) {
            try {
                isoDep.connect();
                byte[] command = hexStringToByteArray("00A4040007F0010203040506"); // Example APDU command
                byte[] response = isoDep.transceive(command);

                // Convert the response to a readable string
                String responseStr = new String(response); // Converts bytes to a string
                Log.d(TAG, "Received APDU Response: " + responseStr);

                // Update the RecyclerView with the string representation
                runOnUiThread(() -> {
                    logList.add(responseStr);
                    logAdapter.notifyItemInserted(logList.size() - 1);
                });

            } catch (IOException e) {
                Log.e(TAG, "Error communicating with NFC tag", e);
            } finally {
                try {
                    isoDep.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing IsoDep", e);
                }
            }
        }
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

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }
}
