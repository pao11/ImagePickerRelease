package com.pao11.imagepicker.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.FileProvider;
import androidx.viewpager.widget.PagerAdapter;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.pao11.imagepicker.ImagePicker;
import com.pao11.imagepicker.R;
import com.pao11.imagepicker.bean.ImageItem;
import com.pao11.imagepicker.util.FileUtil;
import com.pao11.imagepicker.util.ProviderUtil;
import com.pao11.imagepicker.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


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
//        final PhotoView photoView = new PhotoView(mActivity);
        View temView = View.inflate(container.getContext(), R.layout.adapter_image_page, null);
        final PhotoView photoView = temView.findViewById(R.id.pv_photoview);
        final ImageView iv_player = temView.findViewById(R.id.iv_player);
        final ImageItem imageItem = images.get(position);
        if (imageItem.mimeType != null && imageItem.mimeType.startsWith("video")) {
            iv_player.setVisibility(View.VISIBLE);
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
//                        final Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(imageItem.path, MediaStore.Video.Thumbnails.MINI_KIND);
                        Bitmap bitmap = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            try {
                                bitmap = mActivity.getContentResolver().loadThumbnail(imageItem.uri, new Size(512, 384), null);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            bitmap = ThumbnailUtils.createVideoThumbnail(imageItem.path, MediaStore.Video.Thumbnails.MINI_KIND);
                        }
                        if (bitmap != null) {
                            //保存到本地目录
                            FileUtil.saveImageToSD(file.getAbsolutePath(), bitmap, 100);
                            bitmap.recycle();
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if ((int) photoView.getTag() == position) {
                                        imagePicker.getImageLoader().displayImage(mActivity, file.getAbsolutePath(), photoView, screenWidth, screenHeight); //显示本地图片
                                    }
                                }
                            });
                        }
                    }
                }).start();

            }
            iv_player.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
//                    File file = new File(imageItem.path);
//                    Uri uri;
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        Uri contentUri = FileProvider.getUriForFile(mActivity, ProviderUtil.getFileProviderName(mActivity), file);
//                        intent.setDataAndType(imageItem.uri, "video/*");
//                    } else {
//                        uri = Uri.fromFile(file);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.setDataAndType(uri, "video/*");
//                    }
                    intent.setDataAndType(imageItem.uri, "video/*");
                    mActivity.startActivity(intent);
                }
            });

        } else {
            iv_player.setVisibility(View.INVISIBLE);
//            System.out.println(">>>>>>>>>>>>>" + imageItem.path);
            imagePicker.getImageLoader().displayImagePreview(mActivity, imageItem.uri, photoView, screenWidth, screenHeight);
        }

        photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float x, float y) {
                if (listener != null) listener.OnPhotoTapListener(view, x, y);
            }
        });
        container.addView(temView);
        return temView;
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
