package com.pao11.imagepicker.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * 用于处理文件的工具类
 * Created by apple on 2018/7/2.
 */

public class FileUtil {

    private static final int MAX_EXTENSION_LENGTH = 4;
    private static final String INDIVIDUAL_DIR_NAME = "video-thumbnail-cache";


    public static File getIndividualCacheDirectory(Context context) {
        File cacheDir = getCacheDirectory(context, true);
        return new File(cacheDir, INDIVIDUAL_DIR_NAME);
    }

    private static File getCacheDirectory(Context context, boolean preferExternal) {
        File appCacheDir = null;
        String externalStorageState;
        try {
            externalStorageState = Environment.getExternalStorageState();
        } catch (NullPointerException e) { // (sh)it happens
            externalStorageState = "";
        }
        if (preferExternal && MEDIA_MOUNTED.equals(externalStorageState)) {
//            appCacheDir = getExternalCacheDir(context);
            appCacheDir = context.getExternalCacheDir();
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if (appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/cache/";
            appCacheDir = new File(cacheDirPath);
        }
        return appCacheDir;
    }

    private static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                return null;
            }
        }
        return appCacheDir;
    }

    public static String Md5FileNameGenerate(String url, String extension) {
        if (TextUtils.isEmpty(extension)) {
            extension = getExtension(url);
        }
        String name = computeMD5(url);
        return TextUtils.isEmpty(extension) ? name : name + "." + extension;
    }

    public static String getExtension(String url) {
        int dotIndex = url.lastIndexOf('.');
        int slashIndex = url.lastIndexOf('/');
        return dotIndex != -1 && dotIndex > slashIndex && dotIndex + 2 + MAX_EXTENSION_LENGTH > url.length() ?
                url.substring(dotIndex + 1, url.length()) : "";
    }

    private static String computeMD5(String string) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] digestBytes = messageDigest.digest(string.getBytes());
            return bytesToHexString(digestBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 写图片文件到SD卡
     *
     * @throws IOException
     */
    public static void saveImageToSD(String filePath, Bitmap bitmap, int quality) {
        if (bitmap != null) {
            File file = new File(filePath.substring(0,
                    filePath.lastIndexOf(File.separator)));

            if (!file.exists()) {
                file.mkdirs();
            }
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(
                        new FileOutputStream(filePath));
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
                bos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bos != null){
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
