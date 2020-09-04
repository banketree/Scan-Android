package com.banketree.scancore.Interface;

import android.graphics.Rect;


public interface IHandleScanDataListener {
    Boolean onHandleScanData(byte[] previewData, byte[] data, final int format, int width, int height, Rect rect);

    public Boolean isContinuity();//是否连续

    public void release();//释放
}