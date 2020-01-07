package ysn.com.view.cropimageview.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @Author yangsanning
 * @ClassName UriUtils
 * @Description Uri工具类
 * @Date 2020/1/7
 * @History 2020/1/7 author: description:
 */
public class UriUtils {

    public static Uri getUriFromDrawableResId(Context context, int drawableResId) {
        String builder = ContentResolver.SCHEME_ANDROID_RESOURCE +
            "://" +
            context.getResources().getResourcePackageName(drawableResId) +
            "/" +
            context.getResources().getResourceTypeName(drawableResId) +
            "/" +
            context.getResources().getResourceEntryName(drawableResId);
        return Uri.parse(builder);
    }

    /**
     * 根据uri获取文件
     */
    public static File getFileFromUri(final Context context, final Uri uri) {
        String filePath = null;
        final boolean isKitkat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitkat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (UriUtils.isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    filePath = Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (UriUtils.isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                // String "id" may not represent a valid Long type data, it may equals to
                // something like "raw:/storage/emulated/0/Download/some_file" instead.
                // Doing a check before passing the "id" to Long.valueOf(String) would be much safer.
                if (RawDocumentsHelper.isRawDocId(id)) {
                    filePath = RawDocumentsHelper.getAbsoluteFilePath(id);
                } else {
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    filePath = getDataColumn(context, contentUri, null, null);
                }
            }
            // MediaProvider
            else if (UriUtils.isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                    split[1]
                };
                filePath = getDataColumn(context, contentUri, selection, selectionArgs);
            } else if (UriUtils.isGoogleDriveDocument(uri)) {
                return getGoogleDriveFile(context, uri);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (UriUtils.isGooglePhotosUri(uri)) {
                filePath = uri.getLastPathSegment();
            } else {
                filePath = getDataColumn(context, uri, null, null);
            }
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        if (filePath != null) {
            return new File(filePath);
        }
        return null;
    }

    // A copy of com.android.providers.downloads.RawDocumentsHelper since it is invisibility.
    public static class RawDocumentsHelper {
        public static final String RAW_PREFIX = "raw:";

        public static boolean isRawDocId(String docId) {
            return docId != null && docId.startsWith(RAW_PREFIX);
        }

        public static String getDocIdForFile(File file) {
            return RAW_PREFIX + file.getAbsolutePath();
        }

        public static String getAbsoluteFilePath(String rawDocumentId) {
            return rawDocumentId.substring(RAW_PREFIX.length());
        }
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String[] projection = {
            MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int columnIndex =
                    (uri.toString().startsWith("content://com.google.android.gallery3d"))
                        ? cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                        : cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                if (columnIndex != -1) {
                    return cursor.getString(columnIndex);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * @param context The context
     * @param uri     The Uri of Google Drive file
     * @return Google Drive file
     */
    private static File getGoogleDriveFile(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        FileInputStream input = null;
        FileOutputStream output = null;
        String filePath = new File(context.getCacheDir(), "tmp").getAbsolutePath();
        try {
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
            if (pfd == null) {
                return null;
            }
            FileDescriptor fd = pfd.getFileDescriptor();
            input = new FileInputStream(fd);
            output = new FileOutputStream(filePath);
            int read;
            byte[] bytes = new byte[4096];
            while ((read = input.read(bytes)) != -1) {
                output.write(bytes, 0, read);
            }
            return new File(filePath);
        } catch (IOException ignored) {
        } finally {
            close(input);
            close(output);
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check
     * @return Whether the Uri authority is Google Drive.
     */
    public static boolean isGoogleDriveDocument(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority());
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
