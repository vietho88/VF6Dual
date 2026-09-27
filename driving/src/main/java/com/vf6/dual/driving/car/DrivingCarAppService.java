package com.vf6.dual.driving.car;

import androidx.car.app.CarAppService;
import androidx.car.app.Session;
import androidx.car.app.validation.HostValidator;

public class DrivingCarAppService extends CarAppService {
    @Override
    public HostValidator createHostValidator() {
        // Development-only build. A production car app should validate known hosts.
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR;
    }

    @Override
    public Session onCreateSession() {
        return new DrivingSession();
    }
}
