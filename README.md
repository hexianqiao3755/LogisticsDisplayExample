### 物流详情页
---
当在开发有订单模块`App`时基本上都会用到物流详情页


大概看了下各大电商`App`的物流详情页, 其实大同小异都差不多是一种

可能也只是部分细节的不同


**先看效果图**

![example_1](https://github.com/hexianqiao3755/LogisticsDisplayExample/blob/master/art/example_1.jpeg)
![example_2](https://github.com/hexianqiao3755/LogisticsDisplayExample/blob/master/art/example_2.jpeg)


#### Gradle配置
```
repositories { 
    maven { url "https://jitpack.io" }
} 
dependencies {
    compile 'com.github.CymChad:BaseRecyclerViewAdapterHelper:2.9.40'
}
```
`BaseRecyclerViewAdapterHelper`是一个封装了`RecyclerView`自带的`Adapter`的第三方库

---

使用前先解决`ScrollView`嵌套`RecyclerView`的冲突
之前写过一篇文章有说道这个问题

[冲突解决文章](https://www.jianshu.com/p/98f2fcfb0e22)
```
rv_logistics.setLayoutManager(new LinearLayoutManager(this));
//解决ScrollView嵌套RecyclerView出现的系列问题
rv_logistics.setNestedScrollingEnabled(false);
rv_logistics.setHasFixedSize(true);
```

#### XML使用

![draw_example](https://github.com/hexianqiao3755/LogisticsDisplayExample/blob/master/art/draw_example.jpeg)

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:paddingLeft="12dp">

    <LinearLayout
        android:layout_width="18dp"
        android:layout_height="match_parent"
        android:gravity="center_horizontal"
        android:orientation="vertical">

        <View
            android:id="@+id/v_short_line"
            android:layout_width="1dp"
            android:layout_height="15dp"
            android:background="#E6E6E6" />

        <ImageView
            android:id="@+id/iv_new"
            android:layout_width="18dp"
            android:layout_height="18dp"
            android:scaleType="fitXY"
            android:src="@mipmap/icon_logistics_new" />

        <ImageView
            android:id="@+id/iv_old"
            android:layout_width="11dp"
            android:layout_height="11dp"
            android:scaleType="fitXY"
            android:src="@drawable/shape_gray_circle"
            android:visibility="gone" />

        <View
            android:id="@+id/v_long_line"
            android:layout_width="1dp"
            android:layout_height="wrap_content"
            android:background="#E6E6E6" />
    </LinearLayout>

    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginLeft="16dp"
        android:layout_weight="1"
        android:orientation="vertical"
        android:paddingTop="11dp">

        <TextView
            android:id="@+id/tv_info"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textColor="#18C289"
            android:textSize="14sp" />

        <TextView
            android:id="@+id/tv_date"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginBottom="15dp"
            android:layout_marginTop="12dp"
            android:textColor="#18C289"
            android:textSize="12sp" />

        <View
            android:id="@+id/v_bottom_line"
            android:layout_width="match_parent"
            android:layout_height="1px"
            android:background="#E6E6E6" />
    </LinearLayout>
</LinearLayout>
```

#### JAVA使用(适配器代码)
其实核心的使用方法就是控制控件的**隐藏**和**显示**

根据不同的条件来控制
```
public class LogisticsInfoAdapter extends BaseQuickAdapter<LogisticsInfoBean, BaseViewHolder> {
    private Context context;
    private List<LogisticsInfoBean> data;

    public LogisticsInfoAdapter(Context context, int layoutResId, @Nullable List<LogisticsInfoBean> data) {
        super(layoutResId, data);
        this.context = context;
        this.data = data;
    }

    @Override
    protected void convert(BaseViewHolder helper, LogisticsInfoBean item) {
        //获取物流信息和物流时间的字体颜色, 最新的一条物流数据字体为绿色
        int newInfoColor = context.getResources().getColor(helper.getLayoutPosition() == 0 ? R.color.green : R.color.gray);
        //当前item的索引==0 && 物流数据的数量大于1条   ->  显示绿色大圆圈
        helper.setGone(R.id.iv_new, helper.getLayoutPosition() == 0 && data.size() > 1)
                //当前item的索引!=0 && 物流数据的数量大于1条   ->  显示灰色小圆圈
                .setGone(R.id.iv_old, helper.getLayoutPosition() != 0 && data.size() > 1)
                //当前item的索引 != 0    ->  显示圆点上面短一点的灰线
                .setVisible(R.id.v_short_line, helper.getLayoutPosition() != 0)
                //当前item的索引 != 物流数据的最后一条    ->  显示圆点下面长一点的灰线
                .setGone(R.id.v_long_line, helper.getLayoutPosition() != data.size() - 1)
                //当前item的索引 != 物流数据的最后一条    ->  显示物流时间下面的横向的灰线
                .setGone(R.id.v_bottom_line, helper.getLayoutPosition() != data.size() - 1)
                .setTextColor(R.id.tv_info, newInfoColor)
                .setTextColor(R.id.tv_date, newInfoColor)
                //物流信息
                .setText(R.id.tv_info, item.getAcceptStation())
                //物流时间
                .setText(R.id.tv_date, item.getAcceptTime());
    }
}
```

#### Kotlin使用(适配器代码)
```
class LogisticsInfoAdapter(var context: BaseActivity?, var data: ArrayList<LogisticsInfoBean>?)
    : BaseQuickAdapter<LogisticsInfoBean, BaseViewHolder>(R.layout.item_logistics, data) {
    override fun convert(helper: BaseViewHolder?, item: LogisticsInfoBean?) {
        //获取物流信息和物流时间的字体颜色, 最新的一条物流数据字体为绿色
        val textColor = context?.getColorFromBase(if (helper?.layoutPosition == 0) R.color.green_18C289 else R.color.text_btn_gray)
        //当前item的索引==0 && 物流数据的数量大于1条   ->  显示绿色大圆圈
        helper?.setGone(R.id.iv_new, helper.layoutPosition == 0 && data?.size!! > 1)
                //当前item的索引!=0 && 物流数据的数量大于1条   ->  显示灰色小圆圈
                ?.setGone(R.id.iv_old, helper.layoutPosition != 0 && data?.size!! > 1)
                //当前item的索引 != 0    ->  显示圆点上面短一点的灰线
                ?.setVisible(R.id.v_short_line, helper.layoutPosition != 0)
                //当前item的索引 != 物流数据的最后一条    ->  显示圆点下面长一点的灰线
                ?.setGone(R.id.v_long_line, helper.layoutPosition != data?.size!! - 1)
                //当前item的索引 != 物流数据的最后一条    ->  显示物流时间下面的横向的灰线
                ?.setGone(R.id.v_bottom_line, helper.layoutPosition != data?.size!! - 1)
                ?.setTextColor(R.id.tv_info, textColor!!)
                ?.setTextColor(R.id.tv_date, textColor!!)
                //物流信息
                ?.setText(R.id.tv_info, item?.acceptStation)
                //物流时间
                ?.setText(R.id.tv_date, item?.acceptTime)
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
