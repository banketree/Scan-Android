package com.uc56.scancore.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;


class CameraZoomPreview extends SurfaceView {
    private static final String TAG = CameraZoomPreview.class.getSimpleName();
    private float oldDist = 1f;
    protected Camera mCamera;
    private long lastZoomTime = 0L;

    public CameraZoomPreview(Context context) {
        super(context);
    }

    public CameraZoomPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
    }


    //触碰放大缩小
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mCamera == null)
            return super.onTouchEvent(event);

        if (event.getPointerCount() == 1) {
//            handleFocusMetering(event, mCamera);
        } else {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = getFingerSpacing(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDist = getFingerSpacing(event);
                    if (newDist > oldDist) {
                        Log.e("Camera", "进入放大手势");
                        handleZoom(true, mCamera);
                    } else if (newDist < oldDist) {
                        Log.e("Camera", "进入缩小手势");
                        handleZoom(false, mCamera);
                    }
                    oldDist = newDist;
                    break;
            }
        }
        return true;
    }

    private void handleZoom(boolean isZoomIn, Camera camera) {
        Log.i("Camera", "进入缩小放大方法");
        if ((lastZoomTime + 50) >= System.currentTimeMillis())
            return;
        lastZoomTime = System.currentTimeMillis();

        if (camera == null || camera.getParameters() == null)
            return;

        try {
            Camera.Parameters params = camera.getParameters();
            if (params.isZoomSupported()) {
                int maxZoom = params.getMaxZoom();
                int zoom = params.getZoom();
                if (isZoomIn && zoom < maxZoom) {
                    Log.e("Camera", "进入放大方法zoom=" + zoom);
                    zoom++;
                } else if (zoom > 0) {
                    Log.e("Camera", "进入缩小方法zoom=" + zoom);
                    zoom--;
                }
                params.setZoom(zoom);
                camera.setParameters(params);
            } else {
                Log.i("handleZoom", "zoom not supported");
            }
        } catch (Exception e) {
        }
    }

    private static void handleFocusMetering(MotionEvent event, Camera camera) {
        Log.e("Camera", "进入handleFocusMetering");
        if (camera == null || camera.getParameters() == null)
            return;
        try {
            Camera.Parameters params = camera.getParameters();
            Camera.Size previewSize = params.getPreviewSize();
            Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f, previewSize);
            Rect meteringRect = calculateTapArea(event.getX(), event.getY(), 1.5f, previewSize);

            camera.cancelAutoFocus();

            if (params.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> focusAreas = new ArrayList<>();
                focusAreas.add(new Camera.Area(focusRect, 800));
                params.setFocusAreas(focusAreas);
            } else {
                Log.i("handleZoom", "focus areas not supported");
            }
            if (params.getMaxNumMeteringAreas() > 0) {
                List<Camera.Area> meteringAreas = new ArrayList<>();
                meteringAreas.add(new Camera.Area(meteringRect, 800));
                params.setMeteringAreas(meteringAreas);
            } else {
                Log.i("handleZoom", "metering areas not supported");
            }
            final String currentFocusMode = params.getFocusMode();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
            camera.setParameters(params);

            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    Camera.Parameters params = camera.getParameters();
                    params.setFocusMode(currentFocusMode);
                    camera.setParameters(params);
                }
            });
        } catch (Exception e) {
        }
    }

    private static float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        Log.e("Camera", "getFingerSpacing ，计算距离 = " + (float) Math.sqrt(x * x + y * y));
        return (float) Math.sqrt(x * x + y * y);
    }

    private static Rect calculateTapArea(float x, float y, float coefficient, Camera.Size previewSize) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / previewSize.width - 1000);
        int centerY = (int) (y / previewSize.height - 1000);

        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);

        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }


}