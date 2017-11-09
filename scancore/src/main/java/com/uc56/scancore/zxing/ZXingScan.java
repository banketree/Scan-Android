package com.uc56.scancore.zxing;

import android.graphics.Rect;
import android.text.TextUtils;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.uc56.scancore.ScanView;


public class ZXingScan implements ScanView.IHandleScanDataListener {
    private MultiFormatReader mMultiFormatReader;
    private IZXingResultListener listener;

    public ZXingScan(IZXingResultListener listener) {
        this.listener = listener;
        initMultiFormatReader();
    }

    private void initMultiFormatReader() {
        mMultiFormatReader = new MultiFormatReader();
        mMultiFormatReader.setHints(QRCodeDecoder.HINTS);
    }

    @Override
    public Boolean onHandleScanData(final byte[] previewData, byte[] data, int width, int height, Rect rect) {
        String result = null;
        Result rawResult = null;

        try {
            PlanarYUVLuminanceSource source = null;
            if (rect != null) {
                source = new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height(), false);
            } else {
                source = new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false);
            }
            rawResult = mMultiFormatReader.decodeWithState(new BinaryBitmap(new HybridBinarizer(source)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mMultiFormatReader.reset();
        }

        if (rawResult != null) {
            result = rawResult.getText();
        }

        if (TextUtils.isEmpty(result) && rect != null) //重复
            return onHandleScanData(previewData, data, width, height, null);

        if (TextUtils.isEmpty(result))
            return false;

        listener.onScanResult(result);
        return true;
    }

    @Override
    public Boolean isContinuity() {
        return true;
    }

    public interface IZXingResultListener {
        void onScanResult(String result);
    }
}