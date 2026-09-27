package com.vf6.dual.driving.car;

import android.content.Intent;

import androidx.car.app.Screen;
import androidx.car.app.Session;

public class DrivingSession extends Session {
    @Override
    public Screen onCreateScreen(Intent intent) {
        return new DrivingScreen(getCarContext());
    }
}
