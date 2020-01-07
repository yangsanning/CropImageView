package ysn.com.view.cropimageview.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

/**
 * @Author yangsanning
 * @ClassName FileUtils
 * @Description 文件工具类
 * @Date 2020/1/7
 * @History 2020/1/7 author: description:
 */
public class FileUtils {

    private final static String ROOT_FOLDER = "ysn";
    private final static String IMAGE_FOLDER = "image";

    /**
     * 获取图片uri
     */
    public static Uri getYsnUri(Context context, Bitmap.CompressFormat format) {
        Date date = new Date();
        long time = date.getTime() / 1000;
        String mimeType = getMimeType(format);
        return getImageUri(context, FileUtils.getImageFile(date, mimeType), time);
    }

    /**
     * 获取图片uri
     */
    public static Uri getImageUri(Context context, File file, long time) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, file.getName());
        values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + getMimeType(file.getName()));
        values.put(MediaStore.Images.Media.DATA, file.getPath());
        values.put(MediaStore.MediaColumns.DATE_ADDED, time);
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, time);
        if (file.exists()) {
            values.put(MediaStore.Images.Media.SIZE, file.length());
        }

        ContentResolver resolver = context.getContentResolver();
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    /**
     * 获取图片File
     */
    public static File getImageFile(Date date, String mimeType) {
        return new File(getImageFolderFile(), ("/" + ROOT_FOLDER + TimeUtils.getAllFormat(date) + "." + mimeType));
    }

    /**
     * 获取图片类型
     */
    public static String getMimeType(String filename) {
        String mimeType = "png";
        Path path;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            path = Paths.get(filename);
            try {
                mimeType = Files.probeContentType(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mimeType;
    }

    /**
     * 获取图片类型
     */
    public static String getMimeType(Bitmap.CompressFormat format) {
        switch (format) {
            case JPEG:
                return "jpeg";
            case PNG:
                return "png";
            default:
                break;
        }
        return "png";
    }

    /**
     * 获取图片文件夹File
     */
    public static File getImageFolderFile() {
        return getFolderFile(IMAGE_FOLDER);
    }

    /**
     * 获取指定文件夹File
     */
    public static File getFolderFile(String folderName) {
        File generalPath = getRootFile();
        if (generalPath == null) {
            return null;
        }
        File file = new File(generalPath, folderName);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

    /**
     * 获取跟目录File
     */
    public static File getRootFile() {
        File dirs = getSDFile();
        if (dirs == null) {
            return null;
        }
        File file = new File(dirs, ROOT_FOLDER);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

    /**
     * 获取SDFile
     */
    public static File getSDFile() {
        File dirs = null;
        //判断sd卡是否存在
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            //获取跟目录
            dirs = Environment.getExternalStorageDirectory();
        }
        return dirs;
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String url) {
        try {
            return new File(url).getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
