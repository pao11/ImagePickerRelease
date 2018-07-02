package com.pao11.imagepicker.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.pao11.imagepicker.ImagePicker;
import com.pao11.imagepicker.bean.ImageItem;
import com.pao11.imagepicker.util.FileUtil;
import com.pao11.imagepicker.util.Utils;

import java.io.File;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * ================================================
 * 版    本：1.0
 * 创建日期：2018/6/30
 * 描    述：
 * ================================================
 */
public class ImagePageAdapter extends PagerAdapter {

    private int screenWidth;
    private int screenHeight;
    private ImagePicker imagePicker;
    private ArrayList<ImageItem> images = new ArrayList<>();
    private Activity mActivity;
    public PhotoViewClickListener listener;

    public ImagePageAdapter(Activity activity, ArrayList<ImageItem> images) {
        this.mActivity = activity;
        this.images = images;

        DisplayMetrics dm = Utils.getScreenPix(activity);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        imagePicker = ImagePicker.getInstance();
    }

    public void setData(ArrayList<ImageItem> images) {
        this.images = images;
    }

    public void setPhotoViewClickListener(PhotoViewClickListener listener) {
        this.listener = listener;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        final PhotoView photoView = new PhotoView(mActivity);
        final ImageItem imageItem = images.get(position);
        if (imageItem.mimeType.startsWith("video")) {
            String fileName = FileUtil.Md5FileNameGenerate(imageItem.path, "jpg");

            File cacheRoot = FileUtil.getIndividualCacheDirectory(mActivity);
            final File file = new File(cacheRoot, fileName);
            if (file.exists()) {
                imagePicker.getImageLoader().displayImage(mActivity, file.getAbsolutePath(), photoView, screenWidth, screenHeight); //显示本地图片
            } else {

                photoView.setImageBitmap(null);
                //用于滚动时，图片显示不正确的问题
                photoView.setTag(position);

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        final Bitmap[] bitmap = {ThumbnailUtils.createVideoThumbnail(imageItem.path, MediaStore.Video.Thumbnails.MINI_KIND)};
                        //保存到本地目录
                        FileUtil.saveImageToSD(file.getAbsolutePath(), bitmap[0], 100);
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if ((int) photoView.getTag() == position) {
                                    photoView.setImageBitmap(bitmap[0]);
                                }
                                bitmap[0] = null;
                            }
                        });
                    }
                }).start();

            }

        } else {
            imagePicker.getImageLoader().displayImagePreview(mActivity, imageItem.path, photoView, screenWidth, screenHeight);
        }
        photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                if (listener != null) listener.OnPhotoTapListener(view, x, y);
            }
        });
        container.addView(photoView);
        return photoView;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public interface PhotoViewClickListener {
        void OnPhotoTapListener(View view, float v, float v1);
    }
}