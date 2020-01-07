package ysn.com.view.cropimageview.utils;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.opengl.GLES10;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author yangsanning
 * @ClassName Utils
 * @Description 辅助工具类
 * @Date 2020/1/7
 * @History 2020/1/7 author: description:
 */
public class Utils {

    private static final int SIZE_DEFAULT = 2048;
    private static final int SIZE_LIMIT = 4096;

    public static Uri saveImage(Context context, Bitmap bitmap, Uri sourceUri, final Uri saveUri,
                                Bitmap.CompressFormat compressFormat, int compressQuality) throws IOException, IllegalStateException {
        if (saveUri == null) {
            throw new IllegalStateException("uri 不能为 null!");
        }

        OutputStream outputStream = null;
        try {
            outputStream = context.getContentResolver().openOutputStream(saveUri);
            bitmap.compress(compressFormat, compressQuality, outputStream);
            copyExifInfo(context, sourceUri, saveUri, bitmap.getWidth(), bitmap.getHeight());
            updateGalleryInfo(context, saveUri);
            return saveUri;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static void copyExifInfo(Context context, Uri sourceUri, Uri saveUri, int outputWidth, int outputHeight) {
        if (sourceUri == null || saveUri == null) {
            return;
        }
        try {
            File sourceFile = UriUtils.getFileFromUri(context, sourceUri);
            File saveFile = UriUtils.getFileFromUri(context, saveUri);
            if (sourceFile == null || saveFile == null) {
                return;
            }
            String sourcePath = sourceFile.getAbsolutePath();
            String savePath = saveFile.getAbsolutePath();

            ExifInterface sourceExif = new ExifInterface(sourcePath);
            List<String> tags = new ArrayList<>();
            tags.add(ExifInterface.TAG_DATETIME);
            tags.add(ExifInterface.TAG_FLASH);
            tags.add(ExifInterface.TAG_FOCAL_LENGTH);
            tags.add(ExifInterface.TAG_GPS_ALTITUDE);
            tags.add(ExifInterface.TAG_GPS_ALTITUDE_REF);
            tags.add(ExifInterface.TAG_GPS_DATESTAMP);
            tags.add(ExifInterface.TAG_GPS_LATITUDE);
            tags.add(ExifInterface.TAG_GPS_LATITUDE_REF);
            tags.add(ExifInterface.TAG_GPS_LONGITUDE);
            tags.add(ExifInterface.TAG_GPS_LONGITUDE_REF);
            tags.add(ExifInterface.TAG_GPS_PROCESSING_METHOD);
            tags.add(ExifInterface.TAG_GPS_TIMESTAMP);
            tags.add(ExifInterface.TAG_MAKE);
            tags.add(ExifInterface.TAG_MODEL);
            tags.add(ExifInterface.TAG_WHITE_BALANCE);
            tags.add(ExifInterface.TAG_EXPOSURE_TIME);
            tags.add(ExifInterface.TAG_APERTURE);
            tags.add(ExifInterface.TAG_ISO);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tags.add(ExifInterface.TAG_DATETIME_DIGITIZED);
                tags.add(ExifInterface.TAG_SUBSEC_TIME);
                tags.add(ExifInterface.TAG_SUBSEC_TIME_DIG);
                tags.add(ExifInterface.TAG_SUBSEC_TIME_ORIG);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tags.add(ExifInterface.TAG_F_NUMBER);
                tags.add(ExifInterface.TAG_ISO_SPEED_RATINGS);
                tags.add(ExifInterface.TAG_SUBSEC_TIME_DIGITIZED);
                tags.add(ExifInterface.TAG_SUBSEC_TIME_ORIGINAL);
            }

            ExifInterface saveExif = new ExifInterface(savePath);
            String value;
            for (String tag : tags) {
                value = sourceExif.getAttribute(tag);
                if (!TextUtils.isEmpty(value)) {
                    saveExif.setAttribute(tag, value);
                }
            }
            saveExif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, String.valueOf(outputWidth));
            saveExif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, String.valueOf(outputHeight));
            saveExif.setAttribute(ExifInterface.TAG_ORIENTATION,
                String.valueOf(ExifInterface.ORIENTATION_UNDEFINED));

            saveExif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateGalleryInfo(Context context, Uri uri) {
        if (!ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            return;
        }

        ContentValues values = new ContentValues();
        File file = UriUtils.getFileFromUri(context, uri);
        if (file != null && file.exists()) {
            values.put(MediaStore.Images.Media.SIZE, file.length());
        }
        ContentResolver resolver = context.getContentResolver();
        resolver.update(uri, values, null, null);
    }

    public static int getExifOrientation(Context context, Uri uri) {
        String authority = uri.getAuthority().toLowerCase();
        int orientation;
        if (authority.endsWith("media")) {
            orientation = getExifRotation(context, uri);
        } else {
            orientation = getExifRotation(UriUtils.getFileFromUri(context, uri));
        }
        return orientation;
    }

    public static int getExifRotation(Context context, Uri uri) {
        Cursor cursor = null;
        String[] projection = {MediaStore.Images.ImageColumns.ORIENTATION};
        try {
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                return 0;
            }
            return cursor.getInt(0);
        } catch (RuntimeException ignored) {
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int getExifRotation(File file) {
        if (file == null) {
            return 0;
        }
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            return getRotateDegreeFromOrientation(
                exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED));
        } catch (IOException e) {
        }
        return 0;
    }

    public static int getRotateDegreeFromOrientation(int orientation) {
        int degree = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
            default:
                break;
        }
        return degree;
    }

    public static int getMaxSize() {
        int maxSize = SIZE_DEFAULT;
        int[] arr = new int[1];
        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, arr, 0);
        if (arr[0] > 0) {
            maxSize = Math.min(arr[0], SIZE_LIMIT);
        }
        return maxSize;
    }
}
