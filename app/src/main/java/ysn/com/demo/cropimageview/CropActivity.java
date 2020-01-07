package ysn.com.demo.cropimageview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Arrays;

import ysn.com.view.cropimageview.CropImageView;
import ysn.com.view.cropimageview.listener.OnCropMultiListener;
import ysn.com.view.cropimageview.mode.CropMode;
import ysn.com.view.cropimageview.mode.RotateAngle;
import ysn.com.view.cropimageview.utils.FileUtils;
import ysn.com.view.cropimageview.utils.Utils;

/**
 * @Author yangsanning
 * @ClassName CropActivity
 * @Description 一句话概括作用
 * @Date 2020/1/7
 * @History 2020/1/7 author: description:
 */
public class CropActivity extends AppCompatActivity implements View.OnClickListener, OnCropMultiListener {

    public static final int REQUEST_CODE_SELECT = 100;

    RecyclerView cropModeRecyclerView;
    RecyclerView angleRecyclerView;
    CropImageView cropImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        cropImageView = findViewById(R.id.crop_activity_crop_image_view);
        cropImageView.setOnCropMultiListener(this);

        initCropModeAdapter();
        initAngleAdapter();

        findViewById(R.id.crop_activity_select_image).setOnClickListener(this);
        findViewById(R.id.crop_activity_crop).setOnClickListener(this);
    }

    private void initCropModeAdapter() {
        StringAdapter cropModeAdapter = new StringAdapter();
        cropModeAdapter.setOnItemClickListener((adapter, view, position) ->
            cropImageView.setCropMode(CropMode.getValue(position)));
        cropModeRecyclerView = findViewById(R.id.crop_activity_crop_mode_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayout.HORIZONTAL);
        cropModeRecyclerView.setLayoutManager(layoutManager);
        cropModeRecyclerView.setAdapter(cropModeAdapter);
        cropModeAdapter.setNewData(Arrays.asList(getResources().getStringArray(R.array.crop_mode)));
    }

    private void initAngleAdapter() {
        int[] angles = getResources().getIntArray(R.array.angle_value);
        StringAdapter angleAdapter = new StringAdapter();
        angleAdapter.setOnItemClickListener((adapter, view, position) ->
            cropImageView.rotateImage(RotateAngle.getValue(angles[position])));
        angleRecyclerView = findViewById(R.id.crop_activity_angle_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayout.HORIZONTAL);
        angleRecyclerView.setLayoutManager(layoutManager);
        angleRecyclerView.setAdapter(angleAdapter);
        angleAdapter.setNewData(Arrays.asList(getResources().getStringArray(R.array.angle)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.crop_activity_select_image:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_SELECT);
                break;
            case R.id.crop_activity_crop:
                cropImageView.crop(FileUtils.getYsnUri(CropActivity.this, Bitmap.CompressFormat.PNG));
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoadSuccess() {

    }

    @Override
    public void onCropSuccess(Bitmap cropBitmap) {

    }

    @Override
    public void onCropSaveSuccess(Uri saveUri) {
        Intent intent = new Intent();
        intent.setData(saveUri);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onError(Throwable e) {

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_SELECT) {
            cropImageView.load(result.getData(), true);
        }
    }
}
