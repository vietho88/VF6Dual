package com.vf6.dual.ui;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.vf6.dual.R;
import com.vf6.dual.bridge.IVF6PrivilegedService;
import com.vf6.dual.bridge.ShizukuBridge;
import com.vf6.dual.session.SessionController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CarActivity extends Activity implements SurfaceHolder.Callback {
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private SurfaceView surface;
    private TextView status;
    private IVF6PrivilegedService service;
    private SurfaceControl mirrorControl;
    private Bundle mirrorBundle;
    private int displayId = -1;
    private float downX, downY;
    private long downAt;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        surface = findViewById(R.id.display_surface);
        status = findViewById(R.id.car_status);
        Button back = findViewById(R.id.car_back);
        Button home = findViewById(R.id.car_home);
        Button swap = findViewById(R.id.car_swap);
        Button stop = findViewById(R.id.car_stop);
        surface.getHolder().addCallback(this);
        surface.setOnTouchListener(this::onSurfaceTouch);

        back.setOnClickListener(v -> sendKey(4));
        home.setOnClickListener(v -> sendKey(3));
        swap.setOnClickListener(v -> {
            if (service == null) return;
            setStatus("Đang đổi 2 app…");
            io.execute(() -> {
                try {
                    SessionController.swap(this, service, this::setStatus);
                    displayId = SessionController.getActiveDisplayId(this);
                    main.post(this::attachMirrorIfPossible);
                } catch (Throwable t) { setStatus("Swap lỗi: " + t.getMessage()); }
            });
        });
        stop.setOnClickListener(v -> {
            if (service == null) return;
            io.execute(() -> {
                try {
                    detachMirror();
                    SessionController.stop(this, service, this::setStatus);
                } catch (Throwable t) { setStatus("Stop lỗi: " + t.getMessage()); }
            });
        });

        bindAndStart();
    }

    private void bindAndStart() {
        ShizukuBridge.withService(this, new ShizukuBridge.Callback() {
            @Override public void onReady(IVF6PrivilegedService s) {
                service = s;
                io.execute(() -> {
                    try {
                        displayId = SessionController.start(CarActivity.this, s, CarActivity.this::setStatus);
                        setStatus("VF6 Dual • display " + displayId);
                        main.post(CarActivity.this::attachMirrorIfPossible);
                    } catch (Throwable t) {
                        setStatus("Không start được: " + t.getMessage());
                    }
                });
            }
            @Override public void onError(String message) { setStatus(message + " • mở app trên điện thoại để cấp quyền"); }
        });
    }

    @Override public void surfaceCreated(SurfaceHolder holder) { attachMirrorIfPossible(); }
    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { attachMirrorIfPossible(); }
    @Override public void surfaceDestroyed(SurfaceHolder holder) { detachMirror(); }

    private void attachMirrorIfPossible() {
        if (service == null || displayId < 0 || surface.getWidth() <= 0 || surface.getHeight() <= 0) return;
        io.execute(() -> {
            try {
                detachMirror();
                Bundle b = service.mirrorDisplay(displayId);
                b.setClassLoader(SurfaceControl.class.getClassLoader());
                String error = b.getString("error");
                if (error != null) throw new IllegalStateException(error);
                Parcelable parcelable = b.getParcelable("surface_control");
                if (!(parcelable instanceof SurfaceControl)) throw new IllegalStateException("Không nhận được SurfaceControl mirror");
                SurfaceControl mirror = (SurfaceControl) parcelable;
                main.post(() -> {
                    try {
                        SurfaceControl parent = surface.getSurfaceControl();
                        if (parent == null || !parent.isValid()) throw new IllegalStateException("SurfaceView chưa sẵn sàng");
                        int srcW = SessionController.getWidth(this);
                        int srcH = SessionController.getHeight(this);
                        Rect src = new Rect(0, 0, srcW, srcH);
                        Rect dst = new Rect(0, 0, surface.getWidth(), surface.getHeight());
                        SurfaceControl.Transaction tx = new SurfaceControl.Transaction();
                        tx.reparent(mirror, parent)
                                .setGeometry(mirror, src, dst, Surface.ROTATION_0)
                                .setLayer(mirror, 1)
                                .setVisibility(mirror, true)
                                .apply();
                        tx.close();
                        mirrorBundle = b;
                        mirrorControl = mirror;
                        setStatus("VF6 Dual • chạm trực tiếp • " + srcW + "×" + srcH);
                    } catch (Throwable t) {
                        setStatus("Mirror attach lỗi: " + t.getMessage());
                    }
                });
            } catch (Throwable t) {
                setStatus("Mirror lỗi: " + t.getMessage());
            }
        });
    }

    private boolean onSurfaceTouch(View v, MotionEvent e) {
        if (service == null || displayId < 0) return true;
        float sx = SessionController.getWidth(this) / (float) Math.max(1, surface.getWidth());
        float sy = SessionController.getHeight(this) / (float) Math.max(1, surface.getHeight());
        if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
            downX = e.getX() * sx;
            downY = e.getY() * sy;
            downAt = System.currentTimeMillis();
            return true;
        }
        if (e.getActionMasked() == MotionEvent.ACTION_UP) {
            float x = e.getX() * sx, y = e.getY() * sy;
            long d = System.currentTimeMillis() - downAt;
            float dx = x - downX, dy = y - downY;
            io.execute(() -> {
                try {
                    if (dx * dx + dy * dy < 18 * 18) SessionController.tap(service, displayId, x, y);
                    else SessionController.swipe(service, displayId, downX, downY, x, y, d);
                } catch (Throwable t) { setStatus("Touch lỗi: " + t.getMessage()); }
            });
            return true;
        }
        return true;
    }

    private void sendKey(int keyCode) {
        if (service == null || displayId < 0) return;
        io.execute(() -> {
            try { SessionController.key(service, displayId, keyCode); }
            catch (Throwable t) { setStatus("Key lỗi: " + t.getMessage()); }
        });
    }

    private void detachMirror() {
        SurfaceControl m = mirrorControl;
        Bundle b = mirrorBundle;
        mirrorControl = null;
        mirrorBundle = null;
        if (m != null) {
            main.post(() -> {
                try {
                    SurfaceControl.Transaction tx = new SurfaceControl.Transaction();
                    tx.reparent(m, null).apply();
                    tx.close();
                    m.release();
                } catch (Throwable ignored) {}
            });
        }
        if (service != null && b != null) {
            try { service.releaseMirror(b); } catch (Throwable ignored) {}
        }
    }

    private void setStatus(String text) { main.post(() -> status.setText(text)); }

    @Override protected void onDestroy() {
        detachMirror();
        io.shutdownNow();
        super.onDestroy();
    }
}
