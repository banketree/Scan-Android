package com.banketree.scancore.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.SurfaceHolder;


public class CameraPreview extends CameraZoomPreview implements SurfaceHolder.Callback {
    private static final String TAG = CameraPreview.class.getSimpleName();
    private boolean mPreviewing = true;
    private boolean mSurfaceCreated = false;
    private CameraConfigurationManager mCameraConfigurationManager;
    private CountDownTimer countDownTimer;

    public CameraPreview(Context context) {
        super(context);
    }

    @Override
    public void setCamera(Camera camera) {
        super.setCamera(camera);

        if (mCamera != null) {
            mCameraConfigurationManager = new CameraConfigurationManager(getContext());
            mCameraConfigurationManager.initFromCameraParameters(mCamera);

            getHolder().addCallback(this);
            if (mPreviewing) {
                requestLayout();
            } else {
                showCameraPreview();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (surfaceHolder.getSurface() == null) {
            return;
        }
        stopCameraPreview();
        showCameraPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mSurfaceCreated = false;
        stopCameraPreview();
    }

    public void showCameraPreview() {
        if (mCamera != null) {
            post(new Runnable() {
                public void run() {
                    try {
                        mPreviewing = true;
                        mCamera.setPreviewDisplay(getHolder());
                        mCameraConfigurationManager.setDesiredCameraParameters(mCamera);
                        mCamera.startPreview();
                        mSurfaceCreated = true;
                        mCamera.autoFocus(autoFocusCB);
                        startAutoFocusTimer();
                    } catch (Exception e) {
                        Log.e(TAG, e.toString(), e);
                    }
                }
            });
        }
    }

    public void stopCameraPreview() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        mPreviewing = false;
        try {
            if (doAutoFocus != null)
                removeCallbacks(doAutoFocus);
        } catch (Exception e) {
        }

        if (mCamera != null) {
            try {
                mCamera.cancelAutoFocus();
                mCamera.setOneShotPreviewCallback(null);
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
        }
    }

    public void openFlashlight() {
        if (flashLightAvailable()) {
            mCameraConfigurationManager.openFlashlight(mCamera);
        }
    }

    public void closeFlashlight() {
        if (flashLightAvailable()) {
            mCameraConfigurationManager.closeFlashlight(mCamera);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        if (mCameraConfigurationManager != null && mCameraConfigurationManager.getCameraResolution() != null) {
            Point cameraResolution = mCameraConfigurationManager.getCameraResolution();
            // 取出来的cameraResolution高宽值与屏幕的高宽顺序是相反的
            int cameraPreviewWidth = cameraResolution.x;
            int cameraPreviewHeight = cameraResolution.y;
            if (width * 1f / height < cameraPreviewWidth * 1f / cameraPreviewHeight) {
                float ratio = cameraPreviewHeight * 1f / cameraPreviewWidth;
                width = (int) (height / ratio + 0.5f);
            } else {
                float ratio = cameraPreviewWidth * 1f / cameraPreviewHeight;
                height = (int) (width / ratio + 0.5f);
            }
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }


    private boolean flashLightAvailable() {
        return mCamera != null && mPreviewing && mSurfaceCreated && getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (mCamera != null && mPreviewing && mSurfaceCreated) {
                try {
                    mCamera.autoFocus(autoFocusCB);
                } catch (Exception e) {
                }
            }
        }
    };

    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                postDelayed(doAutoFocus, 1000L);
            } else {
                postDelayed(doAutoFocus, 500L);
            }
        }
    };

    private long interVal = 1500L;//延迟3.5秒

    private void startAutoFocusTimer() {
        if (countDownTimer != null)
            countDownTimer.cancel();
        countDownTimer = null;
        countDownTimer = new CountDownTimer(Integer.MAX_VALUE, interVal) {
            @Override
            public void onTick(long millisUntilFinished) {
                post(doAutoFocus);
            }

            public void onFinish() {
            }
        };
        countDownTimer.start();
    }
}