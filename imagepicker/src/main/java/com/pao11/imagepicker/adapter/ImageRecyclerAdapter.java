package com.pao11.imagepicker.adapter;

import android.Manifest;
import android.app.Activity;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pao11.imagepicker.ImagePicker;
import com.pao11.imagepicker.bean.ImageItem;
import com.pao11.imagepicker.ui.ImageBaseActivity;
import com.pao11.imagepicker.ui.ImageGridActivity;
import com.pao11.imagepicker.util.FileUtil;
import com.pao11.imagepicker.util.Utils;
import com.pao11.imagepicker.view.SuperCheckBox;

import java.io.File;
import java.util.ArrayList;

/**
 * 加载相册图片的RecyclerView适配器
 *
 * 用于替换原项目的GridView，使用局部刷新解决选中照片出现闪动问题
 *
 * 替换为RecyclerView后只是不再会导致全局刷新，
 *
 * 但还是会出现明显的重新加载图片，可能是picasso图片加载框架的问题
 *
 * Date: 2017-06-30
 */

public class ImageRecyclerAdapter extends RecyclerView.Adapter<ViewHolder> {


    private static final int ITEM_TYPE_CAMERA = 0;  //第一个条目是相机
    private static final int ITEM_TYPE_NORMAL = 1;  //第一个条目不是相机
    private ImagePicker imagePicker;
    private Activity mActivity;
    private ArrayList<ImageItem> images;       //当前需要显示的所有的图片数据
    private ArrayList<ImageItem> mSelectedImages; //全局保存的已经选中的图片数据
    private boolean isShowCamera;         //是否显示拍照按钮
    private int mImageSize;               //每个条目的大小
    private LayoutInflater mInflater;
    private OnImageItemClickListener listener;   //图片被点击的监听

    public void setOnImageItemClickListener(OnImageItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnImageItemClickListener {
        void onImageItemClick(View view, ImageItem imageItem, int position);
    }

    public void refreshData(ArrayList<ImageItem> images) {
        if (images == null || images.size() == 0) this.images = new ArrayList<>();
        else this.images = images;
        notifyDataSetChanged();
    }

    /**
     * 构造方法
     */
    public ImageRecyclerAdapter(Activity activity, ArrayList<ImageItem> images) {
        this.mActivity = activity;
        if (images == null || images.size() == 0) this.images = new ArrayList<>();
        else this.images = images;

        mImageSize = Utils.getImageItemWidth(mActivity);
        imagePicker = ImagePicker.getInstance();
        isShowCamera = imagePicker.isShowCamera();
        mSelectedImages = imagePicker.getSelectedImages();
        mInflater = LayoutInflater.from(activity);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_CAMERA){
            return new CameraViewHolder(mInflater.inflate(com.pao11.imagepicker.R.layout.adapter_camera_item,parent,false));
        }
        return new ImageViewHolder(mInflater.inflate(com.pao11.imagepicker.R.layout.adapter_image_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder instanceof CameraViewHolder){
            ((CameraViewHolder)holder).bindCamera();
        }else if (holder instanceof ImageViewHolder){
            ((ImageViewHolder)holder).bind(position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isShowCamera) return position == 0 ? ITEM_TYPE_CAMERA : ITEM_TYPE_NORMAL;
        return ITEM_TYPE_NORMAL;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return isShowCamera ? images.size() + 1 : images.size();
    }

    public ImageItem getItem(int position) {
        if (isShowCamera) {
            if (position == 0) return null;
            return images.get(position - 1);
        } else {
            return images.get(position);
        }
    }

    private class ImageViewHolder extends ViewHolder{

        View rootView;
        ImageView ivThumb;
        View mask;
        View checkView;
        SuperCheckBox cbCheck;
        LinearLayout llBottom;
        TextView tvDuration;


        ImageViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            ivThumb = (ImageView) itemView.findViewById(com.pao11.imagepicker.R.id.iv_thumb);
            mask = itemView.findViewById(com.pao11.imagepicker.R.id.mask);
            checkView=itemView.findViewById(com.pao11.imagepicker.R.id.checkView);
            cbCheck = (SuperCheckBox) itemView.findViewById(com.pao11.imagepicker.R.id.cb_check);
            llBottom = (LinearLayout) itemView.findViewById(com.pao11.imagepicker.R.id.ll_bottom);
            tvDuration = (TextView) itemView.findViewById(com.pao11.imagepicker.R.id.tv_duration);
            itemView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mImageSize)); //让图片是个正方形
        }

