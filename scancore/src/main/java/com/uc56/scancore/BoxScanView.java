package com.uc56.scancore;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;


//扫描框
public abstract class BoxScanView extends View {
    protected int mMoveStepDistance; //移动步骤
    protected int mAnimDelayTime = 500; //动画延迟时间

    protected Rect mFramingRect; //框框区域
    protected float mScanLineTop;
    protected float mScanLineLeft;
    protected Paint mPaint;
    protected TextPaint mTipPaint;

    protected int mMaskColor; //面纱
    protected int mCornerColor;
    protected int mCornerLength;
    protected int mCornerSize;
    protected int mRectWidth;
    protected int mRectHeight;
    protected int mTopOffset;
    protected int mScanLineSize;
    protected int mScanLineColor;
    protected int mScanLineMargin;
    protected int mBorderSize;
    protected int mBorderColor;
    protected int mAnimTime;
    protected boolean mIsCenterVertical;
    protected int mToolbarHeight;

    protected String mTipText;
    protected int mTipTextSize;
    protected int mTipTextColor;
    protected boolean mIsTipTextBelowRect;
    protected int mTipTextMargin;
    protected boolean mIsShowTipTextAsSingleLine;
    protected int mTipBackgroundColor;
    protected boolean mIsShowTipBackground;
    protected boolean mIsScanLineReverse;


    protected float mHalfCornerSize;
    protected StaticLayout mTipTextSl;
    protected int mTipBackgroundRadius;

    protected boolean mIsOnlyDecodeScanBoxArea;

    public BoxScanView(Context context) {
        super(context);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mMaskColor = Color.parseColor("#33FFFFFF");
        mCornerColor = Color.WHITE;
        mCornerLength = ScanUtil.dp2px(context, 20);
        mCornerSize = ScanUtil.dp2px(context, 3);
        mScanLineSize = ScanUtil.dp2px(context, 1);
        mScanLineColor = Color.WHITE;
        mTopOffset = ScanUtil.dp2px(context, 90);
        mRectWidth = ScanUtil.dp2px(context, 200);
        mScanLineMargin = 0;
        mBorderSize = ScanUtil.dp2px(context, 1);
        mBorderColor = Color.WHITE;
        mAnimTime = 1000;
        mIsCenterVertical = false;
        mToolbarHeight = 0;

        mMoveStepDistance = ScanUtil.dp2px(context, 2);
        mTipText = null;
        mTipTextSize = ScanUtil.sp2px(context, 14);
        mTipTextColor = Color.WHITE;
        mIsTipTextBelowRect = false;
        mTipTextMargin = ScanUtil.dp2px(context, 20);
        mIsShowTipTextAsSingleLine = false;
        mTipBackgroundColor = Color.parseColor("#22000000");
        mIsShowTipBackground = false;
        mIsScanLineReverse = false;

        mTipPaint = new TextPaint();
        mTipPaint.setAntiAlias(true);

        mTipBackgroundRadius = ScanUtil.dp2px(context, 4);

        mIsOnlyDecodeScanBoxArea = false;
    }

    public void initCustomAttrs(Context context, AttributeSet attrs) {
        int cout = attrs.getAttributeCount();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScanView);
        final int count = typedArray.getIndexCount();
        for (int i = 0; i < count; i++) {
            initCustomAttr(typedArray.getIndex(i), typedArray);
        }
        typedArray.recycle();

