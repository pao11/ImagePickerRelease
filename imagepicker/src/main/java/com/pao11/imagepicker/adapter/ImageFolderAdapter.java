package com.pao11.imagepicker.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pao11.imagepicker.ImagePicker;
import com.pao11.imagepicker.R;
import com.pao11.imagepicker.bean.ImageFolder;
import com.pao11.imagepicker.util.FileUtil;
import com.pao11.imagepicker.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * ================================================
 * 版    本：1.0
 * 创建日期：2018/6/30
 * ================================================
 */
public class ImageFolderAdapter extends BaseAdapter {

    private ImagePicker imagePicker;
    private Activity mActivity;
    private LayoutInflater mInflater;
    private int mImageSize;
    private List<ImageFolder> imageFolders;
    private int lastSelected = 0;

    public ImageFolderAdapter(Activity activity, List<ImageFolder> folders) {
        mActivity = activity;
        if (folders != null && folders.size() > 0) imageFolders = folders;
        else imageFolders = new ArrayList<>();

        imagePicker = ImagePicker.getInstance();
        mImageSize = Utils.getImageItemWidth(mActivity);
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void refreshData(List<ImageFolder> folders) {
        if (folders != null && folders.size() > 0) imageFolders = folders;
        else imageFolders.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return imageFolders.size();
    }

    @Override
    public ImageFolder getItem(int position) {
        return imageFolders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.adapter_folder_list_item, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final ImageFolder folder = getItem(position);
        holder.folderName.setText(folder.name);
        holder.imageCount.setText(mActivity.getString(R.string.ip_folder_image_count, folder.images.size()));
        if (TextUtils.equals(folder.content_type, MediaStore.Images.Media.CONTENT_TYPE)){
            holder.videoPlay.setVisibility(View.GONE);
            imagePicker.getImageLoader().displayImage(mActivity, folder.cover.path, holder.cover, mImageSize, mImageSize);
        } else {
            holder.videoPlay.setVisibility(View.VISIBLE);

            String fileName = FileUtil.Md5FileNameGenerate(folder.cover.path, "jpg");
            fileName = fileName.substring(0, fileName.lastIndexOf(".jpg")) + "_" + mImageSize + "_" + mImageSize + ".jpg";

            File cacheRoot = FileUtil.getIndividualCacheDirectory(mActivity);
            final File file = new File(cacheRoot, fileName);
            if (file.exists()) {
                imagePicker.getImageLoader().displayImage(mActivity, file.getAbsolutePath(), holder.cover, mImageSize, mImageSize); //显示本地图片
            } else {

                //用于滚动时，图片显示不正确的问题
                holder.cover.setTag(position);

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(folder.cover.path, MediaStore.Video.Thumbnails.MINI_KIND);
                        if (bitmap != null) {
                            final Bitmap bitmap1 = ThumbnailUtils.extractThumbnail(bitmap, mImageSize, mImageSize, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

                            bitmap.recycle();

                            if (null != bitmap1 && !bitmap1.isRecycled()) {
                                //保存到本地目录
                                FileUtil.saveImageToSD(file.getAbsolutePath(), bitmap1, 100);
                                bitmap1.recycle();
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if ((int) holder.cover.getTag() == position) {
                                            imagePicker.getImageLoader().displayImage(mActivity, file.getAbsolutePath(), holder.cover, mImageSize, mImageSize); //显示本地图片

                                        }
                                    }
                                });
                            }
                        }
                    }
                }).start();
            }

        }


        if (lastSelected == position) {
            holder.folderCheck.setVisibility(View.VISIBLE);
        } else {
            holder.folderCheck.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    public void setSelectIndex(int i) {
        if (lastSelected == i) {
            return;
        }
        lastSelected = i;
        notifyDataSetChanged();
    }

    public int getSelectIndex() {
        return lastSelected;
    }

    private class ViewHolder {
        ImageView cover;
        TextView folderName;
        TextView imageCount;
        ImageView folderCheck;
        ImageView videoPlay;


        public ViewHolder(View view) {
            cover = (ImageView) view.findViewById(R.id.iv_cover);
            folderName = (TextView) view.findViewById(R.id.tv_folder_name);
            imageCount = (TextView) view.findViewById(R.id.tv_image_count);
            folderCheck = (ImageView) view.findViewById(R.id.iv_folder_check);
            videoPlay = (ImageView) view.findViewById(R.id.iv_video_play);
            view.setTag(this);
        }
    }
}
