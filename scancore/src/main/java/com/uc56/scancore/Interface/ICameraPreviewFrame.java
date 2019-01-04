package com.uc56.scancore.Interface;

public interface ICameraPreviewFrame {

    void onPreviewFrame(final ICameraP iCameraP, final byte[] previewData, final int format, final int previewWidth, final int previewHeight);

}