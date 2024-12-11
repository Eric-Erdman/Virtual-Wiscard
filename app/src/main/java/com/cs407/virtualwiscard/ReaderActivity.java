package com.cs407.virtualwiscard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
    private View greenOverlay;
    private TextView accessMessage;
    private View redOverlay;
    private TextView failureMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);

        //initiate recylcler stuff
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

        greenOverlay = findViewById(R.id.green_overlay);
        accessMessage = findViewById(R.id.access_message);
        redOverlay = findViewById(R.id.red_overlay);
        failureMessage = findViewById(R.id.failure_message);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

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
                byte[] command = hexStringToByteArray("00A4040007F0010203040506");
                byte[] response = isoDep.transceive(command);

                //convert to readable string
                String responseStr = new String(response).trim();
                Log.d(TAG, "Received APDU Response: " + responseStr);

                //update based on pass fail access rights
                runOnUiThread(() -> {
                    logList.add(responseStr);
                    logAdapter.notifyItemInserted(logList.size() - 1);

                    if ("PASS".equals(responseStr)) {
                        showGreenOverlay();
                    } else {
                        showRedOverlay();
                    }
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

    private void showGreenOverlay() {
        accessMessage.setText("Access Granted");
        greenOverlay.setVisibility(View.VISIBLE);
        greenOverlay.bringToFront();


        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(greenOverlay, "alpha", 1f, 0f);
        fadeOut.setDuration(3000);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                greenOverlay.setVisibility(View.GONE);
                greenOverlay.setAlpha(1f);
            }
        });

        fadeOut.start();
    }

    private void showRedOverlay() {
        failureMessage.setText("Access Denied");
        redOverlay.setVisibility(View.VISIBLE);
        redOverlay.bringToFront();


        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(redOverlay, "alpha", 1f, 0f);
        fadeOut.setDuration(3000);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                redOverlay.setVisibility(View.GONE);
                redOverlay.setAlpha(1f);
            }
        });

        fadeOut.start();
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
