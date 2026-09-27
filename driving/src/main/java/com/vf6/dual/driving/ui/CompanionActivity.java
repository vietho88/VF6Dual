package com.vf6.dual.driving.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.vf6.dual.driving.R;

public class CompanionActivity extends Activity {
    private static final String TARGET_PACKAGE = "com.vf6.dual";
    private static final String ACTION_COMMAND = "com.vf6.dual.action.COMMAND";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_companion);

        Button start = findViewById(R.id.test_start);
        Button swap = findViewById(R.id.test_swap);
        Button stop = findViewById(R.id.test_stop);
        start.setOnClickListener(v -> send("start"));
        swap.setOnClickListener(v -> send("swap"));
        stop.setOnClickListener(v -> send("stop"));
    }

    private void send(String command) {
        Intent i = new Intent(ACTION_COMMAND);
        i.setPackage(TARGET_PACKAGE);
        i.putExtra("command", command);
        try {
            sendBroadcast(i);
            Toast.makeText(this, "Sent: " + command, Toast.LENGTH_SHORT).show();
        } catch (Throwable t) {
            Toast.makeText(this, "Command failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
