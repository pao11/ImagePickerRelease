package com.pao11.imagepicker.loader;

import android.app.Activity;
import android.net.Uri;
import android.widget.ImageView;

import java.io.Serializable;

/**
 * ================================================
 * 版    本：1.0
 * 创建日期：2018/6/25
 * 描    述：ImageLoader抽象类，外部需要实现这个类去加载图片， 尽力减少对第三方库的依赖
 * ================================================
 */
public interface ImageLoader extends Serializable {


    void displayImage(Activity activity, String path, ImageView imageView, int width, int height);
    //为了适配Android Q及以上系统，非私有目录的path会报错，改用uri的方式
    void displayImage(Activity activity, Uri uri, ImageView imageView, int width, int height);

    void displayImagePreview(Activity activity, String path, ImageView imageView, int width, int height);

    //为了适配Android Q及以上系统，非私有目录的path会报错，改用uri的方式
    void displayImagePreview(Activity activity, Uri uri, ImageView imageView, int width, int height);

    void clearMemoryCache(Activity activity);
}
