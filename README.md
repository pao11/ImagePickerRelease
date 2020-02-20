[![](https://jitpack.io/v/pao11/ImagePickerRelease.svg)](https://jitpack.io/#pao11/ImagePickerRelease)
[![Travis](https://img.shields.io/badge/Gradle-2.3.1-brightgreen.svg)]()

# ImagePickerRelease
Android自定义相册，完全仿微信UI，实现了拍照、图片选择（单选/多选）、视频选择、 裁剪 、旋转、等功能。

该项目参考了：

* [https://github.com/pengjianbo/GalleryFinal](https://github.com/pengjianbo/GalleryFinal) 
* [https://github.com/easonline/AndroidImagePicker](https://github.com/easonline/AndroidImagePicker)

## 1.用法

使用前，对于Android Studio的用户，可以选择添加:
```
	compile 'com.github.pao11:ImagePickerRelease:1.7.0'  //指定版本
	androix适配版本使用以下地址：
	compile 'com.github.pao11:ImagePickerRelease:2.1.0'
```

## 2.功能和参数含义


|配置参数|参数含义|
|:--:|--|
|multiMode|图片选着模式，单选/多选|
|selectLimit|多选限制数量，默认为9|
|setLoadVideos|是否可选视频文件
|showCamera|选择照片时是否显示拍照按钮|
|crop|是否允许裁剪（单选有效）|
|style|有裁剪时，裁剪框是矩形还是圆形|
|focusWidth|矩形裁剪框宽度（圆形自动取宽高最小值）|
|focusHeight|矩形裁剪框高度（圆形自动取宽高最小值）|
|outPutX|裁剪后需要保存的图片宽度|
|outPutY|裁剪后需要保存的图片高度|
|isSaveRectangle|裁剪后的图片是按矩形区域保存还是裁剪框的形状，例如圆形裁剪的时候，该参数给true，那么保存的图片是矩形区域，如果该参数给fale，保存的图片是圆形区域|
|imageLoader|需要使用的图片加载器，自需要实现ImageLoader接口即可|

## 3.代码参考

更多使用，请下载demo参看源代码


一般在Application初始化配置一次就可以
```
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_image_picker);
    
    ImagePicker imagePicker = ImagePicker.getInstance();
    imagePicker.setImageLoader(new PicassoImageLoader());   //设置图片加载器，此处需要自定义imageloader，参考demo
    imagePicker.setLoadVideos(true);    //设置可以显示并选择视频文件
    imagePicker.setShowCamera(true);  //显示拍照按钮
    imagePicker.setCrop(true);        //允许裁剪（单选才有效）
    imagePicker.setSaveRectangle(true); //是否按矩形区域保存
    imagePicker.setSelectLimit(9);    //选中数量限制
    imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
    imagePicker.setFocusWidth(800);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
    imagePicker.setFocusHeight(800);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
    imagePicker.setOutPutX(1000);//保存文件的宽度。单位像素
    imagePicker.setOutPutY(1000);//保存文件的高度。单位像素
}
```

配置完成后，在适当的方法中开启相册，例如点击按钮时
```
public void onClick(View v) {
        Intent intent = new Intent(this, ImageGridActivity.class);
        startActivityForResult(intent, IMAGE_PICKER);  
    }
}
```

如果你想直接调用相机
```
Intent intent = new Intent(this, ImageGridActivity.class);
intent.putExtra(ImageGridActivity.EXTRAS_TAKE_PICKERS,true); // 是否是直接打开相机
      startActivityForResult(intent, REQUEST_CODE_SELECT);
```

重写`onActivityResult`方法,回调结果
```
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
        if (data != null && requestCode == IMAGE_PICKER) {
            ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
            MyAdapter adapter = new MyAdapter(images);
            gridView.setAdapter(adapter);
        } else {
            Toast.makeText(this, "没有数据", Toast.LENGTH_SHORT).show();
        }
    }
}
```

## 更新日志

V 2.1.0
 * 增加拍摄视频功能

V 2.0.0
 * 适配androidx库，增加视频的浏览功能

V 1.7.0
 * 修复特殊情况下视频缩略图闪退情况

V 1.6.0
 * 修复只有视频文件时闪退情况

V 1.5.0
 * 修复增加先加载视频的构造方法，增加可一直加载一张图片的类型

V 1.3.0
 * 修复图片和视频一起显示时，视频文件显示不全的bug，并优化视频缩略图的加载过程，防止OOM

V 1.2.0
 * 增加图片和视频一栏，并全部显示，仿微信

V 1.1.0
 * 增加优化视频缩略图展示效果，做本地保存

V 1.0.0
 * 增加可以选择视频文件的选项，仿照微信

## Licenses
```
 Copyright 2018 pao11

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
```

