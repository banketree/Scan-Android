package com.uc56.scancore;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextUtils;

//二维码扫描框
public class BoxQRCodeScanView extends BoxScanView {
    public BoxQRCodeScanView(Context context) {
        super(context);
    }

    @Override
    protected void afterInitCustomAttrs() {
        super.afterInitCustomAttrs();

        if (mRectWidth == 0)
            mRectWidth = ScanUtil.dp2px(getContext(), 250);
        if (mRectHeight == 0)
            mRectHeight = mRectWidth;
        mAnimDelayTime = (int) ((1.0f * mAnimTime * mMoveStepDistance) / mRectHeight);

        if (mIsCenterVertical) {
            int screenHeight = ScanUtil.getScreenResolution(getContext()).y;
            if (mToolbarHeight == 0) {
                mTopOffset = (screenHeight - mRectHeight) / 2;
            } else {
                mTopOffset = (screenHeight - mRectHeight) / 2 + mToolbarHeight / 2;
            }
        }

        calFramingRect();

        postInvalidate();
    }

    /**
     * 画扫描线
     *
     * @param canvas
     */
    @Override
    protected void drawScanLine(Canvas canvas) {
        super.drawScanLine(canvas);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mScanLineColor);
        canvas.drawRect(mFramingRect.left + mHalfCornerSize + mScanLineMargin, mScanLineTop, mFramingRect.right - mHalfCornerSize - mScanLineMargin, mScanLineTop + mScanLineSize, mPaint);
    }

    /**
     * 移动扫描线的位置
     */
    @Override
    protected void moveScanLine() {
        super.moveScanLine();

        // 处理非网格扫描图片的情况
        mScanLineTop += mMoveStepDistance;
        int scanLineSize = mScanLineSize;
//        if (mScanLineBitmap != null) {
//            scanLineSize = mScanLineBitmap.getHeight();
//        }

        if (mIsScanLineReverse) {
            if (mScanLineTop + scanLineSize > mFramingRect.bottom - mHalfCornerSize || mScanLineTop < mFramingRect.top + mHalfCornerSize) {
                mMoveStepDistance = -mMoveStepDistance;
            }
        } else {
            if (mScanLineTop + scanLineSize > mFramingRect.bottom - mHalfCornerSize) {
                mScanLineTop = mFramingRect.top + mHalfCornerSize + 0.5f;
            }
        }

        postInvalidateDelayed(mAnimDelayTime, mFramingRect.left, mFramingRect.top, mFramingRect.right, mFramingRect.bottom);
    }

    @Override
    protected void calFramingRect() {
        super.calFramingRect();
        mScanLineTop = mFramingRect.top + mHalfCornerSize + 0.5f;
    }
}