        void bind(final int position){
            final ImageItem imageItem = getItem(position);
            if (imageItem.mimeType.startsWith("video")) {
                llBottom.setVisibility(View.VISIBLE);
                long dur = imageItem.duration;
                String durStr;
                long sec = dur / 1000;
                long min = sec / 60;
                long t_sec = sec % 60;
                String secStr = t_sec > 9 ? t_sec + "" :"0" + t_sec;
                if (min > 0) {
                    long hour = min / 60;
                    long t_min = min % 60;
                    String minStr = t_min > 9 ? t_min + "" :"0" + t_min;
                    if (hour > 0) {
                        long t_hour = hour % 60;
                        durStr = (t_hour > 9 ? t_hour + "" : "0" + t_hour) + ":" + minStr + ":" + secStr;
                    } else {
                        durStr = minStr + ":" + secStr;
                    }
                } else {
                    durStr = "00:" + secStr;
                }
                tvDuration.setText(durStr);

                String fileName = FileUtil.Md5FileNameGenerate(imageItem.path, "jpg");
                fileName = fileName.substring(0, fileName.lastIndexOf(".jpg")) + "_" + mImageSize + "_" + mImageSize + ".jpg";

                File cacheRoot = FileUtil.getIndividualCacheDirectory(mActivity);
                final File file = new File(cacheRoot, fileName);
                if (file.exists()) {
                    imagePicker.getImageLoader().displayImage(mActivity, file.getAbsolutePath(), ivThumb, mImageSize, mImageSize); //显示本地图片
                } else {

                    ivThumb.setImageBitmap(null);
                    //用于滚动时，图片显示不正确的问题
                    ivThumb.setTag(position);

                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(imageItem.path, MediaStore.Video.Thumbnails.MINI_KIND);
                            if (bitmap != null) {
                                final Bitmap bitmap1 = ThumbnailUtils.extractThumbnail(bitmap, mImageSize, mImageSize, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

                                bitmap.recycle();
                                //保存到本地目录
                                FileUtil.saveImageToSD(file.getAbsolutePath(), bitmap1, 100);
                                bitmap1.recycle();

                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if ((int) ivThumb.getTag() == position) {
                                            imagePicker.getImageLoader().displayImage(mActivity, file.getAbsolutePath(), ivThumb, mImageSize, mImageSize);
                                        }
                                    }
                                });
                            }

                        }
                    }).start();
                }
            } else {
                llBottom.setVisibility(View.INVISIBLE);

                imagePicker.getImageLoader().displayImage(mActivity, imageItem.path, ivThumb, mImageSize, mImageSize); //显示图片
            }

            ivThumb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onImageItemClick(rootView, imageItem, position);
                }
            });
            checkView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cbCheck.setChecked(!cbCheck.isChecked());
                    int selectLimit = imagePicker.getSelectLimit();
                    if (cbCheck.isChecked() && mSelectedImages.size() >= selectLimit) {
                        Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(com.pao11.imagepicker.R.string.ip_select_limit, selectLimit), Toast.LENGTH_SHORT).show();
                        cbCheck.setChecked(false);
                        mask.setVisibility(View.GONE);
                    } else {
                        imagePicker.addSelectedImageItem(position, imageItem, cbCheck.isChecked());
                        mask.setVisibility(View.VISIBLE);
                    }
                }
            });
            //根据是否多选，显示或隐藏checkbox
            if (imagePicker.isMultiMode()) {
                cbCheck.setVisibility(View.VISIBLE);
                boolean checked = mSelectedImages.contains(imageItem);
                if (checked) {
                    mask.setVisibility(View.VISIBLE);
                    cbCheck.setChecked(true);
                } else {
                    mask.setVisibility(View.GONE);
                    cbCheck.setChecked(false);
                }
            } else {
                cbCheck.setVisibility(View.GONE);
            }

        }

    }

    private class CameraViewHolder extends ViewHolder{

        View mItemView;

        CameraViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
        }

        void bindCamera(){
            mItemView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mImageSize)); //让图片是个正方形
            mItemView.setTag(null);
            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!((ImageBaseActivity) mActivity).checkPermission(Manifest.permission.CAMERA)) {
                        ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.CAMERA}, ImageGridActivity.REQUEST_PERMISSION_CAMERA);
                    } else {
                        imagePicker.takePicture(mActivity, ImagePicker.REQUEST_CODE_TAKE);
                    }
                }
            });
        }
    }
}
