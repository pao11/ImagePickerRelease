package com.pao11.imagepicker;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.pao11.imagepicker.bean.ImageFolder;
import com.pao11.imagepicker.bean.ImageItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 版    本：1.0
 * 创建日期：2018/6/19
 * 描    述：加载手机图片实现类
 * ================================================
 */
public class ImageDataSource implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER_ALL = 0;         //加载所有图片
    public static final int LOADER_CATEGORY = 1;    //分类加载图片
    public static final int LOADER_ALL_VIDEO = 2;   //加载所有视频
    public static final int LOADER_FIRST_IMG = 3;   //加载第一张图片
    private final String[] IMAGE_PROJECTION = {     //查询图片需要的数据列
            MediaStore.Images.Media.DISPLAY_NAME,   //图片的显示名称  aaa.jpg
            MediaStore.Images.Media.DATA,           //图片的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
            MediaStore.Images.Media.SIZE,           //图片的大小，long型  132492
            MediaStore.Images.Media.WIDTH,          //图片的宽度，int型  1920
            MediaStore.Images.Media.HEIGHT,         //图片的高度，int型  1080
            MediaStore.Images.Media.MIME_TYPE,      //图片的类型     image/jpeg
            MediaStore.Images.Media.DATE_ADDED};    //图片被添加的时间，long型  1450518608
    private final String[] VIDEO_PROJECTION = {     //查询视频需要的数据列
            MediaStore.Video.Media.DISPLAY_NAME,   //视频的显示名称  aaa.mp4
            MediaStore.Video.Media.DATA,           //视频的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.mp4
            MediaStore.Video.Media.SIZE,           //视频的大小，long型  132492
            MediaStore.Video.Media.WIDTH,          //视频的宽度，int型  1920
            MediaStore.Video.Media.HEIGHT,         //视频的高度，int型  1080
            MediaStore.Video.Media.MIME_TYPE,      //视频的类型     image/jpeg
            MediaStore.Video.Media.DATE_ADDED,     //视频被添加的时间，long型  1450518608
            MediaStore.Video.Media.DURATION};      //视频时长，long型  ms

    private FragmentActivity activity;
    private OnImagesLoadedListener loadedListener;                     //图片加载完成的回调接口
    private ArrayList<ImageFolder> imageFolders = new ArrayList<>();   //所有的图片文件夹

    /**
     * @param activity       用于初始化LoaderManager，需要兼容到2.3
     * @param path           指定扫描的文件夹目录，可以为 null，表示扫描所有图片
     * @param loadedListener 图片加载完成的监听
     */
    public ImageDataSource(FragmentActivity activity, String path, OnImagesLoadedListener loadedListener) {
        this.activity = activity;
        this.loadedListener = loadedListener;

        LoaderManager loaderManager = activity.getSupportLoaderManager();
        if (path == null) {
            loaderManager.initLoader(LOADER_ALL, null, this);//加载所有的图片
        } else {
            //加载指定目录的图片
            Bundle bundle = new Bundle();
            bundle.putString("path", path);
            loaderManager.initLoader(LOADER_CATEGORY, bundle, this);
        }
    }

    /**
     *
     * @param activity
     * @param path
     * @param loadedListener
     * @param load_type
     */
