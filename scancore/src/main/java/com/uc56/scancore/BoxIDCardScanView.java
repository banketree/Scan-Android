package com.uc56.scancore;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextUtils;

//身份证扫描框
public class BoxIDCardScanView extends BoxBarCodeView {
    public BoxIDCardScanView(Context context) {
        super(context);
    }

    @Override
    protected void afterInitCustomAttrs() {
        mRectWidth = ScanUtil.dp2px(getContext(), 320);
        mRectHeight = ScanUtil.dp2px(getContext(), 260);
        super.afterInitCustomAttrs();
    }
}