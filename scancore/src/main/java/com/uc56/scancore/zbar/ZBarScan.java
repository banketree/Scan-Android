package com.uc56.scancore.zbar;

import android.graphics.Rect;
import android.text.TextUtils;

import com.uc56.scancore.ScanView;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;

public class ZBarScan implements ScanView.IHandleScanDataListener {
    private boolean release = false;

    static {
        System.loadLibrary("iconv");
    }

    private ImageScanner mScanner;
    private List<BarcodeFormat> mFormats;
    private IZbarResultListener listener;


    public ZBarScan(IZbarResultListener listener) {
        this.listener = listener;
        setupScanner();
    }

    public void setFormats(List<BarcodeFormat> formats) {
        mFormats = formats;
        setupScanner();
    }

    public Collection<BarcodeFormat> getFormats() {
        if (mFormats == null) {
            mFormats = BarcodeFormat.ALL_FORMATS;
        }
        return mFormats;
    }

    public void setupScanner() {
        mScanner = new ImageScanner();
        mScanner.setConfig(0, Config.X_DENSITY, 3);
        mScanner.setConfig(0, Config.Y_DENSITY, 3);

        mScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
        for (BarcodeFormat format : getFormats()) {
            mScanner.setConfig(format.getId(), Config.ENABLE, 1);
        }
    }

    @Override
    public Boolean onHandleScanData(final byte[] previewData, byte[] data, int width, int height, Rect rect) {
        if (mScanner == null || release)
            return false;

//        final Result rawResult = new Result();

        try {
            Image barcode = new Image(width, height, "Y800");
            barcode.setData(data);
            if (rect != null)
                barcode.setCrop(rect.left, rect.top, rect.width(), rect.height());
            else
                barcode.setCrop(0, 0, width, height);

            if (mScanner.scanImage(barcode) != 0) {
                SymbolSet syms = mScanner.getResults();
                for (Symbol sym : syms) {
                    // In order to retreive QR codes containing null bytes we need to
                    // use getDataBytes() rather than getData() which uses C strings.
                    // Weirdly ZBar transforms all data to UTF-8, even the data returned
                    // by getDataBytes() so we have to decode it as UTF-8.
                    String symData;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        symData = new String(sym.getDataBytes(), StandardCharsets.UTF_8);
                    } else {
                        symData = sym.getData();
                    }
                    if (!TextUtils.isEmpty(symData)) {
                        if (listener.onScanResult(BarcodeFormat.getFormatById(sym.getType()), symData))
                            return true;
//                        rawResult.setContents(symData);
//                        rawResult.setBarcodeFormat(BarcodeFormat.getFormatById(sym.getType()));
                    }
                }
            }
        } catch (Exception e) {
        }

//        if (TextUtils.isEmpty(result) && rect != null) //重复
//            return onHandleScanData(previewData, data, width, height, null);

        return false;
    }

    @Override
    public Boolean isContinuity() {
        if (mScanner == null)
            return false;
        return true;
    }

    @Override
    public void release() {
        release = true;
    }

    public interface IZbarResultListener {
        boolean onScanResult(BarcodeFormat codeFormat, String result);
    }
}