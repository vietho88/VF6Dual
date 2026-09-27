package com.vf6.dual.driving.car;

import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.car.app.CarContext;
import androidx.car.app.CarToast;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;

/**
 * Safe Android Auto driving companion.
 *
 * This screen intentionally does not mirror or embed arbitrary Android app UIs
 * while the vehicle is moving. It only controls the phone-side VF6 Dual session.
 */
public class DrivingScreen extends Screen {
    private static final String TARGET_PACKAGE = "com.vf6.dual";
    private static final String ACTION_COMMAND = "com.vf6.dual.action.COMMAND";
    private String lastStatus = "Ready";

    public DrivingScreen(CarContext carContext) {
        super(carContext);
    }

    @Override
    public Template onGetTemplate() {
        boolean installed = isMainInstalled();
        ItemList.Builder items = new ItemList.Builder();

        items.addItem(new Row.Builder()
                .setTitle(installed ? "VF6 Dual main app detected" : "VF6 Dual main app not installed")
                .addText(lastStatus)
                .build());

        items.addItem(new Row.Builder()
                .setTitle("Start / reconnect virtual session")
                .addText("Creates the two-app session configured on the phone")
                .setOnClickListener(() -> sendCommand("start", "START sent"))
                .build());

        items.addItem(new Row.Builder()
                .setTitle("Swap left / right")
                .addText("Restarts the virtual session with the two apps exchanged")
                .setOnClickListener(() -> sendCommand("swap", "SWAP sent"))
                .build());

        items.addItem(new Row.Builder()
                .setTitle("Stop virtual session")
                .addText("Removes the temporary virtual display")
                .setOnClickListener(() -> sendCommand("stop", "STOP sent"))
                .build());

        items.addItem(new Row.Builder()
                .setTitle("Driving mode limitation")
                .addText("For safety, arbitrary app surfaces and unrestricted touch remain parking-only")
                .build());

        return new ListTemplate.Builder()
                .setTitle("VF6 Dual · Driving")
                .setHeaderAction(Action.APP_ICON)
                .setSingleList(items.build())
                .build();
    }

    private boolean isMainInstalled() {
        try {
            getCarContext().getPackageManager().getPackageInfo(TARGET_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void sendCommand(String command, String message) {
        Intent i = new Intent(ACTION_COMMAND);
        i.setPackage(TARGET_PACKAGE);
        i.putExtra("command", command);
        try {
            getCarContext().sendBroadcast(i);
            lastStatus = message;
            CarToast.makeText(getCarContext(), message, CarToast.LENGTH_SHORT).show();
        } catch (Throwable t) {
            lastStatus = "Command failed: " + t.getClass().getSimpleName();
            CarToast.makeText(getCarContext(), lastStatus, CarToast.LENGTH_LONG).show();
        }
        invalidate();
    }
}
