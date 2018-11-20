package com.sdgapps.terrainsandbox.MiniEngine;

import android.os.SystemClock;

import com.sdgapps.terrainsandbox.MVP.MainViewMvp;

public class TimeSystem {
    private long frameStartTime;
    public long deltaTime;
    private long mil_act = 0;
    private long snapshot_time = SystemClock.uptimeMillis();
    private int fps;
    private int lastSecondFPS = 0;
    private int lastSecondDrawcalls = 0;
    private static final float defaultframetime = 16f;
    public static final float defaultframetimeInverted = 1f / defaultframetime;
    public int drawcalls = 0;

    private MainViewMvp.MainViewMvpListener presenter;

    public void tickStart() {
        frameStartTime = SystemClock.uptimeMillis();
    }

    public void tickEnd() {
        long currentEndTime = SystemClock.uptimeMillis();
        deltaTime = currentEndTime - this.frameStartTime;
        frameStartTime = SystemClock.uptimeMillis();
    }

    public long getCurrentDeltaTime() {
        return SystemClock.uptimeMillis() - this.frameStartTime;
    }

    public void update() {
        mil_act = (int) (SystemClock.uptimeMillis() - snapshot_time);
        if (mil_act >= 1000) {
            if (lastSecondFPS != fps) {
                lastSecondFPS = fps;//fps value
                presenter.onUpdateFps(fps);
            }

            if (lastSecondDrawcalls != drawcalls) {
                lastSecondDrawcalls = drawcalls;//fps value
                presenter.onUpdateDrawcalls((int) Math.ceil(drawcalls / fps));
            }

            fps = 0;
            drawcalls = 0;
            snapshot_time = SystemClock.uptimeMillis();
            mil_act = mil_act - 1000;
        }
        fps++;
    }

    public void setPresenter(MainViewMvp.MainViewMvpListener _presenter) {
        this.presenter = _presenter;
    }
}
