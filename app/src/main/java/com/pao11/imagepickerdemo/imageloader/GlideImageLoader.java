package com.pao11.imagepickerdemo.imageloader;

import android.app.Activity;
import android.net.Uri;
import android.widget.ImageView;

import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.pao11.imagepicker.loader.ImageLoader;
import com.pao11.imagepicker.util.ProviderUtil;
import com.pao11.imagepickerdemo.R;

import java.io.File;

/**
 * ================================================
 * 版    本：1.0
 * 创建日期：2018/6/19
 * ================================================
 */
public class GlideImageLoader implements ImageLoader {

    @Override
    public void displayImage(Activity activity, String path, ImageView imageView, int width, int height) {

        Glide.with(activity)                             //配置上下文
                .load(Uri.fromFile(new File(path)))      //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
                .error(R.drawable.ic_default_image)           //设置错误图片
                .placeholder(R.drawable.ic_default_image)     //设置占位图片
//                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存全尺寸
                .override(width, height)
                .into(imageView);
    }

    @Override
    public void displayImage(Activity activity, Uri uri, ImageView imageView, int width, int height) {
        Glide.with(activity)                             //配置上下文
                .load(uri)      //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
                .error(R.drawable.ic_default_image)           //设置错误图片
                .placeholder(R.drawable.ic_default_image)     //设置占位图片
//                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存全尺寸
                .override(width, height)
                .into(imageView);
    }

    @Override
    public void displayImagePreview(Activity activity, String path, ImageView imageView, int width, int height) {
        Glide.with(activity)                             //配置上下文
                .load(Uri.fromFile(new File(path)))      //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存全尺寸
                .override(width, height)
                .into(imageView);
    }

    @Override
    public void displayImagePreview(Activity activity, Uri uri, ImageView imageView, int width, int height) {
        Glide.with(activity)                             //配置上下文
                .load(uri)      //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存全尺寸
                .override(width, height)
                .into(imageView);
    }

    @Override
    public void clearMemoryCache(Activity activity) {
        Glide.get(activity).clearMemory();
    }
}
