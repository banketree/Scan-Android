package com.uc56.scancore.Interface;

import android.view.View;


public interface ICameraP {

    public View getCameraPreView();

    /**
     * 打开后置摄像头开始预览，但是并未开始识别
     */
    public void startCamera();

    /**
     * 关闭摄像头预览，并且隐藏扫描框
     */
    public void stopCamera();

    /**
     * 延迟0.5秒后开始识别
     */
    public void startSpot();


    /**
     * 停止识别
     */
    public void stopSpot();

    /**
     * 打开闪光灯
     */
    public void openFlashlight();

    /**
     * 关闭散光灯
     */
    public void closeFlashlight();

    /**
     * 销毁二维码扫描控件
     */
    public void onDestroy();

    /**
     * 预览回调
     */
    public void setCameraPreviewFrame(ICameraPreviewFrame iCameraPreviewFrame);
}