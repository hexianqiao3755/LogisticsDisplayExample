### 物流详情页
---
今天就来简单的实现一下, 只有两个`Activity`, 晒单、图片显示`Activity`



配置如下

![build.gradle](https://github.com/hexianqiao3755/OrderCommentPage/blob/master/gif/2112446-d0ca6a63443aac4c.png)

布局没有太大难度, 大家照着依葫芦画瓢就写出来了, 就不做介绍了
由于代码过多就不全部展示了, 只讲解重要的逻辑处理
**废话不多说, 先看效果图**

效果图可能加载有点慢 耐心等待

![example.gif](https://github.com/hexianqiao3755/OrderCommentPage/blob/master/gif/GIF_20170418_153324.gif)


#### 1.MainActivity
点击相机后执行`MultiImageSelector.create().count(MAX_PIC - imageUrls.size()).start(this, REQUEST_CODE_PICTURE);`
调出选择图片界面, 选择完成会进入`onActivityResult`方法
我们就在该方法中处理选中的晒单图集合
回调的`onActivityResult`后核心的处理在`handleCommentPicList`方法里
```
    public static final String KEY_IMAGE_LIST = "imageList";
    public static final String KEY_CURRENT_INDEX = "currentIndex";
    private final int REQUEST_CODE_PICTURE = 1;
    private final int RESULT_CODE_LARGE_IMAGE = 1;
    //晒单图片最多选择四张
    private final int MAX_PIC = 4;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICTURE) {
                // 获取返回的图片列表
                List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                imageUrls.addAll(path);
                handleCommentPicList(imageUrls, false);
            }
        } else if (resultCode == RESULT_CODE_LARGE_IMAGE) {
            //晒单大图页返回, 重新设置晒单图片
            handleCommentPicList(imageUrls = data.getStringArrayListExtra(KEY_IMAGE_LIST), true);
        }
    }
```

`handleCommentPicList`方法作用是
- 把返回的几张压缩后保存在一个零时文件夹, 并在用户退出时清空该文件夹
传到服务器时也是上传压缩后的图
 现在的手机拍出的图基本都是`5MB`左右, 不可能就把这么大的图片完全显示上去
**ps: 我最开始没这样压缩时, 把4张5M左右的图不做处理直接显示,  app直接卡着动不了, 内存秒升到300M+**

- 把压缩后的图显示到控件上`sdv_pic.setImageURI(Uri.parse("file://" + path));`

- 设置`onClickListener`跳转到图片详情`Activity`

```
    /**
     * 处理选择的评价图片
     *
     * @param paths      图片的路径集合
     * @param isFromBack 是否来自LargeImageActivity返回
     */
    private void handleCommentPicList(final List<String> paths, boolean isFromBack) {
        LinearLayout rootview = new LinearLayout(context);
        View commentView;
        SimpleDraweeView sdv_pic;
        for (int i = 0, len = paths.size(); i < len; i++) {
            commentView = getLayoutInflater().inflate(R.layout.order_comment_pic_item, null);
            sdv_pic = (SimpleDraweeView) commentView.findViewById(R.id.sdv_pic);
            if (isFromBack) {
                //来自LargeImageActivity
                sdv_pic.setImageURI(Uri.parse("file://" + paths.get(i)));
            } else {
                //来自图片选择器
                String path = FileUtils.getCachePath(context);//获取app缓存路径来存放临时图片
                BitmapUtils.compressImage(paths.get(i), path, 95);
                sdv_pic.setImageURI(Uri.parse("file://" + path));
                imageUrls.set(i, path);
            }

            final int finalI = i;
            sdv_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //点击HorizontalScrollView里的晒单图进入图片详情页
                    Intent intent = new Intent(context, CommentLargeImageActivity.class);
                    intent.putExtra(KEY_CURRENT_INDEX, finalI);
                    intent.putStringArrayListExtra(KEY_IMAGE_LIST, (ArrayList<String>) paths);
                    startActivityForResult(intent, REQUEST_CODE_PICTURE);
                }
            });
            AutoUtils.auto(commentView);
            rootview.addView(commentView);
        }
        hsv_comment_imgs.removeAllViews();
        hsv_comment_imgs.addView(rootview);
    }
```

用户退出时需要清除临时压缩图片文件
```
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //清除临时压缩图片文件
        CleanCacheManager.cleanExternalCache(this);
    }
```

```
public class CleanCacheManager {

    /**
     * 清除外部cache下的内容(/mnt/sdcard/android/data/com.xxx.xxx/cache)
     *
     * @param context
     */
    public static void cleanExternalCache(Context context) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            deleteFilesByDirectory(context.getExternalCacheDir());
        }
    }


    /**
     * 删除方法 这里只会删除某个文件夹下的文件，如果传入的directory是个文件，将不做处理
     *
     * @param directory
     */
    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }
}
```

#### 2.CommentLargeImageActivity
获取点击图片的索引, 作用显示于当前索引图片
获取传来的图片集合, 并设置到`ViewPager`的适配器

```
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            currentIndex = bundle.getInt(MainActivity.KEY_CURRENT_INDEX);
            imgUrls = bundle.getStringArrayList(MainActivity.KEY_IMAGE_LIST);
            vp_large_image.setAdapter(adapter = new LargeImageAdapter(this, imgUrls));//设置晒单图显示
            vp_large_image.setOffscreenPageLimit(imgUrls.size());//预加载的数量为图片集合的长度
            vp_large_image.setCurrentItem(currentIndex);
            tv_current_index.setText(++currentIndex + " / " + imgUrls.size());
            currentIndex--;
        }
```

点击图片详情页右上角的删除时. 会删除当前的图片
```
case R.id.ll_remove:
	//删除当前晒单图
	if (imgUrls.size() == 1) {
		//删除最后一张时直接回到晒单评论页
		imgUrls.clear();
		onBackPressed();//图片删除完后退出当前页面
	} else {
		//删除指定索引的图片
		removeImage(currentIndex);
	}
	break;
```

点击左上角的返回或者物理键盘的返回
**就把处理后的图片集合返回到`MainActivity`**
```
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(MainActivity.KEY_IMAGE_LIST, (ArrayList<String>) adapter.getData());
        //此处返回到MainActivity的onActivityResult回调方法的resultCode == RESULT_CODE_LARGE_IMAGE判断
        setResult(RESULT_CODE_LARGE_IMAGE, intent);
        super.onBackPressed();
    }
```

```
    /**
     * 删除指定索引的晒单图
     * @param index
     */
    private void removeImage(int index) {
        imgUrls.remove(index);
        setImageTitle(index);
        vp_large_image.removeAllViews();//删除viewpager所有的子View
        vp_large_image.setAdapter(adapter = new LargeImageAdapter(this, imgUrls));//重新设置适配器数据显示
        vp_large_image.setCurrentItem(index == imgUrls.size() - 1 ? ++index : --index);//显示指定位置
        adapter.notifyDataSetChanged();
    }
```

**删除图片后, 标题需要的操作**
```
    /**
     * 设置标题显示
     * <p>比如3 / 4
     * @param index
     */
    private void setImageTitle(int index) {
        //删除图片路径后
        if (index == 0 || imgUrls.size() == 1) {
            // 索引 == 0 || 图片集合只剩一张图
            // 就把索引值固定为1
            index = 1;
        } else if (index == imgUrls.size() - 1) {
            // 当前索引 == 图片集合的最后一张
            // 就不做任何处理
        } else {
            //否则就把索引+1便于显示
            index += 1;
        }
        tv_current_index.setText(index + " / " + imgUrls.size());
    }
```

#### 3.LargeImageAdapter
适配器的代码由于使用到图片缩放框架, 所以大部分代码都是固定写法
```
public class LargeImageAdapter extends PagerAdapter {
    private Context context;
    private List<String> imgUrls;

    public LargeImageAdapter(Context context, List<String> imgUrls) {
        this.context = context;
        this.imgUrls = imgUrls;
    }

    public List<String> getData() {
        return this.imgUrls;
    }

    public void setData(List<String> imgUrls) {
        this.imgUrls = imgUrls;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return imgUrls.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ViewPager viewPager = (ViewPager) container;
        View view = (View) object;
        viewPager.removeView(view);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Object instantiateItem(ViewGroup viewGroup, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final PhotoDraweeView photoDraweeView = (PhotoDraweeView) inflater.inflate(R.layout.include_large_image, null).findViewById(R.id.sdv_larget_image);
        PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
        controller.setUri(Uri.parse("file://" + imgUrls.get(position)));//设置图片显示
        controller.setOldController(photoDraweeView.getController());
        controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                super.onFinalImageSet(id, imageInfo, animatable);
                if (imageInfo == null) {
                    return;
                }
                photoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
            }
        });
        photoDraweeView.setOnViewTapListener(new OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                //单击退出
                ((BaseActivity) context).finish();
            }
        });
        photoDraweeView.setController(controller.build());
        try {
            viewGroup.addView(photoDraweeView, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return photoDraweeView;
    }
}
```


## 反馈
欢迎各位提issues和PRs

## 第三方库
- [BaseRecyclerViewAdapterHelper](https://github.com/CymChad/BaseRecyclerViewAdapterHelper)

## 联系我
_hexianqiao3755@gmail.com_

## 许可证

    Copyright 2017 He Qiao

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
