package com.uc56.scancore.zxing;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.uc56.scancore.Interface.IHandleScanDataListener;
import com.uc56.scancore.ScanUtil;
import com.uc56.scancore.ScanView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ZXingScan implements IHandleScanDataListener {
    private IZXingResultListener listener;
    private boolean release = false;

    private MultiFormatReader mMultiFormatReader;
    public static final List<BarcodeFormat> ALL_FORMATS = new ArrayList<>();
    private List<BarcodeFormat> mFormats;

    static {
        //一维码：商品
        ALL_FORMATS.add(BarcodeFormat.UPC_A);
        ALL_FORMATS.add(BarcodeFormat.UPC_E);
        ALL_FORMATS.add(BarcodeFormat.EAN_13);
        ALL_FORMATS.add(BarcodeFormat.EAN_8);
        ALL_FORMATS.add(BarcodeFormat.RSS_14);
        ALL_FORMATS.add(BarcodeFormat.RSS_EXPANDED);
        //一维码：工业
        ALL_FORMATS.add(BarcodeFormat.CODE_39);
        ALL_FORMATS.add(BarcodeFormat.CODE_93);
        ALL_FORMATS.add(BarcodeFormat.CODE_128);
        ALL_FORMATS.add(BarcodeFormat.ITF);
        ALL_FORMATS.add(BarcodeFormat.CODABAR);

        //二维码
        ALL_FORMATS.add(BarcodeFormat.QR_CODE);//二维码

        ALL_FORMATS.add(BarcodeFormat.AZTEC); //Aztec
        ALL_FORMATS.add(BarcodeFormat.DATA_MATRIX);//Data Matrix
        ALL_FORMATS.add(BarcodeFormat.MAXICODE);
        ALL_FORMATS.add(BarcodeFormat.PDF_417);//PDF 417
        ALL_FORMATS.add(BarcodeFormat.UPC_EAN_EXTENSION);
    }

    public ZXingScan(IZXingResultListener listener) {
        this.listener = listener;
        initMultiFormatReader();
    }

    private void initMultiFormatReader() {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, getFormats());
        mMultiFormatReader = new MultiFormatReader();
        mMultiFormatReader.setHints(hints);
    }

    public void setFormats(List<BarcodeFormat> formats) {
        mFormats = formats;
        initMultiFormatReader();
    }

    public Collection<BarcodeFormat> getFormats() {
        if (mFormats == null) {
            mFormats = ALL_FORMATS;
        }
        return mFormats;
    }

    @Override
    public Boolean onHandleScanData(final byte[] previewData, byte[] data, final int format, int width, int height, Rect rect) {
        if (release)
            return false;
        Result rawResult = null;

        if (format == ImageFormat.JPEG) {
            try {
//                Result result = new MultiFormatReader().decode(binaryBitmap);//解析图片中的code
                RGBLuminanceSource source = buildLuminanceSourceRgb(data, width, height, rect);

                if (source != null) {
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                    try {
                        rawResult = mMultiFormatReader.decodeWithState(bitmap);
                    } catch (ReaderException re) { // continue
                    } catch (NullPointerException npe) { // This is terrible
                    } catch (ArrayIndexOutOfBoundsException aoe) {
                    } finally {
                        mMultiFormatReader.reset();
                    }

                    if (rawResult == null) {
                        LuminanceSource invertedSource = source.invert();
                        bitmap = new BinaryBitmap(new HybridBinarizer(invertedSource));
                        try {
                            rawResult = mMultiFormatReader.decodeWithState(bitmap);
                        } catch (NotFoundException e) { // continue
                        } finally {
                            mMultiFormatReader.reset();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                PlanarYUVLuminanceSource source = buildLuminanceSource(data, width, height, rect);

                if (source != null) {
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                    try {
                        rawResult = mMultiFormatReader.decodeWithState(bitmap);
                    } catch (ReaderException re) { // continue
                    } catch (NullPointerException npe) { // This is terrible
                    } catch (ArrayIndexOutOfBoundsException aoe) {
                    } finally {
                        mMultiFormatReader.reset();
                    }

                    if (rawResult == null) {
                        LuminanceSource invertedSource = source.invert();
                        bitmap = new BinaryBitmap(new HybridBinarizer(invertedSource));
                        try {
                            rawResult = mMultiFormatReader.decodeWithState(bitmap);
                        } catch (NotFoundException e) { // continue
                        } finally {
                            mMultiFormatReader.reset();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (rawResult == null)
            return false;

//        if (TextUtils.isEmpty(rawResult.getText()) && rect != null) //重复
//            return onHandleScanData(previewData, data, width, height, null);

        if (TextUtils.isEmpty(rawResult.getText()))
            return false;

        return listener.onScanResult(rawResult.getBarcodeFormat(), rawResult.getText());
    }

    @Override
    public Boolean isContinuity() {
        if (mMultiFormatReader == null)
            return false;
        return true;
    }

    @Override
    public void release() {
        release = true;
    }

    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height, Rect rect) {
        PlanarYUVLuminanceSource source = null;
        try {
            if (rect == null) {
                source = new PlanarYUVLuminanceSource(data, width, height, 0, 0,
                        width, height, false);
            } else {
                source = new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                        rect.width(), rect.height(), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return source;
    }


    public RGBLuminanceSource buildLuminanceSourceRgb(byte[] data, int width, int height, Rect rect) {
        RGBLuminanceSource source = null;
        try {
            Bitmap corpBitmap = ScanUtil.getCorpBitmapInPreview(data, rect);
            int bitmapWidth = corpBitmap.getWidth();
            int bitmapHeight = corpBitmap.getHeight();
            int[] pixels = new int[bitmapWidth * bitmapHeight];
            corpBitmap.getPixels(pixels, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);
            corpBitmap.recycle();
            corpBitmap = null;
            source = new RGBLuminanceSource(bitmapWidth, bitmapHeight, pixels);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return source;
    }

    public interface IZXingResultListener {
        boolean onScanResult(BarcodeFormat codeFormat, String result);
    }
}