package com.uc56.scancore;

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
}
