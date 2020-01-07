package ysn.com.view.cropimageview.mode;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

/**
 * @Author yangsanning
 * @ClassName SavedState
 * @Description 一句话概括作用
 * @Date 2020/1/7
 * @History 2020/1/7 author: description:
 */
public class SavedState extends View.BaseSavedState {

    public float angle;

    public float gridMinSize;
    public float gridStroke;
    public int gridColor;
    public boolean isCropEnabled;
    public CropMode cropMode;

    public float girdLineStroke;
    public int girdRowPart;
    public int girdColumnPart;
    public boolean isShowGridLine;
    public GridLineMode gridLineMode;

    public int dragPointSize;
    public int dragPointPadding;
    public boolean isShowDragPoint;

    public int bgColor;
    public int maskColor;

    public boolean isLockCropArea;

    public Uri sourceUri;
    public Uri saveUri;
    public int exifRotation;
    public int outputMaxWidth;
    public int outputMaxHeight;
    public int outputWidth;
    public int outputHeight;
    public int compressQuality;
    public float customCropRatioX;
    public float customCropRatioY;
    public Bitmap.CompressFormat compressFormat;

    public int animDuration;
    public boolean isAnimEnabled;

    public SavedState(Parcelable superState) {
        super(superState);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public SavedState createFromParcel(final Parcel inParcel) {
            return new SavedState(inParcel);
        }

        @Override
        public SavedState[] newArray(final int inSize) {
            return new SavedState[inSize];
        }
    };

    private SavedState(Parcel in) {
        super(in);
        angle = in.readFloat();

        gridMinSize = in.readFloat();
        gridStroke = in.readFloat();
        gridColor = in.readInt();
        isCropEnabled = (in.readInt() != 0);
        cropMode = (CropMode) in.readSerializable();

        girdLineStroke = in.readFloat();
        girdRowPart = in.readInt();
        girdColumnPart = in.readInt();
        isShowGridLine = (in.readInt() != 0);
        gridLineMode = (GridLineMode) in.readSerializable();

        dragPointSize = in.readInt();
        dragPointPadding = in.readInt();
        isShowDragPoint = (in.readInt() != 0);

        isLockCropArea = (in.readInt() != 0);

        bgColor = in.readInt();
        maskColor = in.readInt();

        sourceUri = in.readParcelable(Uri.class.getClassLoader());
        saveUri = in.readParcelable(Uri.class.getClassLoader());
        exifRotation = in.readInt();
        outputMaxWidth = in.readInt();
        outputMaxHeight = in.readInt();
        outputWidth = in.readInt();
        outputHeight = in.readInt();
        compressQuality = in.readInt();
        customCropRatioX = in.readFloat();
        customCropRatioY = in.readFloat();
        compressFormat = (Bitmap.CompressFormat) in.readSerializable();

        animDuration = in.readInt();
        isAnimEnabled = (in.readInt() != 0);
    }

    @Override
    public void writeToParcel(Parcel out, int flag) {
        super.writeToParcel(out, flag);
        out.writeFloat(angle);

        out.writeFloat(gridMinSize);
        out.writeFloat(gridStroke);
        out.writeInt(gridColor);
        out.writeInt(isCropEnabled ? 1 : 0);
        out.writeSerializable(cropMode);

        out.writeFloat(girdLineStroke);
        out.writeInt(girdRowPart);
        out.writeInt(girdColumnPart);
        out.writeInt(isShowGridLine ? 1 : 0);
        out.writeSerializable(gridLineMode);

        out.writeInt(dragPointSize);
        out.writeInt(dragPointPadding);
        out.writeInt(isShowDragPoint ? 1 : 0);

        out.writeInt(isLockCropArea ? 1 : 0);

        out.writeInt(bgColor);
        out.writeInt(maskColor);

        out.writeParcelable(sourceUri, flag);
        out.writeParcelable(saveUri, flag);
        out.writeInt(exifRotation);
        out.writeInt(outputMaxWidth);
        out.writeInt(outputMaxHeight);
        out.writeInt(outputWidth);
        out.writeInt(outputHeight);
        out.writeInt(compressQuality);
        out.writeFloat(customCropRatioX);
        out.writeFloat(customCropRatioY);
        out.writeSerializable(compressFormat);

        out.writeInt(isAnimEnabled ? 1 : 0);
        out.writeInt(animDuration);
    }
}
