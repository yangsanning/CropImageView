package ysn.com.view.cropimageview.listener;

import android.graphics.Bitmap;

/**
 * @Author yangsanning
 * @ClassName OnCropListener
 * @Description 裁剪监听
 * @Date 2020/1/7
 * @History 2020/1/7 author: description:
 */
public interface OnCropListener extends BaseCropListener {

    void onCropSuccess(Bitmap cropBitmap);
}

