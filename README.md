# Scan-Android
扫描条形码+二维码+身份证 接口插件方式导入 参考开源项目BGAQRCode-Android

分离条形码、二维码扫描框（扫描框 可自定义 冬天加入）

        scanView2.addScanBoxView(View.inflate(this, R.layout.layout_scanbox_bar, null));
        scanView2.getScanBoxView().setTipText("将条形码放入框中");

动态增加扫描的库 也可自定义增加


        scanView2.removeHandleScanDataListenerAll();
        scanView2.addHandleScanDataListener(new ZXingScan(new ZXingScan.IZXingResultListener() {
            @Override
            public boolean onScanResult(BarcodeFormat codeFormat, String result) {
                onScanQRCodeSuccess("result:" + result + "  ZXingScan:" + codeFormat.name());
                return false;
            }
        }));

        scanView2.addHandleScanDataListener(new ZBarScan(new ZBarScan.IZbarResultListener() {
            @Override
            public boolean onScanResult(ZbarBarcodeFormat codeFormat, String result) {
                onScanQRCodeSuccess("result:" + result + "  ZBarScan:" + codeFormat.getName());
                return false;
            }
        }));

        scanView2.addHandleScanDataListener(new IDCardScan(new IDCardScan.IIDCardResultListener() {//身份证
            @Override
            public void onScanResult(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onScanQRCodeSuccess("result:" + result + "   IDCardScan");
                    }
                });
            }
        })
