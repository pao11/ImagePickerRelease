package com.pao11.imagepicker.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pao11.imagepicker.ImageDataSource;
import com.pao11.imagepicker.util.BitmapUtil;
import com.pao11.imagepicker.ImagePicker;
import com.pao11.imagepicker.bean.ImageItem;
import com.pao11.imagepicker.view.CropImageView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * ================================================
 * 版    本：1.0
 * 创建日期：2018/6/30
 * 描    述：
 * ================================================
 */
public class ImageCropActivity extends ImageBaseActivity implements View.OnClickListener, CropImageView.OnBitmapSaveCompleteListener {

    private CropImageView mCropImageView;
    private Bitmap mBitmap;
    private boolean mIsSaveRectangle;
    private int mOutputX;
    private int mOutputY;
    private ArrayList<ImageItem> mImageItems;
    private ImagePicker imagePicker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.pao11.imagepicker.R.layout.activity_image_crop);

        imagePicker = ImagePicker.getInstance();

        //初始化View
        findViewById(com.pao11.imagepicker.R.id.btn_back).setOnClickListener(this);
        Button btn_ok = (Button) findViewById(com.pao11.imagepicker.R.id.btn_ok);
        btn_ok.setText(getString(com.pao11.imagepicker.R.string.ip_complete));
        btn_ok.setOnClickListener(this);
        TextView tv_des = (TextView) findViewById(com.pao11.imagepicker.R.id.tv_des);
        tv_des.setText(getString(com.pao11.imagepicker.R.string.ip_photo_crop));
        mCropImageView = (CropImageView) findViewById(com.pao11.imagepicker.R.id.cv_crop_image);
        mCropImageView.setOnBitmapSaveCompleteListener(this);

        //获取需要的参数
        mOutputX = imagePicker.getOutPutX();
        mOutputY = imagePicker.getOutPutY();
        mIsSaveRectangle = imagePicker.isSaveRectangle();

        mCropImageView.setFocusStyle(imagePicker.getStyle());
        mCropImageView.setFocusWidth(imagePicker.getFocusWidth());
        mCropImageView.setFocusHeight(imagePicker.getFocusHeight());

        mImageItems = imagePicker.getSelectedImages();

        //缩放图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(imagePath, options);
        try {
            BitmapFactory.decodeFileDescriptor(getContentResolver().openFileDescriptor(imagePicker.getImgOrVideoUri(), "r").getFileDescriptor(), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        options.inSampleSize = calculateInSampleSize(options, displayMetrics.widthPixels, displayMetrics.heightPixels);
        options.inJustDecodeBounds = false;
//        mBitmap = BitmapFactory.decodeFile(imagePath, options);
        FileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = getContentResolver().openFileDescriptor(imagePicker.getImgOrVideoUri(), "rw").getFileDescriptor();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
//        mCropImageView.setImageBitmap(mBitmap);
        //设置默认旋转角度
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mCropImageView.setImageBitmap(mCropImageView.rotate(mBitmap, BitmapUtil.getBitmapDegree(fileDescriptor)));
        } else {
            String imagePath = mImageItems.get(0).path;
            mCropImageView.setImageBitmap(mCropImageView.rotate(mBitmap, BitmapUtil.getBitmapDegree(imagePath)));
        }

//        mCropImageView.setImageURI(Uri.fromFile(new File(imagePath)));
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = width / reqWidth;
            } else {
                inSampleSize = height / reqHeight;
            }
        }
        return inSampleSize;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == com.pao11.imagepicker.R.id.btn_back) {
            setResult(RESULT_CANCELED);
            finish();
        } else if (id == com.pao11.imagepicker.R.id.btn_ok) {
            mCropImageView.saveBitmapToFile(imagePicker.getCropCacheFolder(ImageCropActivity.this), mOutputX, mOutputY, mIsSaveRectangle);
        }
    }

    @Override
    public void onBitmapSaveSuccess(Uri uri) {
//        Toast.makeText(ImageCropActivity.this, "裁剪成功:" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

        //裁剪后替换掉返回数据的内容，但是不要改变全局中的选中数据
        ImageItem imageItem = ImageDataSource.getImageItemFromUri(this, uri);
        if (mImageItems.size() > 0) {
            mImageItems.remove(mImageItems.size() - 1);
        }
        mImageItems.add(imageItem);

        Intent intent = new Intent();
        intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, mImageItems);
        setResult(ImagePicker.RESULT_CODE_ITEMS, intent);   //单选不需要裁剪，返回数据
        finish();

    }

    @Override
    public void onBitmapSaveError(Uri uri) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCropImageView.setOnBitmapSaveCompleteListener(null);
        if (null != mBitmap && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }
}