//    public ImageDataSource(FragmentActivity activity, String path, OnImagesLoadedListener loadedListener,
//                           @IntRange(from = 0, to = 3) int load_type) {
//        this.activity = activity;
//        this.loadedListener = loadedListener;
//
//        LoaderManager loaderManager = activity.getSupportLoaderManager();
//        if (path == null) {
//            loaderManager.initLoader(load_type, null, this);//加载所有的图片
//        } else {
//            //加载指定目录的图片
//            Bundle bundle = new Bundle();
//            bundle.putString("path", path);
//            loaderManager.initLoader(load_type, bundle, this);
//        }
//    }

    /**
     * 加载所有的视频文件
     */
    public void loaderAllVideos() {
        LoaderManager loaderManager = activity.getSupportLoaderManager();
        loaderManager.initLoader(LOADER_ALL_VIDEO, null, this);//加载所有的视频
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = null;
        switch (id) {
            case LOADER_ALL:
                //扫描所有图片
                cursorLoader = new CursorLoader(activity, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, null, null, IMAGE_PROJECTION[6] + " DESC");
                break;
            case LOADER_CATEGORY:
                //扫描某个图片文件夹
                cursorLoader = new CursorLoader(activity, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, IMAGE_PROJECTION[1] + " like '%" + args.getString("path") + "%'", null, IMAGE_PROJECTION[6] + " DESC");
                break;
            case LOADER_ALL_VIDEO:
                //扫描所有视频
                cursorLoader = new CursorLoader(activity, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VIDEO_PROJECTION, null, null, VIDEO_PROJECTION[6] + " DESC");
                break;
            case LOADER_FIRST_IMG:
                //读取第一张图片
                cursorLoader = new CursorLoader(activity, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, null, null, IMAGE_PROJECTION[6] + " DESC LIMIT 1");
                break;
        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_ALL_VIDEO) {
            if (data != null) {
                ArrayList<ImageItem> allVideos = new ArrayList<>();   //所有图片的集合,不分文件夹
                ArrayList<ImageItem> images = new ArrayList<>();
                if (imageFolders.size() > 0) {
                    images.addAll(imageFolders.get(0).images);
                }
                int start_i = 0;
                int t_size = images.size();
                while (data.moveToNext()) {
                    //查询数据
                    String videoName = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[0]));
                    String videoPath = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[1]));

                    File file = new File(videoPath);
                    if (!file.exists() || file.length() <= 0) {
                        continue;
                    }

                    long videoSize = data.getLong(data.getColumnIndexOrThrow(VIDEO_PROJECTION[2]));
                    int videoWidth = data.getInt(data.getColumnIndexOrThrow(VIDEO_PROJECTION[3]));
                    int videoHeight = data.getInt(data.getColumnIndexOrThrow(VIDEO_PROJECTION[4]));
                    String videoMimeType = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[5]));
                    long videoAddTime = data.getLong(data.getColumnIndexOrThrow(VIDEO_PROJECTION[6]));
                    long videoDuration = data.getLong(data.getColumnIndexOrThrow(VIDEO_PROJECTION[7]));
                    //封装实体
                    ImageItem imageItem = new ImageItem();
                    imageItem.name = videoName;
                    imageItem.path = videoPath;
                    imageItem.size = videoSize;
                    imageItem.width = videoWidth;
                    imageItem.height = videoHeight;
                    imageItem.mimeType = videoMimeType;
                    imageItem.addTime = videoAddTime;
                    imageItem.duration = videoDuration;
                    allVideos.add(imageItem);
                    //将视频放到图片和视频中
                    if (start_i == t_size) {
                        imageFolders.get(0).images.add(imageItem);
                    } else {
                        for (int i = start_i; i < t_size; i++) {
                            ImageItem imgItem = images.get(i);
                            if (imgItem.addTime < videoAddTime) {
                                imageFolders.get(0).images.add(imageFolders.get(0).images.indexOf(imgItem), imageItem);
                                start_i = i;
                                break;
                            } else if (i == t_size - 1) {
                                start_i = t_size;
                                imageFolders.get(0).images.add(imageItem);
                            }
                        }
                    }
                }
                //防止没有视频报异常
                if (data.getCount() > 0 && allVideos.size()>0) {
                    //构造所有视频的集合
                    ImageFolder allImagesFolder = new ImageFolder();
                    allImagesFolder.content_type = MediaStore.Video.Media.CONTENT_TYPE;
                    allImagesFolder.name = activity.getResources().getString(R.string.ip_all_videos);
                    allImagesFolder.path = "/";
                    allImagesFolder.cover = allVideos.get(0);
                    allImagesFolder.images = allVideos;
                    imageFolders.add(1, allImagesFolder);  //确保第二条是所有视频
                }
            }

            //回调接口，通知图片数据准备完成
            ImagePicker.getInstance().setImageFolders(imageFolders);
            loadedListener.onVideoLoaded(imageFolders);
        } else {
            imageFolders.clear();
            if (data != null) {
                ArrayList<ImageItem> allImages = new ArrayList<>();   //所有图片的集合,不分文件夹
                while (data.moveToNext()) {
                    //查询数据
                    String imageName = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                    String imagePath = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));

                    File file = new File(imagePath);
                    if (!file.exists() || file.length() <= 0) {
                        continue;
                    }

                    long imageSize = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                    int imageWidth = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[3]));
                    int imageHeight = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[4]));
                    String imageMimeType = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[5]));
                    long imageAddTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[6]));
                    //封装实体
                    ImageItem imageItem = new ImageItem();
                    imageItem.name = imageName;
                    imageItem.path = imagePath;
                    imageItem.size = imageSize;
                    imageItem.width = imageWidth;
                    imageItem.height = imageHeight;
                    imageItem.mimeType = imageMimeType;
                    imageItem.addTime = imageAddTime;
                    allImages.add(imageItem);
                    //根据父路径分类存放图片
                    File imageFile = new File(imagePath);
                    File imageParentFile = imageFile.getParentFile();
                    ImageFolder imageFolder = new ImageFolder();
                    imageFolder.content_type = MediaStore.Images.Media.CONTENT_TYPE;
                    imageFolder.name = imageParentFile.getName();
                    imageFolder.path = imageParentFile.getAbsolutePath();

                    if (!imageFolders.contains(imageFolder)) {
                        ArrayList<ImageItem> images = new ArrayList<>();
                        images.add(imageItem);
                        imageFolder.cover = imageItem;
                        imageFolder.images = images;
                        imageFolders.add(imageFolder);
                    } else {
                        imageFolders.get(imageFolders.indexOf(imageFolder)).images.add(imageItem);
                    }
                }
                //防止没有图片报异常
                if (data.getCount() > 0 && allImages.size()>0) {
                    //构造所有图片的集合
                    ImageFolder allImagesFolder = new ImageFolder();
                    allImagesFolder.content_type = MediaStore.Images.Media.CONTENT_TYPE;
                    allImagesFolder.name = ImagePicker.getInstance().isLoadVideos() ?
                            activity.getResources().getString(R.string.ip_all_images_and_videos)
                            : activity.getResources().getString(R.string.ip_all_images);
                    allImagesFolder.path = "/";
                    allImagesFolder.cover = allImages.get(0);
                    allImagesFolder.images = allImages;
                    imageFolders.add(0, allImagesFolder);  //确保第一条是所有图片
                }
            }

            //回调接口，通知图片数据准备完成
            ImagePicker.getInstance().setImageFolders(imageFolders);
            loadedListener.onImagesLoaded(imageFolders);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        System.out.println("--------");
    }

    /** 所有图片加载完成的回调接口 */
    public interface OnImagesLoadedListener {
        void onImagesLoaded(List<ImageFolder> imageFolders);
        void onVideoLoaded(List<ImageFolder> imageFolders);
    }
}
