package com.vf6.dual.bridge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vf6.dual.session.SessionController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Receives low-distraction control commands from the separate Android Auto
 * driving companion. The receiver does not render arbitrary app content on
 * the driving screen; it only starts/stops/swaps the phone-side virtual session.
 */
public class CommandReceiver extends BroadcastReceiver {
    public static final String ACTION_COMMAND = "com.vf6.dual.action.COMMAND";
    public static final String EXTRA_COMMAND = "command";
    public static final String CMD_START = "start";
    public static final String CMD_SWAP = "swap";
    public static final String CMD_STOP = "stop";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_COMMAND.equals(intent.getAction())) return;
        String command = intent.getStringExtra(EXTRA_COMMAND);
        if (command == null) return;

        PendingResult pending = goAsync();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ShizukuBridge.withService(context.getApplicationContext(), new ShizukuBridge.Callback() {
            @Override
            public void onReady(IVF6PrivilegedService service) {
                executor.execute(() -> {
                    try {
                        if (CMD_START.equals(command)) {
                            SessionController.start(context, service, m -> {});
                        } else if (CMD_SWAP.equals(command)) {
                            SessionController.swap(context, service, m -> {});
                        } else if (CMD_STOP.equals(command)) {
                            SessionController.stop(context, service, m -> {});
                        }
                    } catch (Throwable ignored) {
                    } finally {
                        executor.shutdown();
                        pending.finish();
                    }
                });
            }

            @Override
            public void onError(String message) {
                executor.shutdown();
                pending.finish();
            }
        });
    }
}
