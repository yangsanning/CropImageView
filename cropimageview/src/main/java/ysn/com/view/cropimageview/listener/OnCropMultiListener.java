package ysn.com.view.cropimageview.listener;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * @Author yangsanning
 * @ClassName OnCropMultiListener
 * @Description 所以控件事件监听(使用它时, 其他监听将失效)
 * @Date 2020/1/7
 * @History 2020/1/7 author: description:
 */
public interface OnCropMultiListener extends BaseCropListener {

    void onLoadSuccess();

    void onCropSuccess(Bitmap cropBitmap);

    void onCropSaveSuccess(Uri saveUri);
}
