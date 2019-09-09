package com.uc56.scan_android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.zxing.BarcodeFormat;
import com.uc56.scancore.ScanView;
import com.uc56.scancore.ZbarBarcodeFormat;
import com.uc56.scancore.zbar.ZBarScan;
import com.uc56.scancore.zxing.QRCodeDecoder;
import com.uc56.scancore.zxing.ZXingScan;

import java.io.ByteArrayOutputStream;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity;

public class TestScanActivity extends AppCompatActivity {
    private static final String TAG = TestScanActivity.class.getSimpleName();
    private static final int REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY = 666;

    private ScanView mQRCodeView;
    private ImageView imageView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_scan);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mQRCodeView = (ScanView) findViewById(R.id.zxingview);
        imageView = (ImageView) findViewById(R.id.img_camera);
        test();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mQRCodeView.startCamera();
        mQRCodeView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mQRCodeView.startSpotAndShowRect();
            }
        }, 300);
    }

    @Override
    protected void onStop() {
        mQRCodeView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mQRCodeView.onDestroy();
        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    public void onScanQRCodeSuccess(String result) {
        Log.i(TAG, "result:" + result);
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        vibrate();
        mQRCodeView.startSpot();
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_spot:
                mQRCodeView.startSpot();
                break;
            case R.id.stop_spot:
                mQRCodeView.stopSpot();
                break;
            case R.id.start_spot_showrect:
                mQRCodeView.startSpotAndShowRect();
                break;
            case R.id.stop_spot_hiddenrect:
                mQRCodeView.stopSpotAndHiddenRect();
                break;
            case R.id.show_rect:
                mQRCodeView.showScanRect();
                break;
            case R.id.hidden_rect:
                mQRCodeView.hiddenScanRect();
                break;
            case R.id.start_preview:
                mQRCodeView.startCamera();
                break;
            case R.id.stop_preview:
                mQRCodeView.stopCamera();
                break;
            case R.id.open_flashlight:
                mQRCodeView.openFlashlight();
                break;
            case R.id.close_flashlight:
                mQRCodeView.closeFlashlight();
                break;
            case R.id.scan_barcode:
                test();
                mQRCodeView.addScanBoxView(View.inflate(this, R.layout.layout_scanbox_bar, null));
                mQRCodeView.getScanBoxView().setTipText("将条形码放入框中");

                break;
            case R.id.scan_qrcode:
                test();
                mQRCodeView.addScanBoxView(View.inflate(this, R.layout.layout_scanbox_qrcode, null));
                mQRCodeView.getScanBoxView().setTipText("将二维码放入框中");
                break;
            case R.id.choose_qrcde_from_gallery:
                /*
                从相册选取二维码图片，这里为了方便演示，使用的是
                https://github.com/bingoogolapple/BGAPhotoPicker-Android
                这个库来从图库中选择二维码图片，这个库不是必须的，你也可以通过自己的方式从图库中选择图片
                 */
                startActivityForResult(BGAPhotoPickerActivity.newIntent(this, null, 1, null, false), REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY);
                break;
        }
    }

    private void test() {
        try {
            mQRCodeView.addScanBoxView(View.inflate(this, R.layout.layout_scanbox_qrcode, null));
            mQRCodeView.getScanBoxView().setTipText("将证件放入框中");
            mQRCodeView.getScanBoxView().setOnlyDecodeScanBoxArea(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        mQRCodeView.removeHandleScanDataListenerAll();
        mQRCodeView.addHandleScanDataListener(new ZXingScan(new ZXingScan.IZXingResultListener() {
            @Override
            public boolean onScanResult(BarcodeFormat codeFormat, String result) {
                onScanQRCodeSuccess("ZXingScan:" + result + "  " + codeFormat.name());
                return false;
            }
        }));

        mQRCodeView.addHandleScanDataListener(new ZBarScan(new ZBarScan.IZbarResultListener() {
            @Override
            public boolean onScanResult(ZbarBarcodeFormat codeFormat, String result) {
                onScanQRCodeSuccess("ZBarScan:" + result + "  " + codeFormat.getName());
                return false;
            }
        }));

        mQRCodeView.addHandleScanDataListener(new IDCardScan(new IDCardScan.IIDCardResultListener() {//身份证
            @Override
            public void onScanResult(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onScanQRCodeSuccess("IDCardScan:" + result);
                    }
                });
            }
        }) {
            @Override
            public Boolean onHandleScanData(final byte[] previewData, final byte[] desData, final int format, int width, int height, Rect rect) {
                super.onHandleScanData(previewData, desData, format, width, height, rect);
//                int temp = width;
//                width = height;
//                height = temp;
                byte[] data = new byte[previewData.length];
                ScanView.rotateYUV240SP(previewData, data, height, width);//旋转

                try {
                    ByteArrayOutputStream baos;
                    byte[] rawImage;
                    BitmapFactory.Options newOpts = new BitmapFactory.Options();
                    newOpts.inJustDecodeBounds = true;
                    YuvImage yuvimage = new YuvImage(
                            data,
                            ImageFormat.NV21,//YUV240SP
                            width,
                            height,
                            null);
                    baos = new ByteArrayOutputStream();
                    yuvimage.compressToJpeg(rect, 100, baos);// 80--JPG图片的质量[0-100],100最高
                    rawImage = baos.toByteArray();
                    //将rawImage转换成bitmap
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mQRCodeView.showScanRect();

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY) {
            final String picturePath = BGAPhotoPickerActivity.getSelectedImages(data).get(0);

            /*
            这里为了偷懒，就没有处理匿名 AsyncTask 内部类导致 Activity 泄漏的问题
            请开发在使用时自行处理匿名内部类导致Activity内存泄漏的问题，处理方式可参考 https://github.com/GeniusVJR/LearningNotes/blob/master/Part1/Android/Android%E5%86%85%E5%AD%98%E6%B3%84%E6%BC%8F%E6%80%BB%E7%BB%93.md
             */
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    return QRCodeDecoder.syncDecodeQRCode(picturePath);
                }

                @Override
                protected void onPostExecute(String result) {
                    if (TextUtils.isEmpty(result)) {
                        Toast.makeText(TestScanActivity.this, "未发现二维码", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TestScanActivity.this, result, Toast.LENGTH_SHORT).show();
                    }
                }
            }.execute();
        }
    }
}