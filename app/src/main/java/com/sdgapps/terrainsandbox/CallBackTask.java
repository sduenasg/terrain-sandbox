package com.sdgapps.terrainsandbox;

public class CallBackTask implements Runnable {

    private final Runnable task;

    private final Callback callback;

    CallBackTask(Runnable task, Callback callback) {
        this.task = task;
        this.callback = callback;
    }

    public void run() {
        task.run();
        callback.onCallbackTaskFinished();
    }
}