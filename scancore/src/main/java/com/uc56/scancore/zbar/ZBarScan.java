package com.uc56.scancore.zbar;

import android.graphics.Rect;
import android.text.TextUtils;

import com.uc56.scancore.ScanView;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;


public class ZBarScan implements ScanView.IHandleScanDataListener {

    static {
        System.loadLibrary("iconv");
    }

    private ImageScanner mScanner;
    private IZbarResultListener listener;

    public ZBarScan(IZbarResultListener listener) {
        this.listener = listener;
        setupScanner();
    }

    public void setupScanner() {
        mScanner = new ImageScanner();
        mScanner.setConfig(0, Config.X_DENSITY, 3);
        mScanner.setConfig(0, Config.Y_DENSITY, 3);

        mScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
        for (BarcodeFormat format : BarcodeFormat.ALL_FORMATS) {
            mScanner.setConfig(format.getId(), Config.ENABLE, 1);
        }
    }

    @Override
    public Boolean onHandleScanData(byte[] data, int width, int height, Rect rect) {
        String result = null;
        Image barcode = new Image(width, height, "Y800");

        if (rect != null && rect.left + rect.width() <= width && rect.top + rect.height() <= height) {//
            barcode.setCrop(rect.left, rect.top, rect.width(), rect.height());
        }

        barcode.setData(data);
        result = processData(barcode);

//        if (TextUtils.isEmpty(result) && rect != null) //重复
//            return onHandleScanData(data, width, height, null);

        if (TextUtils.isEmpty(result))
            return false;

        listener.onScanResult(result);
        return true;
    }

    @Override
    public Boolean isContinuity() {
        return true;
    }

    private String processData(Image barcode) {
        String result = null;
        if (mScanner.scanImage(barcode) != 0) {
            SymbolSet syms = mScanner.getResults();
            for (Symbol sym : syms) {
                String symData = sym.getData();
                if (!TextUtils.isEmpty(symData)) {
                    result = symData;
                    break;
                }
            }
        }
        return result;
    }


    public interface IZbarResultListener {
        void onScanResult(String result);
    }
}