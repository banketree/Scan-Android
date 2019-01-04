package com.uc56.scancore;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.TypedValue;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.google.zxing.BarcodeFormat;

public class ScanUtil {
    public static final int ORIENTATION_PORTRAIT = 0;
    public static final int ORIENTATION_LANDSCAPE = 1;

    public static final int getOrientation(Context context) {
        Point screenResolution = getScreenResolution(context);
        return screenResolution.x > screenResolution.y ? ORIENTATION_LANDSCAPE : ORIENTATION_PORTRAIT;
    }

    public static Point getScreenResolution(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenResolution = new Point();
        if (android.os.Build.VERSION.SDK_INT >= 13) {
            display.getSize(screenResolution);
        } else {
            screenResolution.set(display.getWidth(), display.getHeight());
        }
        return screenResolution;
    }

    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context context, float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }

    public static Bitmap adjustPhotoRotation(Bitmap inputBitmap, int orientationDegree) {
        if (inputBitmap == null) {
            return null;
        }

        Matrix matrix = new Matrix();
        matrix.setRotate(orientationDegree, (float) inputBitmap.getWidth() / 2, (float) inputBitmap.getHeight() / 2);
        float outputX, outputY;
        if (orientationDegree == 90) {
            outputX = inputBitmap.getHeight();
            outputY = 0;
        } else {
            outputX = inputBitmap.getHeight();
            outputY = inputBitmap.getWidth();
        }

        final float[] values = new float[9];
        matrix.getValues(values);
        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];
        matrix.postTranslate(outputX - x1, outputY - y1);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap.getHeight(), inputBitmap.getWidth(), Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(outputBitmap);
        canvas.drawBitmap(inputBitmap, matrix, paint);
        return outputBitmap;
    }

    public static Bitmap makeTintBitmap(Bitmap inputBitmap, int tintColor) {
        if (inputBitmap == null) {
            return null;
        }

        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap.getWidth(), inputBitmap.getHeight(), inputBitmap.getConfig());
        Canvas canvas = new Canvas(outputBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(inputBitmap, 0, 0, paint);
        return outputBitmap;
    }

    public static int getScreenOrientation(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int orientation = Configuration.ORIENTATION_UNDEFINED;
        if (display.getWidth() == display.getHeight()) {
            orientation = Configuration.ORIENTATION_SQUARE;
        } else {
            if (display.getWidth() < display.getHeight()) {
                orientation = Configuration.ORIENTATION_PORTRAIT;
            } else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }

    /*
     * 二维码
     * */
    public static boolean isQrcode(BarcodeFormat codeFormat) {
        boolean result = false;
        if (codeFormat == BarcodeFormat.QR_CODE || codeFormat == BarcodeFormat.PDF_417 || codeFormat == BarcodeFormat.MAXICODE || codeFormat == BarcodeFormat.DATA_MATRIX || codeFormat == BarcodeFormat.AZTEC) {
            result = true;
        }

        return result;
    }

    /*
     * 二维码
     * */
    public static boolean isQrcode(me.dm7.barcodescanner.zbar.BarcodeFormat codeFormat) {
        boolean result = false;
        if (codeFormat == me.dm7.barcodescanner.zbar.BarcodeFormat.QRCODE || codeFormat == me.dm7.barcodescanner.zbar.BarcodeFormat.PDF417) {
            result = true;
        }

        return result;
    }

    //获取旋转
    public static int getDisplayOrientation(Context context, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        if (cameraId == -1) {
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        } else {
            Camera.getCameraInfo(cameraId, info);
        }

        WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    /**
     * Like {@link #getFramingRect} but coordinates are in terms of the preview frame, not UI / screen.
     *
     * @return {@link Rect} expressing barcode scan area in terms of the preview size
     */
    public static Rect getRectInPreview(Context context, Rect rectBox, Point cameraResolution) {
        // 获取相机分辨率和屏幕分辨率
        Point screenResolution = getScreenResolution(context);
        if (cameraResolution == null || screenResolution == null) {
            // Called early, before init even finished
            return null;
        }
        // 根据相机分辨率和屏幕分辨率的比例对屏幕中央聚焦框进行调整
        rectBox.left = rectBox.left * cameraResolution.x / screenResolution.x;
        rectBox.right = rectBox.right * cameraResolution.x / screenResolution.x;
        rectBox.top = rectBox.top * cameraResolution.y / screenResolution.y;
        rectBox.bottom = rectBox.bottom * cameraResolution.y / screenResolution.y;
        return rectBox;
    }

    public static Bitmap getCorpBitmapInPreview(byte[] data, Rect rect) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap srcBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        Bitmap corpBitmap = null;
        if (rect == null) {
            corpBitmap = srcBitmap;
        } else {
            corpBitmap = Bitmap.createBitmap(srcBitmap, rect.left, rect.top, rect.width(), rect.height());
            srcBitmap.recycle();
            srcBitmap = null;
        }
//        int bitmapWidth = corpBitmap.getWidth();
//        int bitmapHeight = corpBitmap.getHeight();
//        int[] pixels = new int[bitmapWidth * bitmapHeight];
//        corpBitmap.getPixels(pixels, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);
//        corpBitmap.recycle();
//        corpBitmap = null;
        return corpBitmap;
    }

    /**
     * RGB转换成YCbCr
     *
     * @param R 0-255的数值表示R
     * @param G 0-255的数值表示G
     * @param B 0-255的数值表示B
     * @return yuv数组，依次Y，U，
     */
    private byte[] RGBToYUV(int R, int G, int B) {
        int[] rgbs = new int[]{R, G, B};
        byte[] yuvs = new byte[rgbs.length];
        for (int i = 0; i < rgbs.length; i++) {//限制RGB输入值只能是0-255
            if (rgbs[i] < 0) rgbs[i] = -rgbs[i];
            if (rgbs[i] > 255) rgbs[i] = rgbs[i] % 256;
        }
        yuvs[0] = (byte) (0.257 * rgbs[0] + 0.504 * rgbs[1] + 0.098 * rgbs[2] + 16);
        yuvs[1] = (byte) (-0.148 * rgbs[0] - 0.291 * rgbs[1] + 0.439 * rgbs[2] + 128);
        yuvs[2] = (byte) (0.439 * rgbs[0] - 0.368 * rgbs[1] - 0.071 * rgbs[2] + 128);
        return yuvs;
    }
}