package com.uc56.scancore.camera;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.uc56.scancore.Interface.ICameraP;
import com.uc56.scancore.Interface.ICameraPreviewFrame;
import com.uc56.scancore.R;


public class OldCameraP implements ICameraP, Camera.PreviewCallback {
    protected Camera camera;
    protected CameraPreview cameraPreview;

    protected Handler handler;
    protected boolean spotAble = false;
    private Context context;
    private ICameraPreviewFrame iCameraPreviewFrame;

    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    public OldCameraP(Context context) {
        this.context = context;
        handler = new Handler();
        initView();
    }

    private void initView() {
        cameraPreview = new CameraPreview(context);
        cameraPreview.setId(R.id.scan_camera_preview);
    }

    @Override
    public View getCameraPreView() {
        return cameraPreview;
    }

    /**
     * 打开后置摄像头开始预览，但是并未开始识别
     */
    @Override
    public void startCamera() {
        startCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    /**
     * 打开指定摄像头开始预览，但是并未开始识别
     *
     * @param cameraFacing
     */
    private void startCamera(int cameraFacing) {
        if (camera != null) {
            return;
        }
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++) {
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.facing == cameraFacing) {
                startCameraById(cameraId);
                break;
            }
        }
    }

    private void startCameraById(int cameraId) {
        try {
            this.cameraId = cameraId;
            camera = Camera.open(cameraId);
            cameraPreview.setTag(cameraId);
            cameraPreview.setCamera(camera);
        } catch (Exception e) {
            Toast.makeText(context, "打开相机出错", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 关闭摄像头预览，并且隐藏扫描框
     */
    @Override
    public void stopCamera() {
        try {
            if (camera != null) {
                cameraPreview.stopCameraPreview();
                cameraPreview.setCamera(null);
                camera.release();
            }
            camera = null;
        } catch (Exception e) {
        }
    }

    /**
     * 延迟0.5秒后开始识别
     */
    @Override
    public void startSpot() {
        startSpotDelay(500);
    }

    /**
     * 延迟delay毫秒后开始识别
     *
     * @param delay
     */
    public void startSpotDelay(int delay) {
        spotAble = true;

        try {
            startCamera();
            // 开始前先移除之前的任务
            if (mOneShotPreviewCallbackTask != null)
                handler.removeCallbacks(mOneShotPreviewCallbackTask);
            handler.postDelayed(mOneShotPreviewCallbackTask, delay);
        } catch (Exception e) {
        }
    }

    /**
     * 停止识别
     */
    @Override
    public void stopSpot() {
        spotAble = false;

        if (camera != null) {
            try {
                camera.setOneShotPreviewCallback(null);
            } catch (Exception e) {
            }
        }
        if (handler != null) {
            handler.removeCallbacks(mOneShotPreviewCallbackTask);
        }
    }

    /**
     * 打开闪光灯
     */
    @Override
    public void openFlashlight() {
        cameraPreview.openFlashlight();
    }

    /**
     * 关闭散光灯
     */
    @Override
    public void closeFlashlight() {
        cameraPreview.closeFlashlight();
    }

    /**
     * 销毁二维码扫描控件
     */
    @Override
    public void onDestroy() {
        stopCamera();
        handler = null;
        mOneShotPreviewCallbackTask = null;
        iCameraPreviewFrame = null;
    }

    @Override
    public void setCameraPreviewFrame(ICameraPreviewFrame iCameraPreviewFrame) {
        this.iCameraPreviewFrame = iCameraPreviewFrame;
    }

    @Override
    public void onPreviewFrame(final byte[] previewData, final Camera camera) {
        try {
            if (!spotAble || camera == null || camera.getParameters() == null || iCameraPreviewFrame == null)
                return;
            final Camera.Parameters parameters = camera.getParameters();
            final Camera.Size size = parameters.getPreviewSize();
            iCameraPreviewFrame.onPreviewFrame(this, previewData, parameters.getPreviewFormat(), size.width, size.height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void continueShot() {
        handler.post(mOneShotPreviewCallbackTask);
    }

    private Runnable mOneShotPreviewCallbackTask = new Runnable() {
        @Override
        public void run() {
            if (camera != null && spotAble) {
                try {
                    camera.setOneShotPreviewCallback(OldCameraP.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
}