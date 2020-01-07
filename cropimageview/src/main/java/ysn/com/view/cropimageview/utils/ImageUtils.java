package ysn.com.view.cropimageview.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static android.graphics.Bitmap.createBitmap;

/**
 * @Author yangsanning
 * @ClassName ImageUtils
 * @Description 一句话概括作用
 * @Date 2020/1/7
 * @History 2020/1/7 author: description:
 */
public class ImageUtils {

    /**
     * 正方形转圆形
     *
     * @param squareBitmap 正方形Bitmap
     * @return 圆形bitmap
     */
    public static Bitmap getCircleBitmap(Bitmap squareBitmap) {
        if (squareBitmap == null) {
            return null;
        }
        Bitmap output =
            Bitmap.createBitmap(squareBitmap.getWidth(), squareBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        final Rect rect = new Rect(0, 0, squareBitmap.getWidth(), squareBitmap.getHeight());
        Canvas canvas = new Canvas(output);

        int halfWidth = squareBitmap.getWidth() / 2;
        int halfHeight = squareBitmap.getHeight() / 2;

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        canvas.drawCircle(halfWidth, halfHeight, Math.min(halfWidth, halfHeight), paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(squareBitmap, rect, rect, paint);
        return output;
    }

    /**
     * 获取旋转bitmap
     */
    public static Bitmap getRotateBitmap(Bitmap bitmap, float angle) {
        Matrix rotateMatrix = new Matrix();
        rotateMatrix.setRotate(angle, (bitmap.getWidth() / 2f), (bitmap.getHeight() / 2f));
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotateMatrix, Boolean.TRUE);
    }

    /**
     * 根据高度获取缩放bitmap
     */
    public static Bitmap getScaleBitmapForHeight(Bitmap bitmap, int outHeight) {
        float currentWidth = bitmap.getWidth();
        float currentHeight = bitmap.getHeight();
        float ratio = currentWidth / currentHeight;
        int outWidth = Math.round(outHeight * ratio);
        return getScaledBitmap(bitmap, outWidth, outHeight);
    }

    /**
     * 根据宽度获取缩放bitmap
     */
    public static Bitmap getScaleBitmapForWidth(Bitmap bitmap, int outWidth) {
        float currentWidth = bitmap.getWidth();
        float currentHeight = bitmap.getHeight();
        float ratio = currentWidth / currentHeight;
        int outHeight = Math.round(outWidth / ratio);
        return getScaledBitmap(bitmap, outWidth, outHeight);
    }

    /**
     * 获取缩放bitmap
     */
    public static Bitmap getScaledBitmap(Bitmap bitmap, int outWidth, int outHeight) {
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.postScale(((float) outWidth / (float) bitmapWidth), ((float) outHeight / (float) bitmapHeight));
        return createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, scaleMatrix, true);
    }

    public static Bitmap getBitmapFromUri(Context context, Uri uri, int requestSize) {
        InputStream inputStream = null;
        Bitmap bitmap = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = getInSampleSize(context, uri, requestSize);
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            close(inputStream);
        }
        return bitmap;
    }

    /**
     * 获取InSampleSize
     */
    public static int getInSampleSize(Context context, Uri sourceUri, int requestSize) {
        InputStream inputStream = null;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            inputStream = context.getContentResolver().openInputStream(sourceUri);
            BitmapFactory.decodeStream(inputStream, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            close(inputStream);
        }
        int inSampleSize = 1;
        while (options.outWidth / inSampleSize > requestSize || options.outHeight / inSampleSize > requestSize) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    private static void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
