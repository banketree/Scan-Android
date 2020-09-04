@file:Suppress("DEPRECATED_IDENTITY_EQUALS")

package com.uc56.scancore

import com.banketree.scancore.ZbarBarcodeFormat


//zbar 判断条形码
inline fun ZbarBarcodeFormat.isBarCode(): Boolean {
    var result = true
    if (this.id === ZbarBarcodeFormat.QRCODE.id ||
            this.id === ZbarBarcodeFormat.PDF417.id
    ) result = false
    return result
}

//zbar 判断二维码
inline fun ZbarBarcodeFormat.isQCode(): Boolean = !isBarCode()

//zxing 判断条形码
inline fun com.google.zxing.BarcodeFormat.isBarCode(): Boolean {
    var result = true
    if (this == com.google.zxing.BarcodeFormat.QR_CODE ||
            this == com.google.zxing.BarcodeFormat.PDF_417 ||
            this == com.google.zxing.BarcodeFormat.MAXICODE ||
            this == com.google.zxing.BarcodeFormat.DATA_MATRIX ||
            this == com.google.zxing.BarcodeFormat.AZTEC
    ) result = false
    return result
}

//zxing 判断二维码
inline fun com.google.zxing.BarcodeFormat.isQCode(): Boolean = !isBarCode()