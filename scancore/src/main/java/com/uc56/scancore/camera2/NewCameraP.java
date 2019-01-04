package com.uc56.scancore.camera2;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.google.android.cameraview.CameraView;
import com.uc56.scancore.R;
import com.uc56.scancore.Interface.ICameraP;
import com.uc56.scancore.Interface.ICameraPreviewFrame;


public class NewCameraP implements ICameraP {
    private static final String TAG = NewCameraP.class.getCanonicalName();

    protected boolean spotAble = false;
    private Context context;
    private CameraView cameraView;
    private ICameraPreviewFrame iCameraPreviewFrame;

    public NewCameraP(Context context) {
        this.context = context;
        initView();
    }

    private void initView() {
        cameraView = new CameraView(context);
        cameraView.setId(R.id.scan_camera_preview_new);
        cameraView.addCallback(callback);
    }

    public boolean isCamera1() {
        return cameraView.isCamera1();
    }

    public static boolean isRequireCamera1() {
        return CameraView.isRequireCamera1;
    }

    public static void setRequireCamera1(boolean requireCamera1) {
        CameraView.isRequireCamera1 = requireCamera1;
    }

    @Override
    public View getCameraPreView() {
        return cameraView;
    }

    @Override
    public void startCamera() {
        cameraView.start();
    }

    @Override
    public void stopCamera() {
        cameraView.stop();
    }

    @Override
    public void startSpot() {
        spotAble = true;
//        startCamera();
    }

    @Override
    public void stopSpot() {
        spotAble = false;
//        stopCamera();
    }

    @Override
    public void openFlashlight() {
        cameraView.openFlashlight();
    }

    @Override
    public void closeFlashlight() {
        cameraView.closeFlashlight();
    }

    @Override
    public void onDestroy() {
        iCameraPreviewFrame = null;
        stopSpot();
        stopCamera();
        cameraView = null;
    }

    @Override
    public void setCameraPreviewFrame(ICameraPreviewFrame iCameraPreviewFrame) {
        this.iCameraPreviewFrame = iCameraPreviewFrame;
    }

    private CameraView.Callback callback = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data, int width, int height) {
            Log.d(TAG, "onPictureTaken " + data.length);
        }

        @Override
        public void onPreviewFrame(final byte[] data, int format, int width, int height) {
            super.onPreviewFrame(data, format, width, height);
            try {
                //过滤数据
                if (data == null || !spotAble || iCameraPreviewFrame == null) return;
                iCameraPreviewFrame.onPreviewFrame(NewCameraP.this, data, format, width, height);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
