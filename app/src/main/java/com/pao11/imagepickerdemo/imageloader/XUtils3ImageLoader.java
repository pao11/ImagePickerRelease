package com.pao11.imagepickerdemo.imageloader;

/**
 * ================================================
 * 版    本：1.0
 * 创建日期：2018/6/28
 * ================================================
 */

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import com.pao11.imagepicker.loader.ImageLoader;

import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.io.File;

public class XUtils3ImageLoader implements ImageLoader {
    @Override
    public void displayImage(Activity activity, String path, ImageView imageView, int width, int height) {
        ImageOptions options = new ImageOptions.Builder()//
                .setLoadingDrawableId(com.pao11.imagepickerdemo.R.drawable.ic_default_image)//
                .setFailureDrawableId(com.pao11.imagepickerdemo.R.drawable.ic_default_image)//
                .setConfig(Bitmap.Config.RGB_565)//
                .setSize(width, height)//
                .setCrop(false)//
                .setUseMemCache(true)//
                .build();
        x.image().bind(imageView, Uri.fromFile(new File(path)).toString(), options);
    }

    @Override
    public void displayImage(Activity activity, Uri uri, ImageView imageView, int width, int height) {
        ImageOptions options = new ImageOptions.Builder()//
                .setLoadingDrawableId(com.pao11.imagepickerdemo.R.drawable.ic_default_image)//
                .setFailureDrawableId(com.pao11.imagepickerdemo.R.drawable.ic_default_image)//
                .setConfig(Bitmap.Config.RGB_565)//
                .setSize(width, height)//
                .setCrop(false)//
                .setUseMemCache(true)//
                .build();
        x.image().bind(imageView, uri.toString(), options);
    }

    @Override
    public void displayImagePreview(Activity activity, String path, ImageView imageView, int width, int height) {
        ImageOptions options = new ImageOptions.Builder()//
                .setConfig(Bitmap.Config.RGB_565)//
                .setSize(width, height)//
                .setCrop(false)//
                .setUseMemCache(true)//
                .build();
        x.image().bind(imageView, Uri.fromFile(new File(path)).toString(), options);
    }

    @Override
    public void displayImagePreview(Activity activity, Uri uri, ImageView imageView, int width, int height) {
        ImageOptions options = new ImageOptions.Builder()//
                .setConfig(Bitmap.Config.RGB_565)//
                .setSize(width, height)//
                .setCrop(false)//
                .setUseMemCache(true)//
                .build();
        x.image().bind(imageView, uri.toString(), options);
    }

    @Override
    public void clearMemoryCache() {
    }
}
