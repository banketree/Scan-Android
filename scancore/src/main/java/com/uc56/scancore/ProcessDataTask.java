package com.uc56.scancore;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;

abstract class ProcessDataTask extends AsyncTask<Void, Void, Boolean> {

    public ProcessDataTask() {
    }

    public ProcessDataTask perform() {
        if (Build.VERSION.SDK_INT >= 11) {
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            execute();
        }
        return this;
    }

    public void cancelTask() {
        if (getStatus() != Status.FINISHED) {
            cancel(true);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
