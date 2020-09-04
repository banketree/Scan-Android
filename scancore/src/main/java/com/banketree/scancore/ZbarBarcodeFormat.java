package com.banketree.scancore;

import me.dm7.barcodescanner.zbar.BarcodeFormat;

public class ZbarBarcodeFormat extends BarcodeFormat {
    public ZbarBarcodeFormat(int id, String name) {
        super(id, name);
    }

    public ZbarBarcodeFormat(BarcodeFormat barcodeFormat) {
        super(barcodeFormat.getId(), barcodeFormat.getName());
    }

    @Override
    public int getId() {
        return super.getId();
    }

    @Override
    public String getName() {
        return super.getName();
    }

    //zbar 判断条形码
    public Boolean isBarCode() {
        boolean result = true;
        if (this == com.banketree.scancore.ZbarBarcodeFormat.QRCODE ||
                this == com.banketree.scancore.ZbarBarcodeFormat.PDF417
        ) result = false;
        return result;
    }

    //zbar 判断二维码
    public Boolean isQCode() {
        return !isBarCode();
    }
}