        afterInitCustomAttrs();
    }

    protected void initCustomAttr(int attr, TypedArray typedArray) {
        if (attr == R.styleable.ScanView_sc_topOffset) {
            mTopOffset = typedArray.getDimensionPixelSize(attr, mTopOffset);
        } else if (attr == R.styleable.ScanView_sc_cornerSize) {
            mCornerSize = typedArray.getDimensionPixelSize(attr, mCornerSize);
        } else if (attr == R.styleable.ScanView_sc_cornerLength) {
            mCornerLength = typedArray.getDimensionPixelSize(attr, mCornerLength);
        } else if (attr == R.styleable.ScanView_sc_scanLineSize) {
            mScanLineSize = typedArray.getDimensionPixelSize(attr, mScanLineSize);
        } else if (attr == R.styleable.ScanView_sc_rectWidth) {
            mRectWidth = typedArray.getDimensionPixelSize(attr, mRectWidth);
        } else if (attr == R.styleable.ScanView_sc_maskColor) {
            mMaskColor = typedArray.getColor(attr, mMaskColor);
        } else if (attr == R.styleable.ScanView_sc_cornerColor) {
            mCornerColor = typedArray.getColor(attr, mCornerColor);
        } else if (attr == R.styleable.ScanView_sc_scanLineColor) {
            mScanLineColor = typedArray.getColor(attr, mScanLineColor);
        } else if (attr == R.styleable.ScanView_sc_scanLineMargin) {
            mScanLineMargin = typedArray.getDimensionPixelSize(attr, mScanLineMargin);
        } else if (attr == R.styleable.ScanView_sc_borderSize) {
            mBorderSize = typedArray.getDimensionPixelSize(attr, mBorderSize);
        } else if (attr == R.styleable.ScanView_sc_borderColor) {
            mBorderColor = typedArray.getColor(attr, mBorderColor);
        } else if (attr == R.styleable.ScanView_sc_animTime) {
            mAnimTime = typedArray.getInteger(attr, mAnimTime);
        } else if (attr == R.styleable.ScanView_sc_isCenterVertical) {
            mIsCenterVertical = typedArray.getBoolean(attr, mIsCenterVertical);
        } else if (attr == R.styleable.ScanView_sc_toolbarHeight) {
            mToolbarHeight = typedArray.getDimensionPixelSize(attr, mToolbarHeight);
        } else if (attr == R.styleable.ScanView_sc_tipText) {
            mTipText = typedArray.getString(attr);
        } else if (attr == R.styleable.ScanView_sc_tipTextSize) {
            mTipTextSize = typedArray.getDimensionPixelSize(attr, mTipTextSize);
        } else if (attr == R.styleable.ScanView_sc_tipTextColor) {
            mTipTextColor = typedArray.getColor(attr, mTipTextColor);
        } else if (attr == R.styleable.ScanView_sc_isTipTextBelowRect) {
            mIsTipTextBelowRect = typedArray.getBoolean(attr, mIsTipTextBelowRect);
        } else if (attr == R.styleable.ScanView_sc_tipTextMargin) {
            mTipTextMargin = typedArray.getDimensionPixelSize(attr, mTipTextMargin);
        } else if (attr == R.styleable.ScanView_sc_isShowTipTextAsSingleLine) {
            mIsShowTipTextAsSingleLine = typedArray.getBoolean(attr, mIsShowTipTextAsSingleLine);
        } else if (attr == R.styleable.ScanView_sc_isShowTipBackground) {
            mIsShowTipBackground = typedArray.getBoolean(attr, mIsShowTipBackground);
        } else if (attr == R.styleable.ScanView_sc_tipBackgroundColor) {
            mTipBackgroundColor = typedArray.getColor(attr, mTipBackgroundColor);
        } else if (attr == R.styleable.ScanView_sc_isScanLineReverse) {
            mIsScanLineReverse = typedArray.getBoolean(attr, mIsScanLineReverse);
        } else if (attr == R.styleable.ScanView_sc_isOnlyDecodeScanBoxArea) {
            mIsOnlyDecodeScanBoxArea = typedArray.getBoolean(attr, mIsOnlyDecodeScanBoxArea);
        }
    }

    protected void afterInitCustomAttrs() {
        mTopOffset += mToolbarHeight;
        mHalfCornerSize = 1.0f * mCornerSize / 2;

        mTipPaint.setTextSize(mTipTextSize);
        mTipPaint.setColor(mTipTextColor);

        if (!TextUtils.isEmpty(mTipText)) {
            if (mIsShowTipTextAsSingleLine) {
                mTipTextSl = new StaticLayout(mTipText, mTipPaint, ScanUtil.getScreenResolution(getContext()).x, Layout.Alignment.ALIGN_CENTER, 1.0f, 0, true);
            } else {
                mTipTextSl = new StaticLayout(mTipText, mTipPaint, mRectWidth - 2 * mTipBackgroundRadius, Layout.Alignment.ALIGN_CENTER, 1.0f, 0, true);
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mFramingRect == null) {
            return;
        }

        // 画遮罩层
        drawMask(canvas);

        // 画边框线
        drawBorderLine(canvas);

        // 画四个直角的线
        drawCornerLine(canvas);

        // 画扫描线
        drawScanLine(canvas);

        // 画提示文本
        drawTipText(canvas);

        // 移动扫描线的位置
        moveScanLine();

    }

    /**
     * 画遮罩层
     *
     * @param canvas
     */
    protected void drawMask(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        if (mMaskColor != Color.TRANSPARENT) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mMaskColor);
            canvas.drawRect(0, 0, width, mFramingRect.top, mPaint);
            canvas.drawRect(0, mFramingRect.top, mFramingRect.left, mFramingRect.bottom + 1, mPaint);
            canvas.drawRect(mFramingRect.right + 1, mFramingRect.top, width, mFramingRect.bottom + 1, mPaint);
            canvas.drawRect(0, mFramingRect.bottom + 1, width, height, mPaint);
        }
    }

    /**
     * 画边框线
     *
     * @param canvas
     */
    protected void drawBorderLine(Canvas canvas) {
        if (mBorderSize > 0) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(mBorderColor);
            mPaint.setStrokeWidth(mBorderSize);
            canvas.drawRect(mFramingRect, mPaint);
        }
    }

    /**
     * 画四个直角的线
     *
     * @param canvas
     */
    protected void drawCornerLine(Canvas canvas) {
        drawBorderLine(canvas);

        if (mHalfCornerSize > 0) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(mCornerColor);
            mPaint.setStrokeWidth(mCornerSize);
            canvas.drawLine(mFramingRect.left - mHalfCornerSize, mFramingRect.top, mFramingRect.left - mHalfCornerSize + mCornerLength, mFramingRect.top, mPaint);
            canvas.drawLine(mFramingRect.left, mFramingRect.top - mHalfCornerSize, mFramingRect.left, mFramingRect.top - mHalfCornerSize + mCornerLength, mPaint);
            canvas.drawLine(mFramingRect.right + mHalfCornerSize, mFramingRect.top, mFramingRect.right + mHalfCornerSize - mCornerLength, mFramingRect.top, mPaint);
            canvas.drawLine(mFramingRect.right, mFramingRect.top - mHalfCornerSize, mFramingRect.right, mFramingRect.top - mHalfCornerSize + mCornerLength, mPaint);

            canvas.drawLine(mFramingRect.left - mHalfCornerSize, mFramingRect.bottom, mFramingRect.left - mHalfCornerSize + mCornerLength, mFramingRect.bottom, mPaint);
            canvas.drawLine(mFramingRect.left, mFramingRect.bottom + mHalfCornerSize, mFramingRect.left, mFramingRect.bottom + mHalfCornerSize - mCornerLength, mPaint);
            canvas.drawLine(mFramingRect.right + mHalfCornerSize, mFramingRect.bottom, mFramingRect.right + mHalfCornerSize - mCornerLength, mFramingRect.bottom, mPaint);
            canvas.drawLine(mFramingRect.right, mFramingRect.bottom + mHalfCornerSize, mFramingRect.right, mFramingRect.bottom + mHalfCornerSize - mCornerLength, mPaint);
        }
    }

    /**
     * 画扫描线
     *
     * @param canvas
     */
    protected void drawScanLine(Canvas canvas) {
    }

    /**
     * 画提示文本
     *
     * @param canvas
     */
    protected void drawTipText(Canvas canvas) {
        if (TextUtils.isEmpty(mTipText) || mTipTextSl == null) {
            return;
        }

        if (mIsTipTextBelowRect) {
            if (mIsShowTipBackground) {
                mPaint.setColor(mTipBackgroundColor);
                mPaint.setStyle(Paint.Style.FILL);
                if (mIsShowTipTextAsSingleLine) {
                    Rect tipRect = new Rect();
                    mTipPaint.getTextBounds(mTipText, 0, mTipText.length(), tipRect);
                    float left = (canvas.getWidth() - tipRect.width()) / 2 - mTipBackgroundRadius;
                    canvas.drawRoundRect(new RectF(left, mFramingRect.bottom + mTipTextMargin - mTipBackgroundRadius, left + tipRect.width() + 2 * mTipBackgroundRadius, mFramingRect.bottom + mTipTextMargin + mTipTextSl.getHeight() + mTipBackgroundRadius), mTipBackgroundRadius, mTipBackgroundRadius, mPaint);
                } else {
                    canvas.drawRoundRect(new RectF(mFramingRect.left, mFramingRect.bottom + mTipTextMargin - mTipBackgroundRadius, mFramingRect.right, mFramingRect.bottom + mTipTextMargin + mTipTextSl.getHeight() + mTipBackgroundRadius), mTipBackgroundRadius, mTipBackgroundRadius, mPaint);
                }
            }

            canvas.save();
            if (mIsShowTipTextAsSingleLine) {
                canvas.translate(0, mFramingRect.bottom + mTipTextMargin);
            } else {
                canvas.translate(mFramingRect.left + mTipBackgroundRadius, mFramingRect.bottom + mTipTextMargin);
            }
            mTipTextSl.draw(canvas);
            canvas.restore();
        } else {
            if (mIsShowTipBackground) {
                mPaint.setColor(mTipBackgroundColor);
                mPaint.setStyle(Paint.Style.FILL);

                if (mIsShowTipTextAsSingleLine) {
                    Rect tipRect = new Rect();
                    mTipPaint.getTextBounds(mTipText, 0, mTipText.length(), tipRect);
                    float left = (canvas.getWidth() - tipRect.width()) / 2 - mTipBackgroundRadius;
                    canvas.drawRoundRect(new RectF(left, mFramingRect.top - mTipTextMargin - mTipTextSl.getHeight() - mTipBackgroundRadius, left + tipRect.width() + 2 * mTipBackgroundRadius, mFramingRect.top - mTipTextMargin + mTipBackgroundRadius), mTipBackgroundRadius, mTipBackgroundRadius, mPaint);
                } else {
                    canvas.drawRoundRect(new RectF(mFramingRect.left, mFramingRect.top - mTipTextMargin - mTipTextSl.getHeight() - mTipBackgroundRadius, mFramingRect.right, mFramingRect.top - mTipTextMargin + mTipBackgroundRadius), mTipBackgroundRadius, mTipBackgroundRadius, mPaint);
                }
            }

            canvas.save();
            if (mIsShowTipTextAsSingleLine) {
                canvas.translate(0, mFramingRect.top - mTipTextMargin - mTipTextSl.getHeight());
            } else {
                canvas.translate(mFramingRect.left + mTipBackgroundRadius, mFramingRect.top - mTipTextMargin - mTipTextSl.getHeight());
            }
            mTipTextSl.draw(canvas);
            canvas.restore();
        }
    }

    /**
     * 移动扫描线的位置
     */
    protected void moveScanLine() {
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calFramingRect();
    }

    protected void calFramingRect() {
        int leftOffset = (getWidth() - mRectWidth) / 2;
        mFramingRect = new Rect(leftOffset, mTopOffset, leftOffset + mRectWidth, mTopOffset + mRectHeight);
        mScanLineLeft = mFramingRect.left + mHalfCornerSize + 0.5f;
    }

    private Rect getScanBoxAreaRect(int previewHeight) {
        if (mIsOnlyDecodeScanBoxArea) {
            Rect rect = new Rect(mFramingRect);
            float ratio = 1.0f * previewHeight / getMeasuredHeight();
            rect.left = (int) (rect.left * ratio);
            rect.right = (int) (rect.right * ratio);
            rect.top = (int) (rect.top * ratio);
            rect.bottom = (int) (rect.bottom * ratio);
            return rect;
        }

        return null;
    }

    public Rect getScanBoxAreaRect(final Camera camera) {
        if (camera == null || camera.getParameters() == null)
            return null;

        final Camera.Parameters parameters = camera.getParameters();
        final Camera.Size size = parameters.getPreviewSize();

        int width = size.width;
        int height = size.height;
        if (ScanUtil.getOrientation(getContext()) == ScanUtil.ORIENTATION_PORTRAIT) {
            int tmp = width;
            width = height;
            height = tmp;
        }

        return getScanBoxAreaRect(height);
    }

    public int getMaskColor() {
        return mMaskColor;
    }

    public void setMaskColor(int maskColor) {
        mMaskColor = maskColor;
    }

    public int getCornerColor() {
        return mCornerColor;
    }

    public void setCornerColor(int cornerColor) {
        mCornerColor = cornerColor;
    }

    public int getCornerLength() {
        return mCornerLength;
    }

    public void setCornerLength(int cornerLength) {
        mCornerLength = cornerLength;
    }

    public int getCornerSize() {
        return mCornerSize;
    }

    public void setCornerSize(int cornerSize) {
        mCornerSize = cornerSize;
    }

    public int getRectWidth() {
        return mRectWidth;
    }

    public void setRectWidth(int rectWidth) {
        mRectWidth = rectWidth;
    }

    public int getRectHeight() {
        return mRectHeight;
    }

    public void setRectHeight(int rectHeight) {
        mRectHeight = rectHeight;
    }

    public int getTopOffset() {
        return mTopOffset;
    }

    public void setTopOffset(int topOffset) {
        mTopOffset = topOffset;
    }

    public int getScanLineSize() {
        return mScanLineSize;
    }

    public void setScanLineSize(int scanLineSize) {
        mScanLineSize = scanLineSize;
    }

    public int getScanLineColor() {
        return mScanLineColor;
    }

    public void setScanLineColor(int scanLineColor) {
        mScanLineColor = scanLineColor;
    }

    public int getScanLineMargin() {
        return mScanLineMargin;
    }

    public void setScanLineMargin(int scanLineMargin) {
        mScanLineMargin = scanLineMargin;
    }

    public int getBorderSize() {
        return mBorderSize;
    }

    public void setBorderSize(int borderSize) {
        mBorderSize = borderSize;
    }

    public int getBorderColor() {
        return mBorderColor;
    }

    public void setBorderColor(int borderColor) {
        mBorderColor = borderColor;
    }

    public int getAnimTime() {
        return mAnimTime;
    }

    public void setAnimTime(int animTime) {
        mAnimTime = animTime;
    }

    public boolean isCenterVertical() {
        return mIsCenterVertical;
    }

    public void setCenterVertical(boolean centerVertical) {
        mIsCenterVertical = centerVertical;
    }

    public int getToolbarHeight() {
        return mToolbarHeight;
    }

    public void setToolbarHeight(int toolbarHeight) {
        mToolbarHeight = toolbarHeight;
    }

    public String getTipText() {
        return mTipText;
    }

    public void setTipText(String tipText) {
        mTipText = tipText;

        if (!TextUtils.isEmpty(mTipText)) {
            if (mIsShowTipTextAsSingleLine) {
                mTipTextSl = new StaticLayout(mTipText, mTipPaint, ScanUtil.getScreenResolution(getContext()).x, Layout.Alignment.ALIGN_CENTER, 1.0f, 0, true);
            } else {
                mTipTextSl = new StaticLayout(mTipText, mTipPaint, mRectWidth - 2 * mTipBackgroundRadius, Layout.Alignment.ALIGN_CENTER, 1.0f, 0, true);
            }
        }
    }

    public int getTipTextColor() {
        return mTipTextColor;
    }

    public void setTipTextColor(int tipTextColor) {
        mTipTextColor = tipTextColor;
    }

    public int getTipTextSize() {
        return mTipTextSize;
    }

    public void setTipTextSize(int tipTextSize) {
        mTipTextSize = tipTextSize;
    }

    public boolean isTipTextBelowRect() {
        return mIsTipTextBelowRect;
    }

    public void setTipTextBelowRect(boolean tipTextBelowRect) {
        mIsTipTextBelowRect = tipTextBelowRect;
    }

    public int getTipTextMargin() {
        return mTipTextMargin;
    }

    public void setTipTextMargin(int tipTextMargin) {
        mTipTextMargin = tipTextMargin;
    }

    public boolean isShowTipTextAsSingleLine() {
        return mIsShowTipTextAsSingleLine;
    }

    public void setShowTipTextAsSingleLine(boolean showTipTextAsSingleLine) {
        mIsShowTipTextAsSingleLine = showTipTextAsSingleLine;
    }

    public boolean isShowTipBackground() {
        return mIsShowTipBackground;
    }

    public void setShowTipBackground(boolean showTipBackground) {
        mIsShowTipBackground = showTipBackground;
    }

    public int getTipBackgroundColor() {
        return mTipBackgroundColor;
    }

    public void setTipBackgroundColor(int tipBackgroundColor) {
        mTipBackgroundColor = tipBackgroundColor;
    }

    public boolean isScanLineReverse() {
        return mIsScanLineReverse;
    }

    public void setScanLineReverse(boolean scanLineReverse) {
        mIsScanLineReverse = scanLineReverse;
    }

    public float getHalfCornerSize() {
        return mHalfCornerSize;
    }

    public void setHalfCornerSize(float halfCornerSize) {
        mHalfCornerSize = halfCornerSize;
    }

    public StaticLayout getTipTextSl() {
        return mTipTextSl;
    }

    public void setTipTextSl(StaticLayout tipTextSl) {
        mTipTextSl = tipTextSl;
    }

    public int getTipBackgroundRadius() {
        return mTipBackgroundRadius;
    }

    public void setTipBackgroundRadius(int tipBackgroundRadius) {
        mTipBackgroundRadius = tipBackgroundRadius;
    }

    public boolean isOnlyDecodeScanBoxArea() {
        return mIsOnlyDecodeScanBoxArea;
    }

    public void setOnlyDecodeScanBoxArea(boolean onlyDecodeScanBoxArea) {
        mIsOnlyDecodeScanBoxArea = onlyDecodeScanBoxArea;
    }
}