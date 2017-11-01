package com.uc56.scancore;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;


//条形码扫描框
public class BoxBarCodeView extends BoxScanView {

    public BoxBarCodeView(Context context) {
        super(context);
    }

    @Override
    protected void afterInitCustomAttrs() {
        super.afterInitCustomAttrs();

        if (mRectWidth == 0)
            mRectWidth = ScanUtil.dp2px(getContext(), 200);
        if (mRectHeight == 0)
            mRectHeight = ScanUtil.dp2px(getContext(), 140);

        mAnimDelayTime = (int) ((1.0f * mAnimTime * mMoveStepDistance) / mRectWidth);

        if (!TextUtils.isEmpty(mTipText)) {
            if (mIsShowTipTextAsSingleLine) {
                mTipTextSl = new StaticLayout(mTipText, mTipPaint, ScanUtil.getScreenResolution(getContext()).x, Layout.Alignment.ALIGN_CENTER, 1.0f, 0, true);
            } else {
                mTipTextSl = new StaticLayout(mTipText, mTipPaint, mRectWidth - 2 * mTipBackgroundRadius, Layout.Alignment.ALIGN_CENTER, 1.0f, 0, true);
            }
        }

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
        canvas.drawRect(mScanLineLeft, mFramingRect.top + mHalfCornerSize + mScanLineMargin, mScanLineLeft + mScanLineSize, mFramingRect.bottom - mHalfCornerSize - mScanLineMargin, mPaint);
    }


    /**
     * 移动扫描线的位置
     */
    @Override
    protected void moveScanLine() {
        // 处理非网格扫描图片的情况
        mScanLineLeft += mMoveStepDistance;
        int scanLineSize = mScanLineSize;

        if (mIsScanLineReverse) {
            if (mScanLineLeft + scanLineSize > mFramingRect.right - mHalfCornerSize || mScanLineLeft < mFramingRect.left + mHalfCornerSize) {
                mMoveStepDistance = -mMoveStepDistance;
            }
        } else {
            if (mScanLineLeft + scanLineSize > mFramingRect.right - mHalfCornerSize) {
                mScanLineLeft = mFramingRect.left + mHalfCornerSize + 0.5f;
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