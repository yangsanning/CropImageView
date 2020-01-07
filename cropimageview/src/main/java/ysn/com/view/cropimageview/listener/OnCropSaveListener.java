package ysn.com.view.cropimageview.listener;

import android.net.Uri;

/**
 * @Author yangsanning
 * @ClassName OnCropSaveListener
 * @Description 裁剪保存监听
 * @Date 2020/1/7
 * @History 2020/1/7 author: description:
 */
public interface OnCropSaveListener extends BaseCropListener {

    void onCropSaveSuccess(Uri saveUri);
}

