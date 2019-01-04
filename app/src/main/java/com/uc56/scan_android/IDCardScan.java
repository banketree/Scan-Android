package com.uc56.scan_android;

import android.graphics.Rect;

import com.uc56.scancore.Interface.IHandleScanDataListener;
import com.uc56.scancore.ScanView;


/**
 * Created by banketree on 2017/11/1.
 */

public class IDCardScan implements IHandleScanDataListener {
    private IIDCardResultListener listener;

    public IDCardScan(IIDCardResultListener listener) {
        this.listener = listener;
    }

    @Override
    public Boolean onHandleScanData(final byte[] previewData, byte[] data, final int format, int width, int height, Rect rect) {
        return false;
    }

    @Override
    public Boolean isContinuity() {
        return true;
    }

    @Override
    public void release() {

    }

    public interface IIDCardResultListener {
        void onScanResult(String result);
    }
}
