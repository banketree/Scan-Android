package com.uc56.scancore;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.uc56.scancore.camera.CameraPreview;

import java.util.Queue;
import java.util.concurrent.*;

import me.dm7.barcodescanner.core.DisplayUtils;


public class ScanView extends RelativeLayout implements Camera.PreviewCallback {
    protected Camera mCamera;
    protected CameraPreview mPreview;
    protected ScanBoxView mBoxView;

    private Queue<IHandleScanDataListener> handleScanDataListenerQueue = new ConcurrentLinkedQueue<IHandleScanDataListener>();

    protected Handler mHandler;
    protected boolean mSpotAble = false;
    protected Thread mProcessDataTask;
    private int mOrientation;
    RelativeLayout.LayoutParams layoutParams;
    private Context context;

    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    public ScanView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHandler = new Handler();
        initView(context, attrs);
    }


    private void initView(Context context, AttributeSet attrs) {
        this.context = context;
        mPreview = new CameraPreview(getContext());
        mPreview.setId(R.id.scan_camera_preview);
        addView(mPreview);
        mOrientation = ScanUtil.getOrientation(context);

        layoutParams = new RelativeLayout.LayoutParams(context, attrs);
        layoutParams.addRule(RelativeLayout.ALIGN_TOP, mPreview.getId());
        layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, mPreview.getId());
    }

    public void addScanBoxView(View view) {
        if (mBoxView != null) {
            removeView(mBoxView);
            mBoxView.setVisibility(GONE);
            mBoxView = null;
        }
        if (view instanceof ScanBoxView) {
        } else {
            return;
        }
        mBoxView = (ScanBoxView) view;
        addView(mBoxView, layoutParams);
        mBoxView.setVisibility(VISIBLE);
    }

    public void removeScanBoxView() {
        if (mBoxView != null) {
            removeView(mBoxView);
            mBoxView.setVisibility(GONE);
        }
        mBoxView = null;
    }

    private synchronized Queue<IHandleScanDataListener> getHandleScanDataListenerQueque() {
        return handleScanDataListenerQueue;
    }

    /**
     * 添加扫描二维码的处理
     *
     * @param listener 扫描二维码的处理
     */
    public void addHandleScanDataListener(IHandleScanDataListener listener) {
        if (listener == null)
            return;
        if (!getHandleScanDataListenerQueque().isEmpty()) {
            for (IHandleScanDataListener itemListener : getHandleScanDataListenerQueque()) {
                if (itemListener == listener)
                    return;
            }
        }
        getHandleScanDataListenerQueque().add(listener);
    }

    /**
     * 删除扫描二维码的处理
     *
     * @param listener 扫描二维码的处理
     */
    public void removeHandleScanDataListener(IHandleScanDataListener listener) {
        if (listener == null)
            return;
        if (!getHandleScanDataListenerQueque().isEmpty()) {
            for (IHandleScanDataListener itemListener : getHandleScanDataListenerQueque()) {
                if (itemListener == listener) {
                    getHandleScanDataListenerQueque().remove(listener);
                    listener.release();
                    return;
                }
            }
        }
    }

    public void removeHandleScanDataListenerAll() {
        if (!getHandleScanDataListenerQueque().isEmpty()) {
            for (IHandleScanDataListener itemListener : getHandleScanDataListenerQueque()) {
                try {
                    getHandleScanDataListenerQueque().remove(itemListener);
                    itemListener.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ScanBoxView getScanBoxView() {
        return mBoxView;
    }

    /**
     * 显示扫描框
     */
    public void showScanRect() {
        if (mBoxView != null) {
            mBoxView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏扫描框
     */
    public void hiddenScanRect() {
        if (mBoxView != null) {
            mBoxView.setVisibility(View.GONE);
        }
    }

    /**
     * 打开后置摄像头开始预览，但是并未开始识别
     */
    public void startCamera() {
        startCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    /**
     * 打开指定摄像头开始预览，但是并未开始识别
     *
     * @param cameraFacing
     */
    public void startCamera(int cameraFacing) {
        if (mCamera != null) {
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

    public CameraPreview getCameraPreView() {
        return mPreview;
    }

    private void startCameraById(int cameraId) {
        try {
            this.cameraId = cameraId;
            mCamera = Camera.open(cameraId);
            mPreview.setTag(cameraId);
            mPreview.setCamera(mCamera);
        } catch (Exception e) {
            Toast.makeText(getContext(), "打开相机出错", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 关闭摄像头预览，并且隐藏扫描框
     */
    public void stopCamera() {
        try {
            stopSpotAndHiddenRect();
            if (mCamera != null) {
                mPreview.stopCameraPreview();
                mPreview.setCamera(null);
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
        }
    }

    /**
     * 延迟0.5秒后开始识别
     */
    public void startSpot() {
        startSpotDelay(500);
    }

    /**
     * 延迟delay毫秒后开始识别
     *
     * @param delay
     */
    public void startSpotDelay(int delay) {
        mSpotAble = true;

        try {
            startCamera();
            // 开始前先移除之前的任务
            if (mOneShotPreviewCallbackTask != null)
                mHandler.removeCallbacks(mOneShotPreviewCallbackTask);
            mHandler.postDelayed(mOneShotPreviewCallbackTask, delay);
        } catch (Exception e) {
        }
    }

    /**
     * 停止识别
     */
    public void stopSpot() {
        cancelProcessDataTask();

        mSpotAble = false;

        if (mCamera != null) {
            try {
                mCamera.setOneShotPreviewCallback(null);
            } catch (Exception e) {
            }
        }
        if (mHandler != null) {
            mHandler.removeCallbacks(mOneShotPreviewCallbackTask);
        }
    }

    /**
     * 停止识别，并且隐藏扫描框
     */
    public void stopSpotAndHiddenRect() {
        try {
            stopSpot();
            hiddenScanRect();
        } catch (Exception e) {
        }
    }

    /**
     * 显示扫描框，并且延迟1.5秒后开始识别
     */
    public void startSpotAndShowRect() {
        try {
            startSpot();
            showScanRect();
        } catch (Exception e) {
        }
    }

    /**
     * 打开闪光灯
     */
    public void openFlashlight() {
        mPreview.openFlashlight();
    }

    /**
     * 关闭散光灯
     */
    public void closeFlashlight() {
        mPreview.closeFlashlight();
    }

    /**
     * 销毁二维码扫描控件
     */
    public void onDestroy() {
        stopCamera();
        mHandler = null;
        mOneShotPreviewCallbackTask = null;
    }

    /**
     * 取消数据处理任务
     */
    protected void cancelProcessDataTask() {
        if (mProcessDataTask != null) {
            try {
                mProcessDataTask.stop();
            } catch (Exception e) {
            }
        }
        mProcessDataTask = null;
    }

    @Override
    public void onPreviewFrame(final byte[] previewData, final Camera camera) {
        try {
            if (!mSpotAble || getHandleScanDataListenerQueque().isEmpty() || camera == null || camera.getParameters() == null)
                return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            final Camera.Parameters parameters = mCamera.getParameters();
            final Camera.Size size = parameters.getPreviewSize();
            final Rect rect = mBoxView != null ? mBoxView.getScanBoxAreaRect(camera) : null;

            cancelProcessDataTask();
            mProcessDataTask = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (!mSpotAble || getHandleScanDataListenerQueque().isEmpty())
                        return;

                    try { //数据处理
                        int width = size.width;
                        int height = size.height;
                        byte[] data = previewData;
                        if (DisplayUtils.getScreenOrientation(getContext()) == Configuration.ORIENTATION_PORTRAIT) {
                            int rotationCount = getRotationCount();
                            if (rotationCount == 1 || rotationCount == 3) {
                                data = getRotatedData(rotationCount, data, width, height);

                                int tmp = width;
                                width = height;
                                height = tmp;
                            }
                        }

                        if (!mSpotAble || getHandleScanDataListenerQueque().isEmpty())
                            return;

                        for (IHandleScanDataListener listener : getHandleScanDataListenerQueque()) {
                            if (listener.onHandleScanData(previewData, data, width, height, rect) && mSpotAble && !getHandleScanDataListenerQueque().isEmpty())
                                break;
                        }
                    } catch (Exception e) {
                    }

                    try { //是否继续扫描
                        if (!mSpotAble || getHandleScanDataListenerQueque().isEmpty())
                            return;

                        boolean isContinuity = false;
                        for (IHandleScanDataListener listener : getHandleScanDataListenerQueque()) {
                            if (listener.isContinuity()) {
                                isContinuity = listener.isContinuity();
                                break;
                            }
                        }

                        if (!isContinuity)
                            return;
                    } catch (Exception e) {
                    }

                    try { //继续扫描
                        System.gc();
                        mHandler.post(mOneShotPreviewCallbackTask);
                    } catch (Exception e) {
                    }
                }
            });
            mProcessDataTask.start();
        } catch (Exception e) {
        }
    }

    public int getRotationCount() {
        int displayOrientation = this.mPreview.getDisplayOrientation(cameraId);
        return displayOrientation / 90;
    }


    private Runnable mOneShotPreviewCallbackTask = new Runnable() {
        @Override
        public void run() {
            if (mCamera != null && mSpotAble) {
                try {
                    mCamera.setOneShotPreviewCallback(ScanView.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public interface IHandleScanDataListener {
        Boolean onHandleScanData(byte[] previewData, byte[] data, int width, int height, Rect rect);

        public Boolean isContinuity();//是否连续

        public void release();//释放
    }

    public static void rotateYUV240SP(byte[] src, byte[] des, int width, int height) {
        int wh = width * height;
        //旋转Y
        int k = 0;
//        for (int i = 0; i < width; i++) { //左右互调了
//            for (int j = 0; j < height; j++) {
//                des[k] = src[width * j + i];
//                k++;
//            }
//        }
//
//        for (int i = 0; i < width; i += 2) {
//            for (int j = 0; j < height / 2; j++) {
//                des[k] = src[wh + width * j + i];
//                des[k + 1] = src[wh + width * j + i + 1];
//                k += 2;
//            }
//        }
        for (int i = 0; i < width; i++) { //正解
            for (int j = height - 1; j >= 0; j--) {
                des[k] = src[width * j + i];
                k++;
            }
        }
        for (int i = 0; i < width; i += 2) {
            for (int j = height / 2 - 1; j >= 0; j--) {
                des[k] = src[wh + width * j + i];
                des[k + 1] = src[wh + width * j + i + 1];
                k += 2;
            }
        }
    }

    public static byte[] getRotatedData(int rotationCount, byte[] data, int width, int height) {
        if (rotationCount == 1 || rotationCount == 3) {
            for (int i = 0; i < rotationCount; ++i) {
                byte[] rotatedData = new byte[data.length];

                int y;
                for (y = 0; y < height; ++y) {
                    for (int x = 0; x < width; ++x) {
                        rotatedData[x * height + height - y - 1] = data[x + y * width];
                    }
                }

                data = rotatedData;
                y = width;
                width = height;
                height = y;
            }
        }

        return data;
    }
}
